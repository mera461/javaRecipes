/*
 * Created on 25-10-2004
 *
 */
package net.sf.recipetools.javarecipes.format;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import net.sf.recipetools.javarecipes.fileprocessor.TextInputProcessor;
import net.sf.recipetools.javarecipes.model.Image;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ft
 * 
 */
public abstract class RecipeTextFormatter extends TextInputProcessor implements RecipeFormatter {
	private static final Logger log = LoggerFactory.getLogger(RecipeTextFormatter.class);

	static public String linebreak = System.getProperty("line.separator");

	// also write the images when formatting the recipe?
	private boolean writeImages = true;

	// the directory where the images are stored
	private File imageDir = null;

	public abstract List<Recipe> readRecipes(LineNumberReader in);

    @Override
	public List<Recipe> read(LineNumberReader in) {
		return readRecipes(in);
	}

	/**
	 * Read the recipes from the given string.
	 * 
	 * @param lines
	 * @return
	 */
	public List<Recipe> readRecipes(String lines) {
		LineNumberReader br = new LineNumberReader(new StringReader(lines));
		return readAllRecipes(br);
	}

	/**
	 * Read all the recipes from the given file
	 * 
	 * @param f
	 * @return
	 */
    @Override
	public List<Recipe> read(File f) {
		return readRecipes(f);
	}

	/**
	 * Read all the recipes from the stream
	 * 
	 * @param in
	 * @return
	 */
	public List<Recipe> readAllRecipes(LineNumberReader in) {
		List<Recipe> all = new ArrayList<Recipe>();
		boolean done = false;
		while (!done) {
			List<Recipe> recipes = readRecipes(in);
			if (recipes != null && recipes.size() > 0) {
				for (Recipe r : recipes)
					all.add(r);
			} else {
				done = true;
			}
		}
		return all;
	}

	/**
	 * Read all the recipes from the given file
	 * 
	 * @param f
	 * @return all the recipes
	 */
	public List<Recipe> readRecipes(File f) {
		try (LineNumberReader br = new LineNumberReader(new InputStreamReader(new FileInputStream(f), getDefaultCharacterSet()))) {
			return readAllRecipes(br);
		} catch (FileNotFoundException e) {
			throw new RecipeFoxException("Could not find the file: " + f.getName(), e);
		} catch (IOException e1) {
			throw new RecipeFoxException("Could not close the file: " + f.getName(), e1);
		}
	}

	/**
	 * @param name
	 * @return
	 */
	public PrintWriter initializeNewFileWriter(String name) {
		try {
			return new PrintWriter(name, getDefaultCharacterSet());
		} catch (FileNotFoundException e) {
			throw new RecipeFoxException("Could not create a new file for the writer.", e);
		} catch (UnsupportedEncodingException e) {
			throw new RecipeFoxException("Could not find an encoding for the writer.", e);
		}
	}

	public abstract void writeRecipe(PrintWriter out, Recipe recipes);

	public void writeRecipe(PrintWriter out, List<Recipe> recipes) {
		for (Recipe recipe : recipes) {
			writeRecipe(out, recipe);
		}
	}

	public void writeRecipe(PrintWriter out, Collection<Recipe> recipes) {
		for (Recipe recipe : recipes) {
			writeRecipe(out, recipe);
		}
	}

    @Override
	public String getDefaultCharacterSet() {
		return "windows-1252";
	}

