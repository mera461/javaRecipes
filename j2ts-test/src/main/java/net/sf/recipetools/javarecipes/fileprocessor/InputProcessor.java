package net.sf.recipetools.javarecipes.fileprocessor;

import java.io.File;
import java.util.List;

import net.sf.recipetools.javarecipes.model.Recipe;

public interface InputProcessor {
	
	List<Recipe> read(File f);

}
