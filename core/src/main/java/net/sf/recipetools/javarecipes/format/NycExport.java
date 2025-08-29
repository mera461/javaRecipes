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

import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;
import net.sf.recipetools.javarecipes.model.RecipeIngredient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author ft
 *
 */
public class NycExport extends RecipeTextFormatter {
	private static final Logger log = LoggerFactory.getLogger(NycExport.class);
	
	private static Pattern recipeSeparatorPattern = Pattern.compile("^\\s*@@@@@\\s*Now\\s*You're\\s*Cooking!\\s*Export\\s*Format\\s*$", 
													Pattern.CASE_INSENSITIVE);

	private static Pattern yieldPattern = Pattern.compile("^Yield:\\s*(\\d+.*)", 
			Pattern.CASE_INSENSITIVE);
	
	
	/**
	 * 
	 */
	public NycExport() {
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
			boolean found = findFirstLineContaining(in, recipeSeparatorPattern);
			if (! found) {
				return null;
			}
			
			// read the header data: title, categories, no servings
			readHeader(in, recipe);
			
			// read the ingredients
			readIngredients(in, recipe);

			// try to adjust strange things in the ingredients.
			adjustNycIngredients(recipe);
			
			// read the ingredients
			readDirections(in, recipe);
			
			// delete trailing info from directions
			adjustDirections(recipe);

		} catch (RuntimeException e) {
			throw new RecipeFoxException("*** ERROR in title: "+recipe.getTitle(), e);
		}
		