	/**
	 * Format the given recipe and return the string.
	 * 
	 * @param recipe
	 *            recipe to be formatted
	 * @return the formatted recipe as a string.
	 */
	public String recipeAsString(Recipe recipe) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		PrintWriter ps = new PrintWriter(bytes);
		writeRecipe(ps, recipe);
		ps.close();
		return bytes.toString();
	}

	public String fileHeaderAsString() {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		PrintWriter ps = new PrintWriter(bytes);
		writeFileHeader(ps);
		ps.close();
		return bytes.toString();
	}

	public String fileTailAsString() {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		PrintWriter ps = new PrintWriter(bytes);
		writeFileTail(ps);
		ps.close();
		return bytes.toString();
	}

	static protected void postProcessUnits(Recipe recipe) {
	}

	/**
	 * Find the first line containing the given text
	 * 
	 * @param in
	 *            Stream to read from
	 * @param text
	 *            Text to look for
	 * @return true if the text was found
	 */
	public boolean findFirstLineContaining(LineNumberReader in, String text) {
		boolean found = false;
		String line = null;
		while (!found) {
			try {
				line = in.readLine();
			} catch (IOException e) {
				log.info("RecipeFormatter: findFirstLine: " + e);
				line = null;
				break;
			}
			if (line == null) {
				break;
			}

			// check the line for matching chars
			if (line.contains(text)) {
				found = true;
			}
		}

		return found;
	}

	/**
	 * Find the first line containing the given text
	 * 
	 * @param in
	 *            Stream to read from
	 * @param headerPattern
	 *            Pattern to look for
	 * @return true if the text was found
	 */
	public boolean findFirstLineContaining(LineNumberReader in, Pattern headerPattern) {
		boolean found = false;
		String line = null;
		while (!found) {
			try {
				line = in.readLine();
			} catch (IOException e) {
				log.info("RecipeFormatter: findFirstLine: " + e);
				line = null;
				break;
			}
			if (line == null) {
				break;
			}

			// check the line for matching chars
			if (headerPattern.matcher(line).find()) {
				found = true;
			}
		}

		return found;
	}

	public void skipEmptyLines(LineNumberReader in) {
		String line = null;
		boolean found = false;
		while (!found) {
			try {
				in.mark(1000);
				line = in.readLine();
				if (line == null || line.trim().length() > 0) {
					found = true;
					in.reset();
				}
			} catch (IOException e) {
				log.info("RecipeFormatter: findFirstLine: " + e);
				found = true;
			}
		}

	}

	/**
	 * Extract the match groups fron the given line with the given pattern. If
	 * the line does not match the pattern and exceptions is thrown.
	 * 
	 * @param line
	 *            The input line
	 * @param p
	 *            The pattern with the matching groups
	 * @param fieldName
	 *            The fieldname used in error msg if the line does not match the
	 *            pattern
	 * @return
	 */
	public String[] getMatchesOrThrow(String line, Pattern p, String fieldName) {
		Matcher m = p.matcher(line);
		if (!m.find()) {
			String msg = "Could not find the " + fieldName + " field in line=" + line;
			log.error(msg);
			throw new RecipeFoxException(msg);
		}
		String[] result = new String[m.groupCount()];
		for (int i = 1; i <= m.groupCount(); i++) {
			result[i - 1] = m.group(i);
			if (result[i - 1] != null) {
				result[i - 1] = result[i - 1].trim();
			}
		}
		return result;
	}

	/**
	 * Extract the match groups from the given line with the given pattern. If
	 * the line does not match the pattern and exceptions is thrown.
	 * 
	 * @param line
	 *            The input line
	 * @param p
	 *            The pattern with the matching groups
	 * @param fieldName
	 *            The fieldname used in error msg if the line does not match the
	 *            pattern
	 * @return
	 */
	public String[] getMatchesOrThrow(LineNumberReader bis, Pattern p, Pattern nextPattern, String fieldName) {
		boolean done = false;
		StringBuilder value = new StringBuilder();
		try {
			while (!done) {
				bis.mark(5000);
				String line = bis.readLine();
				// EOF
				if (line == null) {
					break;
				}
				if (line.trim().length() == 0) {
					continue;
				}
				// Matching next line?
				if (nextPattern != null && nextPattern.matcher(line).find()) {
					bis.reset();
					break;
				}
				// some real content
				if (value.length() > 0) {
					value.append(' ');
				}
				value.append(line);
			}
		} catch (IOException e) {
			log.error("Error finding a line matching: " + nextPattern.pattern(), e);
		}
		return getMatchesOrThrow(value.toString(), p, fieldName);
	}

	// -------------------------------------- WRITING
	// --------------------------------------

	public void writeFileHeader(PrintWriter out) {
	}

	public void writeFileTail(PrintWriter out) {
	}

	public File saveImage(Recipe recipe, Image image, String suffix) {
		// return if not configured to save images
		if (!writeImages || imageDir == null || image == null) {
			return null;
		}

		// if the image already exists and have the same length then use the
		// original
		String name = image.getName();
		// add suffix if needed
		if (suffix != null && suffix.length() > 0) {
			name.replaceAll("\\.(\\w+)$", suffix + ".$1");
		}
		if (name != null && name.length() > 0) {
			File file = new File(imageDir, name);
			if (file.exists() && file.canRead() && file.length() == image.getImage().length) {
				return file;
			}
		}

		// set the filename to be the title
		name = recipe.titleAsFilename();

		// add suffix
		if (suffix != null && suffix.length() > 0) {
			name = name + suffix;
		}

		// Get the file type
		String extension = image.getImageType();

		name = makeFilenameUnique(imageDir, name, extension);
		File file = new File(name);
		image.saveAs(file);
		return file;
	}

	/**
	 * Save the main recipe image
	 * 
	 * @param recipe
	 * @return
	 */
	public File saveMainImage(Recipe recipe) {
		// return if no images in recipe
		List<Image> images = recipe.getImages();
		if (images == null || images.size() == 0) {
			return null;
		}

		return saveImage(recipe, images.get(0), "");
	}

	public File saveDirectionImage(Recipe recipe, int imgIndex) {
		String suffix = String.format("-step-%02d", imgIndex);

		return saveImage(recipe, recipe.getDirectionImage(imgIndex), suffix);
	}

	/**
	 * Try to make a unique filename unique in the given directory and with the
	 * given extension. If a file exists with the given name an extension "-1",
	 * "-2", etc. is added to the name. The number is increased until the name
	 * is unique.
	 * 
	 * @param dir
	 *            directory
	 * @param name
	 *            base filename
	 * @param extension
	 *            filename extension
	 * @return the unique filename
	 */
	public static String makeFilenameUnique(File dir, String name, String extension) {
		String nameNo = "";
		File file = new File(dir, name + "." + extension);
		int no = 0;
		while (file.exists()) {
			no++;
			nameNo = "-" + no;
			file = new File(dir, name + nameNo + "." + extension);
		}

		return file.getAbsolutePath();
	}

	/**
	 * @return the writeImages
	 */
	public boolean isWriteImages() {
		return writeImages;
	}

	/**
	 * @param writeImages
	 *            the writeImages to set
	 */
	public void setWriteImages(boolean writeImages) {
		this.writeImages = writeImages;
	}

	/**
	 * @return the imageDir
	 */
	public File getImageDir() {
		return imageDir;
	}

	/**
	 * @param imageDir
	 *            the imageDir to set
	 */
	public void setImageDir(File imageDir) {
		if (!imageDir.exists() && imageDir.isDirectory()) {
			throw new IllegalArgumentException("The image directory: " + imageDir
					+ " does not exist or is not a directory.");
		} else {
			this.imageDir = imageDir;
		}
	}

	/**
	 * @param imageDir
	 *            the imageDir to set
	 */
	public void setImageDir(String imageDir) {
		setImageDir(new File(imageDir));
	}

	/**
	 * @return a pattern to recognize this recipe format.
	 */
	public Pattern getRecognizePattern() {
		return null;
	}

	public static String escapeXml(String str) {
		if (str == null || str.length() == 0) {
			return "";
		}
		return StringEscapeUtils.escapeXml(str);
	}

	static Pattern encodedChar = Pattern.compile("&#(\\d+);");

	public static String escapeXmlForMasterCook(String str) {
		if (str == null || str.length() == 0) {
			return "";
		}

		// change special utf-8 chars and numbers
		String result = new HtmlStringBuffer(str).changeChars().toString();
		result = escapeXml(result);
		//log.info("INPUT: "+str+", OUT: "+result);

		// Apaches escapeXml: Note that unicode characters greater than 0x7f are
		// currently
		// escaped to their numerical \\u equivalent.
		// Undo this, since the output file is not necessarily in UTF-8 format.
		// And well MasterCook does not accept UTF-8 files.
		Matcher m = encodedChar.matcher(result);
		if (!m.find()) {
			return result;
		}

		StringBuffer newTxt = new StringBuffer();
		m = encodedChar.matcher(result);
		while (m.find()) {
			int i = Integer.parseInt(m.group(1));
			m.appendReplacement(newTxt, new Character((char) i).toString());
		}
		m.appendTail(newTxt);
		return newTxt.toString();
	}

	/**
	 * Create a temporary directory and return the file
	 * 
	 * @return
	 */
	public static File createTempDirectory() {
		File dir = null;
		try {
			dir = Files.createTempDirectory("RecipeFox-").toFile();
		} catch (IOException e) {
			log.error("Could not create a temporary directory", e);
		}
		dir.deleteOnExit();
		return dir;
	}

	/**
	 * Delete everything in a given directory
	 */
	public static void deleteDirectory(File dir) {
		File[] files = dir.listFiles();
		List<File> subdirs = new ArrayList<File>();
		for (File f : files) {
			if (f.isDirectory()) {
				subdirs.add(f);
			} else {
				f.delete();
			}
		}
		for (File f : subdirs) {
			deleteDirectory(f);
		}
		dir.delete();
	}

	/**
	 * unzip a file to the given directory
	 */
	public static void unzip(File file, File dir) {
		if (!(file.exists() && file.canRead() && dir.exists() && dir.canWrite())) {
			return;
		}

		try {
			ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
			// get the zipped file list entry
			ZipEntry ze = zis.getNextEntry();
			while (ze != null) {
				// skip directories.
				if (ze.isDirectory()) {
					ze = zis.getNextEntry();
					continue;
				}
				String fileName = ze.getName();
				File newFile = new File(dir, fileName);

				// create all non exists folders
				// else you will hit FileNotFoundException for compressed folder
				new File(newFile.getParent()).mkdirs();

				FileOutputStream fos = new FileOutputStream(newFile);

				int len;
				byte[] buffer = new byte[1024];
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}

				fos.close();
				ze = zis.getNextEntry();
			}

			zis.closeEntry();
			zis.close();
		} catch (FileNotFoundException e) {
			log.error("Could not unzip file=" + file.getAbsolutePath() + " to dir=" + dir.getAbsolutePath()
					+ ". File not found.", e);
		} catch (IOException e) {
			log.error("Could not unzip file=" + file.getAbsolutePath() + " to dir=" + dir.getAbsolutePath(), e);
		}

	}

	/**
	 * zip a directory to the given file
	 */
	public static void zip(File dir, File file) {
		if (!(dir.exists() && dir.canRead())) {
			return;
		}
		byte[] data = new byte[4098];
		// create output
		try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file))) {
			// zip all files
			for (File f : dir.listFiles()) {
				try (FileInputStream in = new FileInputStream(f)) {
					// create ZipEntry and add to outputting stream.
					out.putNextEntry(new ZipEntry(f.getName()));
					// write the data.
					int len;
					while ((len = in.read(data)) > 0) {
						out.write(data, 0, len);
					}
					out.flush();
					out.closeEntry();
				}
			}
		} catch (IOException e) {
			log.error("Could not write to zip file=" + file.getAbsolutePath(), e);
			throw new RecipeFoxException(e);
		}
	}

	/**
	 * Find all files with a given extension in the given directory
	 * 
	 * @param dir
	 *            start directory
	 * @param extension
	 *            file extension
	 * @return list of files matching
	 */
	public static List<File> findFilesWithExtension(File dir, final String extension) {
		if (!dir.exists() || !dir.isDirectory()) {
			throw new RecipeFoxException("The file does not exists or is not a directory: dir=" + dir.getAbsolutePath());
		}
		final ArrayList<File> result = new ArrayList<File>();
		try {
			Files.walkFileTree(dir.toPath(), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (file.toString().toLowerCase().endsWith(extension)) {
						result.add(file.toFile());
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			throw new RecipeFoxException("Error searching for file with extension '"+extension+"' in dir="+dir.getAbsolutePath(), e);
		}
		return result;
	}

	private PrintWriter ps = null;

	@Override
	public void write(List<Recipe> recipes) {
		if (ps == null)
			return;
		writeRecipe(ps, recipes);
	}

	@Override
	public void startFile(String name) {
		startFile(new File(name));
	}


	@Override
	public void startFile(File f) {
		if (ps != null) {
			ps.close();
		}
		ps = initializeNewFileWriter(f.getAbsolutePath());
		writeFileHeader(ps);
	}

	@Override
	public void endFile() {
		if (ps == null)
			return;
		writeFileTail(ps);
		ps.close();
		ps = null;
	}
	
    @Override
	public void setConfig(String property, String value) {}
    @Override
	public String getConfig(String property) {
		return "";
	}
	
    @Override
	public boolean isImagesInSameFile() {
		return true;
	}

	
	
}
