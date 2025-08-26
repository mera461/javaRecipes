package net.sf.recipetools.javarecipes.fileprocessor;

import java.io.File;
import java.util.List;

import net.sf.recipetools.javarecipes.model.Recipe;

public interface OutputProcessor {
	void write(List<Recipe> recipe);
	
	void startFile(File name);
	void startFile(String name);
	void endFile();
}
