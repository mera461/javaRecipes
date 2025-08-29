/**
 * 
 */
package net.sf.recipetools.javarecipes.format;

import java.io.File;
import java.util.List;

import net.sf.recipetools.javarecipes.fileprocessor.InputProcessor;
import net.sf.recipetools.javarecipes.fileprocessor.OutputProcessor;
import net.sf.recipetools.javarecipes.model.Recipe;

/**
 * @author Frank
 *
 */
public interface RecipeFormatter extends InputProcessor, OutputProcessor {

	/**
	 *--------------------- READING ------------------------
	 */
	@Override
	public List<Recipe> read(File f);	
	
	/**
	 *--------------------- WRITING ------------------------
	 */
	@Override
	public void write(List<Recipe> recipe); 

	@Override
	public void startFile(File f);
	
	@Override
	public void startFile(String name);
	
	@Override
	public void endFile();
	
	default boolean isImagesInSameFile() {
		return true;
	}
	
	/**
	 *--------------------- CONFIG ------------------------
	 */
	public void setConfig(String property, String value);
	public String getConfig(String property);
	
}
