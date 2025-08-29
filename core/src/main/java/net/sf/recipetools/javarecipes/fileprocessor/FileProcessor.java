package net.sf.recipetools.javarecipes.fileprocessor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;


public class FileProcessor {
	
	private static final Logger log = LoggerFactory.getLogger(FileProcessor.class);
	
	private InputProcessor inputProcessor;
	private OutputProcessor outputProcessor;
	
	/** filenames to include in the processing */
	private Pattern namePatternInclude = null;
	/** filenames to exclude from the processing */
	private Pattern namePatternExclude = null;
	
	String currentFilename = "";
	String currentZipEntry = "";

	
	/**
	 * @param inputProcessor
	 * @param outputProcessor
	 */
	public FileProcessor(InputProcessor inputProcessor,
			OutputProcessor outputProcessor) {
		super();
		this.inputProcessor = inputProcessor;
		this.outputProcessor = outputProcessor;
	}
	
	public void process(File[] files) {
		if (files==null || files.length==0) {
			return;
		}
		
		for (File file : files) {
			process(file);
		}
	}
	
	public void process(String filename) {
		process(new File(filename));
	}
	
	public void process(File start) {
		// if not readable then stop
		if (! start.canRead()) {
			return;
		}
		
		// process all files in a directory
		if (start.isDirectory()) {
			process(start.listFiles());
			return;
		} else if (! start.isFile()) {
			log.warn("Invalid filetype: filetype not dir/file.");
			return;
		}

		// Set the current filename
		currentFilename = start.getName();
		
		// does it match the name patterns
		String filename = start.getName();
		if (! processThisFile(filename)) {
			return;
		}

		if (filename.toLowerCase().endsWith("zip")) {
			processZipFile(start);
			return;
		}
	
		// send signal about starting of a new input file
		outputProcessor.startFile(start);
		
		// A binary file?
		if (inputProcessor instanceof BinaryInputProcessor) {
			try {
				processBinary(start);
			} catch (Exception e) {
				log.error("Error processing file={}",start.getAbsolutePath(), e);
				throw new RecipeFoxException(e);
			}
			outputProcessor.endFile();
			return;
		}
		
		// normal text file
		currentZipEntry = "";
		try(InputStream is = new FileInputStream(start)) {
			processText(is);
		} catch (FileNotFoundException e) {
			log.error("*** ERROR: Could not open file {}", currentFilename);
			throw new RecipeFoxException(e);
		} catch (IOException e1) {
			log.error("*** ERROR: Could not read file {}", currentFilename);
			throw new RecipeFoxException(e1);
		}
		outputProcessor.endFile();
	}
	
	void processBinary(File f) {
		BinaryInputProcessor p = (BinaryInputProcessor) inputProcessor;
		List<Recipe> recipes = null;
		try {
			recipes = p.read(f);
		} catch (RuntimeException e) {
			log.error("***ERROR in file: {}", f);
			throw new RecipeFoxException(e);
		}
		if (recipes!=null && recipes.size()>0) {
			outputProcessor.write(recipes);
		}
	}
	
	private void processText(InputStream is) {
		if (is==null) {
			return;
		}
		TextInputProcessor textProcessor = (TextInputProcessor) inputProcessor; 
		LineNumberReader reader;
		try {
			reader = new LineNumberReader(new InputStreamReader(is, textProcessor.getDefaultCharacterSet()));
		} catch (UnsupportedEncodingException e1) {
			log.error("Invalid default character set: {}", textProcessor.getDefaultCharacterSet(), e1);
			reader = new LineNumberReader(new InputStreamReader(is));
		}
		List<Recipe> recipes = null;
		boolean more = true;
		while (more) {
			try {
				recipes=textProcessor.read(reader);
				if (recipes!=null && recipes.size()>0) {
					// set the file source
					for (Recipe r: recipes) {
						String file = currentFilename;
						if (currentZipEntry.length()>0) {
							file = file + "/" + currentZipEntry;
						}
						r.setFileSource(file);
					}

					// send them to output
					outputProcessor.write(recipes);
				}
			} catch (RuntimeException e) {
				log.error("***ERROR in file: "+currentFilename + (currentZipEntry.length()>0 ? "-->"+currentZipEntry : ""), e);
			}
			if (eof(reader)) {
				more = false;
			}
		}
	}
	
	/**
	 * Test for EOF.
	 * @param is The inputstream to test for END-OF-FILE
	 * @return true if EOF
	 */
	private boolean eof(BufferedReader is) {
		try {
			is.mark(5);
			int n = is.read();
			is.reset();
			if (n == -1) {
				return true;
			}
		} catch (IOException e) {
			return true;
		}
		return false;
	}

