/*
 * Created on 25-10-2004
 *
 */
package net.sf.recipetools.javarecipes.format;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.recipetools.javarecipes.fileprocessor.BinaryInputProcessor;
import net.sf.recipetools.javarecipes.model.Recipe;

/**
 * @author ft
 *
 */
public class MasterCookArchive implements AllFileInOneGo, BinaryInputProcessor, RecipeFormatter {
	
	private static final Logger log = LoggerFactory.getLogger(MasterCookArchive.class);
	
	MasterCookXml reader = null;
	
	/**
	 * 
	 */
	public MasterCookArchive() {
		super();
		reader = new MasterCookXml();
		reader.setWriteImages(true);
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
		File[] mx2files = dir.listFiles(new FilenameFilter() {
		    @Override
			public boolean accept(File arg0, String arg1) {
				return arg1.toLowerCase().endsWith("mx2");
			}
		});

		// where to read the images from
		reader.setImageDir(dir);

		// process all MX2 files
		List<Recipe> all = new ArrayList<Recipe>();
		for (File mx2: mx2files) {
			List<Recipe> recipes = reader.read(mx2);
			all.addAll(recipes);
		}
		
		// delete the temp dir
		RecipeTextFormatter.deleteDirectory(dir);
		
		return all;
	}

	/**
	 * @return the theMasterCookProgram
	 */
	public File getTheMasterCookProgram() {
		return reader.getTheMasterCookProgram();
	}

	/**
	 * @param theMasterCookProgram the theMasterCookProgram to set
	 */
	public void setTheMasterCookProgram(File theMasterCookProgram) {
		reader.setTheMasterCookProgram(theMasterCookProgram);
	}
	
	/**
	 * @param theMasterCookProgram the theMasterCookProgram to set
	 */
	public void setTheMasterCookProgram(String theMasterCookProgram) {
		setTheMasterCookProgram(new File(theMasterCookProgram));
	}
	
    @Override
	public void setConfig(String property, String value) {
		setTheMasterCookProgram(value);
	}
    @Override
	public String getConfig(String property) {
		return getTheMasterCookProgram().getAbsolutePath();
	}
	
	


	// ---------------------------------------------------------------
	//
	//          writing 
	//
	// ---------------------------------------------------------------
	private File outputFile = null;
	private File dir = null;
	private PrintWriter mx2 = null;
	
	@Override
	public void write(List<Recipe> recipes) {
		reader.writeRecipe(mx2, recipes);
	}

	@Override
	public void startFile(File file) {
		if (outputFile != null) {
			endFile();
		}
		outputFile = file;
		dir = RecipeTextFormatter.createTempDirectory();
		String mx2name = outputFile.getName().replaceAll("\\.\\w+$", ".mx2");
		try {
			mx2 = new PrintWriter(new File(dir, mx2name), "windows-1252");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			log.error("Could not write to file="+mx2name, e);
		}
		reader.setImageDir(dir);
		reader.writeFileHeader(mx2);
	}

    @Override
	public void startFile(String file) {
		if (file!=null && file.length()>0) {
			startFile(new File(file));
		}
	}
	
	@Override
	public void endFile() {
		reader.writeFileTail(mx2);
		try {
			mx2.close();
			RecipeTextFormatter.zip(dir, outputFile);
		} finally {
			RecipeTextFormatter.deleteDirectory(dir);
			outputFile = null;
			dir = null;
			mx2 = null;
		}
	}
	
}
