/*
 * Created on 25-10-2004
 *
 */
package net.sf.recipetools.javarecipes.format;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import net.sf.recipetools.javarecipes.fileprocessor.BinaryInputProcessor;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;

/**
 * @author ft
 *
 */
public class LivingCookbookArchive implements AllFileInOneGo, BinaryInputProcessor, RecipeFormatter {
	LivingCookbookFDX reader = null;
	
	/**
	 * 
	 */
	public LivingCookbookArchive() {
		super();
		reader = new LivingCookbookFDX();
	}

	// ---------------------------------------------------------------
	//
	//          reading 
	//
	// ---------------------------------------------------------------
    @Override
	public List<Recipe> read(File f) {
		File dir = RecipeTextFormatter.createTempDirectory();
		RecipeTextFormatter.unzip(f, dir);
		File[] fdxFiles = dir.listFiles(new FilenameFilter() {
		    @Override
			public boolean accept(File arg0, String arg1) {
				return arg1.toLowerCase().endsWith("data.xml");
			}
		});

		// where to read the images from
		reader.setImageDir(new File(dir, "Images"));

		// process all FDX files
		List<Recipe> all = new ArrayList<Recipe>();
		for (File fdx: fdxFiles) {
			List<Recipe> recipes = reader.read(fdx);
			all.addAll(recipes);
		}
		
		// delete the temp dir
		RecipeTextFormatter.deleteDirectory(dir);
		
		return all;
	}

    @Override
	public void setConfig(String property, String value) {
        //throw new UnsupportedOperationException("No configuration needed");
	}
    @Override
	public String getConfig(String property) {
	    return "";
	}


	// ---------------------------------------------------------------
	//
	//          writing 
	//
	// ---------------------------------------------------------------
	
	@Override
	public void write(List<Recipe> recipes) {
        throw new RecipeFoxException("LivingCookbook FDXZ do not support writing");
	}

	@Override
	public void startFile(File file) {
        throw new RecipeFoxException("LivingCookbook FDXZ do not support writing");
	}

    @Override
	public void startFile(String file) {
        throw new RecipeFoxException("LivingCookbook FDXZ do not support writing");
	}
	
	@Override
	public void endFile() {
        throw new RecipeFoxException("LivingCookbook FDXZ do not support writing");
	}
	
}
