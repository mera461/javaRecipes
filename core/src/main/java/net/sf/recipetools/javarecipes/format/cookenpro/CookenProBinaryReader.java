/**
 * 
 */
package net.sf.recipetools.javarecipes.format.cookenpro;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import net.sf.recipetools.javarecipes.fileprocessor.BinaryInputProcessor;
import net.sf.recipetools.javarecipes.format.RecipeFormatter;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;

/**
 * @author Frank
 *
 */
public class CookenProBinaryReader implements RecipeFormatter, BinaryInputProcessor {

	CookenProBinaryReader theRightVersion = null;
	
	public CookenProBinaryReader() {
	}
	
	public CookenProBinaryReader(File file) {
		initializeWithFile(file);
	}

	/**
	 * Test if the given file is version 9
	 * @param file
	 * @return
	 */
	public static boolean isFileVersion9(File file) {
		boolean isVersion9 = false;
		try (ZipFile zip = new ZipFile(file)) {
			isVersion9 = true;
		} catch (ZipException e) {
			isVersion9 = false;
		} catch (IOException e) {
			isVersion9 = false;
		}
		return isVersion9;
	}
	
	
	public void initializeWithFile(File file) {
		if (! file.exists()) {
			throw new RecipeFoxException("The file does not exists: file="+file.getAbsolutePath());
		}
		
		if (! file.getName().toLowerCase().endsWith("dvo")) {
			throw new RecipeFoxException("The reader should point to a .dvo export file:\nfile="+file.getAbsolutePath());
		}

		// use the right version
		if (isFileVersion9(file)) {
			theRightVersion = new CookenProV9BinaryReader();
		} else {
			theRightVersion = new CookenProV8BinaryReader();
		}
		theRightVersion.initializeWithFile(file);
	}
	
	/**
	 * Return all recipes in the file
	 * @see net.sf.recipetools.javarecipes.fileprocessor.BinaryInputProcessor#read(java.io.File)
	 */
    @Override
	public List<Recipe> read(File f) {
		initializeWithFile(f);
		return theRightVersion.readAllRecipes();
	}

	List<Recipe> readAllRecipes() {
		return theRightVersion.readAllRecipes();
	}	
	
	@Override
	public void write(List<Recipe> recipe) {
		throw new RecipeFoxException("CookenPro formatter does not support writing.");
	}

	@Override
	public void startFile(String name) {
		throw new RecipeFoxException("CookenPro formatter does not support writing.");
	}

	@Override
	public void startFile(File f) {
		throw new RecipeFoxException("CookenPro formatter does not support writing.");
	}

	@Override
	public void endFile() {
		throw new RecipeFoxException("CookenPro formatter does not support writing.");
	}
	
    @Override
	public void setConfig(String property, String value) {}
    @Override
	public String getConfig(String property) {
		return "";
	}
	
}
