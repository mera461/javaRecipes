package net.sf.recipetools.javarecipes.fileprocessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;


public abstract class TextInputProcessor implements InputProcessor {
	private static final Logger log = LoggerFactory.getLogger(TextInputProcessor.class);
	
	abstract public List<Recipe> read(LineNumberReader s);
	
	/**
	 * read all recipes from the file
	 */
    @Override
	public List<Recipe> read(File f) {
		List<Recipe> all = new ArrayList<Recipe>(); 
		try (LineNumberReader r = new LineNumberReader(new InputStreamReader(new FileInputStream(f), getDefaultCharacterSet()))) {
			boolean done = false;
			while (! done) {
				List<Recipe> recipes = read(r);
				if (recipes!=null && recipes.size()>0) {
					all.addAll(recipes);
				} else {
					done = true;
				}
			} 
		} catch (FileNotFoundException e) {
			log.error("Could not open file f="+f.getAbsolutePath(),e);
			throw new RecipeFoxException(e);
		} catch (IOException e1) {
			log.error("Could not read file f="+f.getAbsolutePath(),e1);
			throw new RecipeFoxException(e1);
		}
		return all;
	}

	
	public String getDefaultCharacterSet() {
		return "windows-1252";
	}
}