	private boolean processThisFile(String filename) {
		boolean isInvalid = 
			   namePatternInclude!=null && ! namePatternInclude.matcher(filename).find()
		    || namePatternExclude!=null && namePatternExclude.matcher(filename).find(); 
		return ! isInvalid;
	}

	private void processZipFile(File file) {
		ZipInputStream zin = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			zin = new ZipInputStream(new BufferedInputStream(fis));
			ZipEntry entry = null;
			while ((entry=zin.getNextEntry()) != null) {
				// send signal about starting of a new input file
				outputProcessor.startFile(new File(file.getAbsolutePath()+"-"+entry.getName()));
				// process the zipped file
				processZipFileEntry(zin, entry);
				outputProcessor.endFile();
			}
		} catch (IllegalArgumentException e) {
			// filenames which is not UTF-8 characters. See
			// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4244499
			// Apache fix:
			// http://dpml.net/api/ant/1.7.0/org/apache/tools/zip/ZipFile.html
			log.error("***ERROR in file: "+currentFilename + (currentZipEntry.length()>0 ? "-->"+currentZipEntry : "") + "\n\tA file in the zip file contains non UTF-8 characters.", e );
		} catch (FileNotFoundException e1) {
			log.error("***ERROR in file: "+currentFilename + (currentZipEntry.length()>0 ? "-->"+currentZipEntry : ""), e1 );
		} catch (IOException e) {
			log.error("***ERROR in file: "+currentFilename + (currentZipEntry.length()>0 ? "-->"+currentZipEntry : ""),e );
		} finally {
			if (zin != null) {
				try {
					zin.close();
				} catch (IOException e) {
					log.error("***ERROR in file: "+currentFilename + (currentZipEntry.length()>0 ? "-->"+currentZipEntry : ""), e );
				}
			}
			if (fis!=null) {
				try {
					fis.close();
				} catch (IOException e) {
					log.error("***ERROR in file: "+currentFilename + (currentZipEntry.length()>0 ? "-->"+currentZipEntry : ""),e );
				}
			}
		}
	}

	private void processZipFileEntry(ZipInputStream zis, ZipEntry entry) {
		String filename = entry.getName();
		if (! processThisFile(filename)) {
			return;
		}
		
		currentZipEntry = entry.getName();
		
		// process as text file
		if (inputProcessor instanceof TextInputProcessor) {
			processText(zis);
		} else {
			// save it to a temp file
			File f = saveStreamToFile(entry, zis);
			processBinary(f);
			f.delete();
		}
	}
	
	private File saveStreamToFile(ZipEntry entry, ZipInputStream zis) {
		File tempFile = null;
		try {
			String name = entry.getName().replace('/', '-');
			String suffix = "";
			int posDot = name.lastIndexOf('.');
			if (posDot != -1) {
				suffix = name.substring(posDot, name.length());
				name = name.substring(0, posDot);
			}
			tempFile = File.createTempFile(name, suffix);
			tempFile.deleteOnExit();
			BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(tempFile));
			byte[] buffer = new byte[4000];
			int length = zis.read(buffer);
			while (length > 0) {
				os.write(buffer, 0, length);
				length = zis.read(buffer);
			}
			os.close();
		} catch (FileNotFoundException e) {
			throw new RecipeFoxException("Could not find the file: "+tempFile.getName(), e);
		} catch (IOException e) {
			throw new RecipeFoxException("read or write error to the tempfile:"+tempFile.getName()
									   +" entry:"+entry.getName(), e);
		}
		
		return tempFile;
	}

	/**
	 * @return the inputProcessor
	 */
	public InputProcessor getInputProcessor() {
		return inputProcessor;
	}

	/**
	 * @param inputProcessor the inputProcessor to set
	 */
	public void setInputProcessor(InputProcessor inputProcessor) {
		this.inputProcessor = inputProcessor;
	}

	/**
	 * @return the outputProcessor
	 */
	public OutputProcessor getOutputProcessor() {
		return outputProcessor;
	}

	/**
	 * @param outputProcessor the outputProcessor to set
	 */
	public void setOutputProcessor(OutputProcessor outputProcessor) {
		this.outputProcessor = outputProcessor;
	}

	/**
	 * @return the namePatternInclude
	 */
	public Pattern getNamePatternInclude() {
		return namePatternInclude;
	}

	/**
	 * @param namePatternInclude the namePatternInclude to set
	 */
	public void setNamePatternInclude(Pattern namePatternInclude) {
		this.namePatternInclude = namePatternInclude;
	}

	/**
	 * @return the namePatternExclude
	 */
	public Pattern getNamePatternExclude() {
		return namePatternExclude;
	}

	/**
	 * @param namePatternExclude the namePatternExclude to set
	 */
	public void setNamePatternExclude(Pattern namePatternExclude) {
		this.namePatternExclude = namePatternExclude;
	}


}
