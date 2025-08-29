package net.sf.recipetools.javarecipes.fileprocessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.recipetools.javarecipes.format.RecipeTextFormatter;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;


public class RecipeFileWriter implements OutputProcessor {
	
	private static final Logger log = LoggerFactory.getLogger(RecipeFileWriter.class);

	private RecipeTextFormatter formatter = null;
	
	private boolean ignoreEmptyRecipes = true;

	// keep the filenames of the original input file
	private boolean keepFilenames = true;
	
	/** The maximum number of recipes per file or -1 if no limit.
	 * Please note that MasterCook is only capable of importing 8000 recipes in one file.
	 */
	private int maxNoRecipesPerFile = -1;
	
	private String filenameFormat = "Recipes-%03d";
	
	private String filenameExtension = "txt";
	
	private int recipeCount = 0;
	private int fileCount = -1;
	private PrintWriter printWriter = null;

	/**
	 * To be used from JavaScript, because it all is Objects.
	 * @param formatter
	 * @param maxNoRecipesPerFile
	 * @param filenameFormat
	 */
	public RecipeFileWriter(Object formatter, int maxNoRecipesPerFile, String filenameFormat) {
		super();
		if (formatter instanceof RecipeTextFormatter) {
			this.formatter = (RecipeTextFormatter) formatter;
		} else {
			throw new RecipeFoxException("invalid formattertype="
						+formatter.getClass().getName()+" Should be RecipeFormatter");
		}
		this.maxNoRecipesPerFile = maxNoRecipesPerFile;
		this.filenameFormat = filenameFormat;
	}

	/**
	 * @param formatter
	 * @param maxNoRecipesPerFile
	 * @param filenameFormat
	 */
	public RecipeFileWriter(RecipeTextFormatter formatter, int maxNoRecipesPerFile,
			String filenameFormat) {
		super();
		this.formatter = formatter;
		this.maxNoRecipesPerFile = maxNoRecipesPerFile;
		this.filenameFormat = filenameFormat;
	}

	/**
	 * @param formatter
	 * @param maxNoRecipesPerFile
	 */
	public RecipeFileWriter(RecipeTextFormatter formatter, int maxNoRecipesPerFile) {
		super();
		this.formatter = formatter;
		this.maxNoRecipesPerFile = maxNoRecipesPerFile;
	}

	/**
	 * To be used from JavaScript, because it all is Objects.
	 * @param formatter
	 */
	public RecipeFileWriter(Object formatter) {
		super();
		if (formatter instanceof RecipeTextFormatter) {
			this.formatter = (RecipeTextFormatter) formatter;
		} else {
			throw new RecipeFoxException("invalid formattertype="
						+formatter.getClass().getName()+" Should be RecipeFormatter");
		}
	}

	/**
	 * @param formatter
	 */
	public RecipeFileWriter(RecipeTextFormatter formatter) {
		super();
		this.formatter = formatter;
	}
	
    @Override
	public void write(List<Recipe> recipes) {
		for (Recipe r : recipes) {
			write(r);
		}
	}	
	
	public void write(Recipe recipe) {
		// ignore empty recipes?
		if (ignoreEmptyRecipes
			&& (recipe.getTitle() == null 
				|| recipe.getTitle().equals("UNKNOWN TITLE"))
			&& recipe.getIngredients().size()==0
			&& (recipe.getDirections()==null
				|| recipe.getDirectionsAsString().length()<10)) {
			return;
		}
		
		
		// no stream yet?
		if (printWriter == null) {
			initializeNewStream(null);
		}
		
		// start a new file?
		if (maxNoRecipesPerFile>0 && recipeCount>0 && recipeCount%maxNoRecipesPerFile==0) {
			initializeNewStream(null);
		}
		
		recipeCount++;
		formatter.writeRecipe(printWriter, recipe);
	}

	private void initializeNewStream(File file) {
		if (printWriter!=null) {
			formatter.writeFileTail(printWriter);
			printWriter.close();
		}
		
		String name = "";
		if (keepFilenames) {
			if (file == null) {
				throw new RecipeFoxException("new filename cannot be null");
			}
			name = file.getAbsolutePath();
			name = name.replaceAll("\\.\\w+$", ".converted."+filenameExtension);
		} else {
			fileCount++;
			name = String.format(filenameFormat, fileCount)+"."+filenameExtension;
		}
		File newFile = new File(name);
		String basename = newFile.getName().replaceAll("\\.\\w+$", "");
		name = RecipeTextFormatter.makeFilenameUnique(newFile.getParentFile(), basename, filenameExtension);
		
		try {
			printWriter = new PrintWriter(name, formatter.getDefaultCharacterSet());
			formatter.writeFileHeader(printWriter);
		} catch (FileNotFoundException e) {
			printWriter = null;
			log.error("File not found: "+name, e);
		} catch (UnsupportedEncodingException e) {
			printWriter = null;
			log.error("Unknown encoding: "+formatter.getDefaultCharacterSet(), e);
		}
	}
	
	public void close() {
		if (printWriter!=null) {
			formatter.writeFileTail(printWriter);
			printWriter.close();
			printWriter = null;
		}
	}

	/**
	 * @return the ignoreEmptyRecipes
	 */
	public boolean isIgnoreEmptyRecipes() {
		return ignoreEmptyRecipes;
	}

	/**
	 * @param ignoreEmptyRecipes the ignoreEmptyRecipes to set
	 */
	public void setIgnoreEmptyRecipes(boolean ignoreEmptyRecipes) {
		this.ignoreEmptyRecipes = ignoreEmptyRecipes;
	}

	/**
	 * @return the keepFilenames
	 */
	public boolean isKeepFilenames() {
		return keepFilenames;
	}

	/**
	 * @param keepFilenames the keepFilenames to set
	 */
	public void setKeepFilenames(boolean keepFilenames) {
		this.keepFilenames = keepFilenames;
	}

	/**
	 * @return the filenameExtension
	 */
	public String getFilenameExtension() {
		return filenameExtension;
	}

	/**
	 * @param filenameExtension the filenameExtension to set
	 */
	public void setFilenameExtension(String filenameExtension) {
		this.filenameExtension = filenameExtension;
	}

    @Override
	public void startFile(File name) {
		initializeNewStream(name);
	}

    @Override
	public void startFile(String name) {
		startFile(new File(name));
	}

    @Override
	public void endFile() {
		close();
	}
}
