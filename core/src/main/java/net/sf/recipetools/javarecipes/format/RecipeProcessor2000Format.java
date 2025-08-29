/*
 * Created on 25-10-2004
 *
 */
package net.sf.recipetools.javarecipes.format;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;
import net.sf.recipetools.javarecipes.model.RecipeIngredient;


/**
 * @author ft
 *
 */
public class RecipeProcessor2000Format extends RecipeTextFormatter {
	private static final Logger log = LoggerFactory.getLogger(RecipeProcessor2000Format.class);

	private static Pattern sourcePattern = Pattern.compile("source\\s+(.*)\\s*$", Pattern.CASE_INSENSITIVE);
	
	
	/**
	 * 
	 */
	public RecipeProcessor2000Format() {
		super();
	}

	// ---------------------------------------------------------------
	//
	//          reading 
	//
	// ---------------------------------------------------------------
	
    @Override
	public List<Recipe> readRecipes(LineNumberReader in) {
		Recipe recipe = new Recipe();

		try {
			// find the first line in the recipe
			boolean found = findFirstLine(in);
			if (! found) {
				return null;
			}
				
			// read the header data: title, categories, no servings
			readHeader(in, recipe);
				
			// read the ingredients
			readIngredients(in, recipe);

			// read the ingredients
			readDirections(in, recipe);
		} catch (RuntimeException e) {
			String msg = "Error reading from line: "+in.getLineNumber()+"\n"+e.getMessage(); 
			log.error(msg);
			throw new RecipeFoxException(msg, e);
		}

		return Arrays.asList(recipe);
	}

	private boolean findFirstLine(LineNumberReader in) {
		boolean found = false;
		String line = null;
		while (! found) {
			try {
				line = in.readLine();
			} catch (IOException e) {
				log.info("RecipeProcessor2000: findFirstLine: "+e);
				line = null;
				break;
			}
			if (line == null) {
				break;
			}

			// check the line for matching chars
			if (line.startsWith("RPW-----")) {
				found = true;
			}
		}
		
		return found;
	}

	private void readHeader(LineNumberReader in, Recipe recipe) {
		String line = "";
		try {
			// get the title
			while (line.length() < 2) {
				line = FormattingUtils.rtrim(in.readLine());
			}
			recipe.setTitle(line);
			
			// get the categories 
			line = in.readLine().trim();
			for (String category: line.split("\\s+")) {
				recipe.addCategory(category);
			}

			// get the number of servings
			line = in.readLine().trim();
			if (line!=null && line.length() > 0) {
				recipe.setServings(Integer.parseInt(line));
			}

			// skip a empty line
			skipEmptyLines(in);
			
		} catch (IOException e) {
			String msg = "RecipeProcessor2000: error reading header: " + line; 
			log.info(msg);
			throw new RecipeFoxException(msg, e);
		}
		
	}

	private void readIngredients(LineNumberReader in, Recipe recipe) {
		String line = "";
		try {
			// get the next line
			for (line = in.readLine(); line != null; in.mark(1000), line = in.readLine()) {
				// end of recipe ?
				if ("+++".equals(line)) {
					return;
				}
				
				// delete trailing spaces
				line = line.trim();
				
				// empty line ?
				if (line.length() < 2) {
					continue;
				}

				// end of ingredients ?
				if (!line.contains("\t")) {
					// put the line back
					in.reset();
					break;
				}
				
				// source line?
				Matcher m = sourcePattern.matcher(line);
				if (m.find()) {
					recipe.setSource(m.group(1));
					continue;
				}

				String[] parts = line.split("\t");
				RecipeIngredient ingredient = null;
				if (parts.length == 3) {
					float amount = RecipeIngredient.getNumber(parts[0]);
					ingredient = new RecipeIngredient(amount, parts[1], parts[2]);
				} else {
					ingredient = new RecipeIngredient(line);
				}
				recipe.addIngredient(ingredient);
			}
		} catch (IOException e) {
			String msg = "RecipeProcessor2000: error reading ingredients - " + line; 
			log.info(msg);
			throw new RecipeFoxException(msg);
		} finally {
		}
	}
	
	private void readDirections(LineNumberReader in, Recipe recipe) {
		String line = "";
		StringBuilder directions = new StringBuilder();
		try {
			// get the next line
			for (line = in.readLine(); line != null; line = in.readLine()) {
				// end of recipe ?
				if ("+++".equals(line)) {
					break;
				}

				// directive
				if (directions.length() > 0) { 
					directions.append('\n');
				}
				directions.append(line.trim());
			}
			recipe.setDirections(directions.toString());
		} catch (IOException e) {
			String msg = "RecipeProcessor2000: error reading directions - " + line; 
			log.info(msg);
			throw new RecipeFoxException(msg);
		}
	}
	

	// ---------------------------------------------------------------
	//
	//          writing 
	//
	// ---------------------------------------------------------------
    @Override
	public void writeRecipe(PrintWriter out, Recipe recipe) {
		throw new RecipeFoxException("Writing in RecipeProcessor200 format is not yet supported");
	}
	
	/**
	 * @return a pattern to recognize this type of recipes.
	 */
    @Override
	public Pattern getRecognizePattern() {
		return Pattern.compile("^RPW-----", Pattern.CASE_INSENSITIVE+Pattern.MULTILINE);
	}
}
