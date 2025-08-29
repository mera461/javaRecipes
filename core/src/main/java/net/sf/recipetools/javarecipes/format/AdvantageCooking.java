/*
 * Created on 25-10-2004
 *
 */
package net.sf.recipetools.javarecipes.format;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import net.sf.recipetools.javarecipes.model.Ingredient;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;
import net.sf.recipetools.javarecipes.model.RecipeIngredient;
import net.sf.recipetools.javarecipes.model.Unit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author ft
 *
 */
public class AdvantageCooking extends RecipeTextFormatter {
	private static final Logger log = LoggerFactory.getLogger(AdvantageCooking.class);

	/**
	 * 
	 */
	public AdvantageCooking() {
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
			// read the header data: title, categories, no servings
			readHeader(in, recipe);
				
			// read the ingredients
			readIngredients(in, recipe);

		} catch (RuntimeException e) {
			String msg = "Error reading from line: "+in.getLineNumber()+"\n"+e.getMessage(); 
			log.error(msg);
			throw new RecipeFoxException(msg, e);
		}

		List<Recipe> result = new ArrayList<Recipe>();
		result.add(recipe);
		return result;
	}

	List<String> readLine(LineNumberReader in) {
		List<String> result = new ArrayList<String>();
		String line="";
		try {
			line = in.readLine();
			if (line==null) return null;
			line = FormattingUtils.rtrim(line);
			String[] parts = line.split("=", 2);
			result.add(parts[0]);
			result.addAll(splitCsv(parts[1]));
		} catch (IOException e) {
			String msg = "AdvantageCooking: error reading header: " + line; 
			log.info(msg);
			throw new RecipeFoxException(msg, e);
		}
		
		return result;
	}

	/**
	 * Split the given line into parts separated with comma and
	 * with quoted strings.
	 *  
	 * @param line
	 * @return
	 */
	List<String> splitCsv(String line) {
		List<String> result = new ArrayList<String>();
		int start = 0;
		for (int i=0; i<line.length(); i++) {
			if (line.charAt(i) == ',') {
				result.add(FormattingUtils.rtrim(line.substring(start, i)));
				start = i+1;
				continue;
			} else if (line.charAt(i) == '"') {
				i++;
				start = i;
				while (line.charAt(i)!='"') i++;
				result.add(FormattingUtils.rtrim(line.substring(start, i).replace("~", "\n")));
				i += 1; // ignore the following ,
				start = i+1;
			}
		}
		if (start<line.length()) {
			result.add(line.substring(start, line.length()));
		}
		return result;
	}	
	
	private void readHeader(LineNumberReader in, Recipe recipe) {
		boolean endOfHeader = false;

		while (! endOfHeader) {
			List<String> parts = readLine(in);
			if (parts == null) {
				endOfHeader = true;
			} else if ("recipetitle".equalsIgnoreCase(parts.get(0))) {
				recipe.setTitle(parts.get(1));
			} else if ("recipes".equalsIgnoreCase(parts.get(0))) {
				recipe.setDirections(parts.get(3));
				recipe.setYield(parts.get(5));
				if (parts.get(7).length()>0) recipe.setSource(parts.get(7));
				if (parts.get(8).length()>0) recipe.setPreparationTime(parts.get(8));
				endOfHeader = true;
			} else if ("setitems".equalsIgnoreCase(parts.get(0))
					   && "appliance".equalsIgnoreCase(parts.get(2))) {
				recipe.addCategory(parts.get(3));
			} else if ("setitems".equalsIgnoreCase(parts.get(0))
					   && "recipe cat".equalsIgnoreCase(parts.get(2))) {
				recipe.addCategory(parts.get(3));
			}
		}
	}

	private void readIngredients(LineNumberReader in, Recipe recipe) {
		boolean eof = false;
		HashMap<String, ArrayList<RecipeIngredient>> ingredients = new HashMap<String, ArrayList<RecipeIngredient>>();

		while (! eof) {
			List<String> parts = readLine(in);
			if (parts == null) {
				eof = true;
			} else if ("setitems".equalsIgnoreCase(parts.get(0))) {
				continue;
			} else if ("ingredients".equalsIgnoreCase(parts.get(0))) {
				continue;
			} else if ("measures".equalsIgnoreCase(parts.get(0))) {
				continue;
			} else if ("recipeingredients".equalsIgnoreCase(parts.get(0))) {
				RecipeIngredient ingr = new RecipeIngredient();
				if (parts.get(4).length()>0) ingr.setAmount(Float.parseFloat(parts.get(4)));
				if (parts.get(5).length()>0) ingr.setUnit(new Unit(parts.get(5)));
				if (parts.get(6).length()>0) ingr.setIngredient(new Ingredient(parts.get(6)));
				if (parts.get(8).length()>0) ingr.setProcessing(parts.get(8));
				String type = parts.get(7).toLowerCase();
				if (! ingredients.containsKey(type)) {
					ingredients.put(type, new ArrayList<RecipeIngredient>());
				}
				ingredients.get(type).add(ingr);
			} else if ("recipecontainers".equalsIgnoreCase(parts.get(0))) {
				RecipeIngredient ingr = new RecipeIngredient();
				if (parts.get(3).length()>0) ingr.setAmount(Float.parseFloat(parts.get(3)));
				if (parts.get(4).length()>0) ingr.setIngredient(new Ingredient(parts.get(4)));
				String type = "container";
				if (! ingredients.containsKey(type)) {
					ingredients.put(type, new ArrayList<RecipeIngredient>());
				}
				ingredients.get(type).add(ingr);
			}
		}
		
		ArrayList<RecipeIngredient> all = new ArrayList<RecipeIngredient>();
		if (ingredients.containsKey("cooking day")) {
			all.add(new RecipeIngredient("S>On cooking day:"));
			all.addAll(ingredients.get("cooking day"));
		}
		if (ingredients.containsKey("serving day")) {
			all.add(new RecipeIngredient("S>On serving day:"));
			all.addAll(ingredients.get("serving day"));
		}
		if (ingredients.containsKey("container")) {
			all.add(new RecipeIngredient("S>Containers:"));
			all.addAll(ingredients.get("container"));
		}
		recipe.setIngredients(all);
	}
	

	// ---------------------------------------------------------------
	//
	//          writing 
	//
	// ---------------------------------------------------------------
	@Override
	public void writeRecipe(PrintWriter out, Recipe recipe) {
		throw new RecipeFoxException("Writing in Advantage Cooking format is not yet supported");
	}
	
	/**
	 * @return a pattern to recognize this type of recipes.
	 */
    @Override
	public Pattern getRecognizePattern() {
		return Pattern.compile("^RecipeTitle=\"", Pattern.CASE_INSENSITIVE+Pattern.MULTILINE);
	}
}
