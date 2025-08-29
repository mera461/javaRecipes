package net.sf.recipetools.javarecipes.format.MasterCookBinary;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import net.sf.recipetools.javarecipes.model.Configuration;
import net.sf.recipetools.javarecipes.model.Image;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;

public class McxReader extends MastercookReader {

	boolean convertCmp = true;
	
	String leadCmdDir = "c:\\tools\\LEADCMD";
	
	public McxReader(File file) {
		setFile(file);
		// read config for LEADCMD path
		String lead = Configuration.getStringProperty("LEADCMP_DIRECTORY");
		if (lead!=null && lead.length()>0) {
			leadCmdDir = lead;
		} else {
		    convertCmp = false;
		}
		readAllImages1();
		convertAllImages();
	}
	
	// main images
	Map<Integer, Image> imagemap = new HashMap<Integer, Image>();
	
	// directive images
	// map: recipeno, step no -> image
	Map<Integer, HashMap<Integer, Image>> directiveImagemap = new HashMap<Integer, HashMap<Integer, Image>>();
	
	
	/**
	 * Read all images from the MCX file and put them into the imagemap.
	 *  
	 */
	void readAllImages1() {
		// get the addresses of the Database index parts (max 500 recipes in each part)
		@SuppressWarnings("unused")
		int noParts = 0;
		seek(dbSection.get("#dbi")+0x48);
		Deque<Long> partAddr = new ArrayDeque<Long>();
		long addr = readInt();
		while (addr != 0xa55a5aa5) {
			if (addr!=0) partAddr.add(addr);
			addr = readInt();
			noParts++;
		}
		
		// move to first index
		seek(partAddr.pop()*pagesize);

		int recipeIndex = 0;
		int noInThisPart = 0;
		while (recipeIndex < noItems) {
			long dataBlockAddr = readInt();
			@SuppressWarnings("unused")
			int no = readInt();
			@SuppressWarnings("unused")
			int recipeId = readInt();
			@SuppressWarnings("unused")
			Date changeDate = readDate();
			@SuppressWarnings("unused")
			int x1 = readInt();
			@SuppressWarnings("unused")
			int x2 = readInt();
			@SuppressWarnings("unused")
			int x3 = readInt();
			@SuppressWarnings("unused")
			int x4 = readInt();
			@SuppressWarnings("unused")
			int x5 = readInt();
			@SuppressWarnings("unused")
			int x6 = readInt();
			if (dataBlockAddr!=0) {
				readImageAt(dataBlockAddr * pagesize);
			}
			
			recipeIndex++;
			noInThisPart++;
			if (noInThisPart >= itemsPerPart) {
				if (! partAddr.isEmpty()) {
					// go to the next block of recipes
					seek(partAddr.pop()*pagesize);
				} else {
					// no more parts -> no more recipes
					break;
				}
				noInThisPart=0;
			}
		}
	}
	
	void readImageAt(long fileAddr) {
		long currentAddr = getFilePointer();
		seek(fileAddr);
		readImage();
		seek(currentAddr);
	}
	
	
	void readImage() {
        // start of recipe
        boolean found = checkMark('0');
        if (! found) return;
        int length = readInt();
        skipBytes(24);
        int recipeNo = readInt();
        String type = readFixedSizeString(4); // type= Pict or *diP
        int stepno = readInt();
        String subtype = readFixedSizeString(4); // subtype = MPic, Thmb
        skipBytes(20);

        checkMark('1');

        // read the image data
        byte[] imageData = new byte[length];
        try {
			file.readFully(imageData);
		} catch (IOException e) {
			throw new RecipeFoxException("Could not read image data at addr "+getFilePointer()+" from file "+filename, e);
		}
        
		
        // Ignore Thumbs
        if (! "Thmb".equals(subtype) && length>0) {
        	// get the image data
			@SuppressWarnings("unused")
            String header = new String(imageData,0,4);
            Image image = new Image();
            image.setImage(imageData);
			image.setName("image."+image.getImageType());
			
			// main image?
			if ("Pict".equals(type)) {
				imagemap.put(recipeNo, image);
			} else { // "*diP"
				HashMap<Integer, Image> stepMap = directiveImagemap.get(recipeNo);
				if (stepMap==null) {
					stepMap = new HashMap<Integer, Image>();
					directiveImagemap.put(recipeNo, stepMap);
				}
				stepMap.put(stepno, image);
			}
        }

        // read the end of image mark
        checkMark('z');
	}
	
	/**
	 * Get the image for the given Recipe number
	 * @param recipeNo
	 * @return
	 */
	public Image getImage(int recipeNo) {
		Image result = null;
		if (imagemap.containsKey(recipeNo)) {
			result = imagemap.get(recipeNo);
		}
		return result;
	}
	