		return Arrays.asList(recipe);
	}

	/**
	 * NYC has max 35 chars for the ingredient name and put the rest in the processing
	 * This means that lines can split with ';' in strange positions.
	 * ===== Examples with ';' in pos 35
	 * quaker oats, uncooked - (quick or o; ld-fashioned)
	 * hershey's chocolate chips (semi-swe; et)
	 * cocoa - if chocolate cake - is desi; red
	 * double graham crackers, broken (3 1; /3 cups)
	 * milk, soymilk or water - (if needed; )
	 * vanilla milk chips or vanilla candy; coating
	 * ===== Examples with ';' in pos 34
	 * coarsely chopped hazelnuts roasted; oregon hazelnuts
	 * clarified butter, melted and ooled; to lukewarm
	 * hershey's®  semi-sweet bakin broke; into pieces, *
	 * semi-sweet chocolate mini- morsels; divided
	 * ===== Examples with ';' in pos 33
	 * c and h golden brown sugar - firmly; packed
	 * 
	 * ===== Examples with ';' in earlier position (Also counting the unit name?) 
	 * 1.0 package caramel candies (14oz) [unw; raped] (pos=27.. +7 for unitname)
	 * 8.0 oz pkg cream cheese, at room temperatu; re (pos=31)
	 * 2.0 (1 pint) baskets, strawberries, hul; led, halved
	 * 0.5 cup chopped nuts (can be a mix; ture of nuts) -- (1/2  to 1) (pos=26)
	 * 2.0 small boxes french vanilla instant puddin; g (pos=29)
	 * 
	 * Users can choose to put them earlier like:
	 * 
	 * @param recipe
	 */
	private void adjustNycIngredients(Recipe recipe) {
		for (RecipeIngredient ingr : recipe.getIngredients()) {
			if (Math.abs(ingr.getAmount()-1.0)<0.001
				&& ingr.hasNoUnit()) {
				//System.out.println("Strange ingr:"+ingr.toString());
			}
			String name = ingr.getIngredient().getName();
			int pos = name.indexOf(';');
			int unitLength = ingr.hasNoUnit() ? 0 : ingr.getUnit().getName().length();
			if (35==pos
				|| 35==pos+1+unitLength) {
				// delete the ';' and the following space
				ingr.getIngredient().setName(name.substring(0, pos)
										   + name.substring(pos+2));
			}
			if (34==pos
					|| 34==pos+1+unitLength) {
					// delete the ';' but NOT the following space
					ingr.getIngredient().setName(name.substring(0, pos)
											   + name.substring(pos+1));
				}
//			if (pos > 25) {
//				System.out.println("Strange ingr (pos="+pos+")\nBEFORE:"+name+"\nAFTER :"+ingr.getIngredient().getName());
//			}
		}
	}

	/**
	 * Remove trailing lines from the directions. They typically look like this:
	 * 
	 * Yield: 4 servings
	 * 
	 *   NYC Nutrilink: N0^00000,N0^00000,N0^00000,N0^00000
	 *   NYC Nutrilink: N0^00000,N0^00000,N0^00000
	 *   
	 *   ** Exported from Now You're Cooking! v5.71 **
	 * 
	 * @param recipe The recipe to be fixed.
	 */
	private void adjustDirections(Recipe recipe) {
		String[] lines = recipe.getDirectionsAsString().split("\n");
		int endLine = -1;
		for(int i=lines.length-1; i>=0; i--) {
			// skip if empty line
			if (lines[i].trim().length()==0) continue;
			// skip if is a line like "** Exported from Now You're Cooking! v5.71 **"
			if (lines[i].contains("xported from")) continue;
			// skip lines with nutrilink NYC Nutrilink: N0^00000,N0^00000,N0^00000
			if (lines[i].contains("utrilink")) continue;
			// yield?
			Matcher m = yieldPattern.matcher(lines[i]);
			if (m.find()) {
				recipe.setYield(m.group(1));
				continue;
			}
			
			// found a real directions line
			endLine = i;
			break;
		}
		if (endLine == -1) {
			recipe.setDirections("");
		} else {
			StringBuilder sb = new StringBuilder();
			for (int i=0; i<=endLine; i++) {
				sb.append(lines[i]);
				sb.append('\n');
			}
			recipe.setDirections(sb.toString());
		}
	}

	private void readHeader(LineNumberReader in, Recipe recipe) {
		String line = "";
		try {
			// skip empty line
			skipEmptyLines(in);
			
			// get the title
			line = in.readLine();
			recipe.setTitle(line.trim());
			
			// skip empty line
			skipEmptyLines(in);
			
			// get the categories
			line = in.readLine();
			if (!line.contains("none")) {
				String[] categories = line.split(",");
				for (String cat : categories) {
					recipe.addCategory(cat);
				}
			}
		} catch (IOException e) {
			String msg = "NycExport: error reading header" + line; 
			log.info(msg);
			throw new RecipeFoxException(msg);
		}
		
	}
	
	private void readIngredients(LineNumberReader in, Recipe recipe) {
		String line = "";
		try {
			// skip empty lines
			skipEmptyLines(in);
			
			// get the next line
			for (line = in.readLine(); line != null; in.mark(5000), line = in.readLine()) {
				// end of recipe ?
				if (recipeSeparatorPattern.matcher(line).find()) {
					// put the line back
					in.reset();
					return;
				}
				
				// empty line? Then skip to directions
				if (line.trim().length() < 2) {
					break;
				}
				
				// First part of the line.
				RecipeIngredient ingredient = new RecipeIngredient(line);
				recipe.addIngredient(ingredient);
			}
		} catch (IOException e) {
			String msg = "NycExport: error reading ingredients - " + line; 
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
			for (line = in.readLine(); line != null; in.mark(5000), line = in.readLine()) {
				// end of recipe ?
				if (recipeSeparatorPattern.matcher(line).find()) {
					// put the line back
					in.reset();
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
			String msg = "NycExport: error reading directions - " + line; 
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
		throw new RecipeFoxException("Writing to NYC Export format not supported.");
	}

	/**
	 * @return a pattern to recognize this type of recipes.
	 */
    @Override
	public Pattern getRecognizePattern() {
		return Pattern.compile("^\\s*@@@@@\\s*Now\\s*You're\\s*Cooking!\\s*Export\\s*Format\\s*$", Pattern.CASE_INSENSITIVE+Pattern.MULTILINE);
	}
	
}