	/**
	 * Get the directive image for the given step and Recipe number
	 * @param recipeNo
	 * @return
	 */
	public Image getDirectiveImage(int recipeNo, int stepNo) {
		Image result = null;
		if (directiveImagemap.containsKey(recipeNo)) {
			Map<Integer, Image> stepMap = directiveImagemap.get(recipeNo);
			if (stepMap.containsKey(stepNo)) {
				result = stepMap.get(stepNo);
			}
		}
		return result;
	}
	/**
	 * Convert all CMP images from the imagemap to JPEG.
	 * 
	 */
	void convertAllImages() {
		if (! convertCmp) {
			return;
		}
		// make a temporary directory
		File dir = makeTempDirectory();
		
		// save all images to convert
		Map<Image, File> tempFiles = saveAllImagesToConvert(dir);
		if (tempFiles.isEmpty()) {
			dir.delete();
			return;
		}
		
		File f = new File(leadCmdDir); 
		if (! f.exists() || ! f.isDirectory()) {
			throw new RecipeFoxException("Invalid path to LEAD commandline tool (should point to the LEADCMD directory): "+leadCmdDir);
		}

		// convert all images in dir
		convertImagesInDirectory(dir);
		
		// read the jpg images back
		for (Image image : tempFiles.keySet()) {
			String newFile = tempFiles.get(image).getAbsolutePath();
			newFile = newFile.replaceAll("\\.CMP$", ".jpg");
			image.loadFromFile(newFile);
			// delete the jpg file.
			new File(newFile).delete();
			// delete the CMP file
			tempFiles.get(image).delete();
		}

		// delete the temp directory
		dir.delete();
		
	}
	
	/**
	 * Call the LEAD command tool to convert all images in the given
	 * directory 
	 * @param dir
	 */
	void convertImagesInDirectory(File dir) {
		// convert it
		//lfc D:\Pr\Projects\Perl\McxExtract\in d:\ /S /F=FILE_JFIF /Q0 /NOUI
		String path = System.getProperty("java.library.path");
		String pathSep = System.getProperty("path.separator");
		String newPath = path+pathSep+leadCmdDir+"\\"+"bin";
		String[] env = new String[] {
				"PATH="+newPath
		};
		String cmd = leadCmdDir + "\\lfc "+dir.getAbsolutePath() +" /S /F=FILE_JFIF /Q15 /NOUI";
		Process proc = null;
		try {
			int fileCountBefore = dir.list().length;
			
			proc = Runtime.getRuntime().exec(cmd, env);

			// because of limited buffers the exec may hang if the output isn't read.
			// instead of reading the output it is easier to wait for the files to be
			// genereated.

			boolean done = false;
			InputStream in = proc.getInputStream();
			BufferedReader inr = new BufferedReader (new InputStreamReader(in));
			InputStream err = proc.getErrorStream();
			@SuppressWarnings("unused")
			BufferedReader errr = new BufferedReader(new InputStreamReader(err));
			int exitcode = -1;
			@SuppressWarnings("unused")
			String line = null;
			int noTries = 0;
			while (! done && noTries<500) {
				
				// wait a little
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					// just continue
				}
				
				// skip any input on stdin and stderr
				/*
				int no = in.available();
				if (no>0)  in.skip(no);
				no = err.available();
				if (no>0)  err.skip(no);
				*/
				while ((line = inr.readLine()) != null) {
				}
				
				
				// test if we are done
				try {
					exitcode = proc.exitValue();
					done = true;
				} catch (Exception e) {
					done = false;
				}
				noTries++;
			}
			if (exitcode!=1) {
				throw new RecipeFoxException("Could not convert the CMP images. ImageCount before="+fileCountBefore+" after="+dir.list().length+ " exitcode="+exitcode+" noTries="+noTries);
			}
			
			if (dir.list().length!=2*fileCountBefore) {
				throw new RecipeFoxException("Could not convert the CMP image. ImageCount before="+fileCountBefore+" after="+dir.list().length+" noTries="+noTries);
			}
		} catch (IOException e) {
			throw new RecipeFoxException("Could not convert the CMP image.", e);
		} finally {
			if (proc!=null) proc.destroy();
		}
	}
	
	/**
	 * Save all CMP images to a temp file.
	 * @return
	 */
	Map<Image, File> saveAllImagesToConvert(File dir) {
		Map<Image, File> tempFiles = new HashMap<Image, File>();
		
		for (Image image : imagemap.values()) {
			if (! "CMP".equals(image.getImageType())) {
				continue;
			}
			File tempFile = image.saveToTemp(dir);
			tempFiles.put(image, tempFile);
		}
		
		return tempFiles;
	}
	
	/**
	 * Create a temporary directory
	 * @return
	 */
	File makeTempDirectory() {
		File tempFile;
		try {
			tempFile = File.createTempFile("recipefox-images-", "");
			tempFile.deleteOnExit();
			if (!tempFile.delete()) {
				throw new RecipeFoxException("Could not delete the temporary file.");
			}
			if (!tempFile.mkdir()) {
				throw new RecipeFoxException("Could not create the temporary dir.");
			}
		} catch (IOException e) {
			throw new RecipeFoxException("Could not create the temporary file.", e);
		}
        return tempFile;      
	}

    /**
     * @return the imagemap
     */
    public Map<Integer, Image> getImagemap() {
        return imagemap;
    }

    /**
     * @return the directiveImagemap
     */
    public Map<Integer, HashMap<Integer, Image>> getDirectiveImagemap() {
        return directiveImagemap;
    }
	
}
