/*
 * Created on 25-10-2004
 *
 */
package net.sf.recipetools.javarecipes.format;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.recipetools.javarecipes.model.Category;
import net.sf.recipetools.javarecipes.model.Configuration;
import net.sf.recipetools.javarecipes.model.Ingredient;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;
import net.sf.recipetools.javarecipes.model.RecipeIngredient;


/**
 * @author ft
 *
 */
public class MealMaster extends RecipeTextFormatter {
	private static final Logger log = LoggerFactory.getLogger(MealMaster.class);
	

	private static Pattern titlePattern = Pattern.compile("\\s*Title\\s*:\\s*(.*?)\\s*$",
													 Pattern.CASE_INSENSITIVE);
	private static Pattern categoriesPattern = Pattern.compile("\\s*Categories\\s*:\\s*(.*?)\\s*$",
													 Pattern.CASE_INSENSITIVE);
	private static Pattern yieldPattern = Pattern.compile("\\D*(\\d*)\\s*(\\w*)");
	private static Pattern endOfRecipePattern = Pattern.compile("^[M-]+\\s*$");
	private static Pattern ingredientHeaderPattern = Pattern.compile("^[M-]{5,5}\\W*(.+?)[\\s\\-]*$");

	boolean strictIngredientFormat = false;
	
	/**
	 * 
	 */
	public MealMaster() {
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
				log.info("MealMaster: findFirstLine: "+e);
				line = null;
				break;
			}
			if (line == null) {
				break;
			}

			// check the line for matching chars
			if ((line.startsWith("-----")
					|| line.startsWith("MMMMM"))
					&& line.indexOf("Meal-Master") >= 0) {
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
			String[] matches = getMatchesOrThrow(line, titlePattern, "Title");
			recipe.setTitle(matches[0]);
			
			// get the categories 
			line = in.readLine();
			matches = getMatchesOrThrow(line, categoriesPattern, "Categories");
			String categories[] = matches[0].trim().split(",");
			for (int i = 0; i < categories.length; i++) {
				recipe.getCategories().add(new Category(categories[i].trim()));
			}

			// get the number of servings
			line = in.readLine();
			matches = getMatchesOrThrow(line, yieldPattern, "Yield");
			if (matches[0]!=null && matches[0].length() > 0) {
				recipe.setYield(matches[0]+" "+matches[1]);
				if (matches[1] != null && matches[1].equalsIgnoreCase("servings")) {
					recipe.setServings(Integer.parseInt(matches[0]));
				}
			}

			// skip a empty line
			line = in.readLine();
			
		} catch (IOException e) {
			String msg = "Meal-Master: error reading header: " + line; 
			log.info(msg);
			throw new RecipeFoxException(msg, e);
		}
		
	}

	private void readIngredients(LineNumberReader in, Recipe recipe) {
		String line = "";
		try {
			// get the next line
			for (line = in.readLine(); line != null; in.mark(100), line = in.readLine()) {
				// end of recipe ?
				if (endOfRecipePattern.matcher(line).find()) {
					return;
				}
				
				// delete trailing spaces
				line = line.replaceAll("\\s+$", "");
				
				// empty line ?
				if (line.length() < 2) {
					break;
				}
				
				// ingredient section (header) ?
				Matcher m = ingredientHeaderPattern.matcher(line);
				if (line.length()> 40 && m.find()) {
					RecipeIngredient ingredient = new RecipeIngredient(); 
					ingredient.setIngredient(new Ingredient(line.trim()));
					recipe.addIngredient(ingredient);
					continue;
				}
				
				if (strictIngredientFormat) {
					// First part of the line.
					RecipeIngredient ingredient = RecipeIngredient.createFromFixedPositionString(line, 0, 7, 3, 29);
					recipe.addIngredient(ingredient);

					// is there a second part
					if (line.length() > 40) {
						ingredient = RecipeIngredient.createFromFixedPositionString(line, 41, 7, 3, 29);
						recipe.addIngredient(ingredient);
					}
				} else {
					recipe.addIngredient(new RecipeIngredient(line.trim()));

					// TODO: if there is a second part it doesn't necessarily split at char 40
				}
				
				// put the line back
//				in.reset();
			}
		} catch (IOException e) {
			String msg = "Meal-Master: error reading ingredients - " + line; 
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
				if (endOfRecipePattern.matcher(line).find()) {
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
			String msg = "Meal-Master: error reading directions - " + line; 
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
		if (recipe.getFileSource()!=null && recipe.getFileSource().length()>0) {
			out.println("# Original file: "+recipe.getFileSource());
		}
		
		out.println("MMMMM----- Recipe via Meal-Master (from JavaRecipes)");
		out.println();
		out.println("      Title: "+recipe.getTitle());
		StringBuilder categoryString = new StringBuilder();
		for(Category c : recipe.getCategories()) {
			categoryString.append(c.getName());
			categoryString.append(", ");
		}
		out.print( " Categories: ");
		if (categoryString.length()>0) {
			out.print(categoryString.substring(0, categoryString.length()-2));
		}
		out.println();
		out.printf ("      Yield: %d Servings\n", recipe.getServings());
		out.println();
		for (RecipeIngredient ingr : recipe.getIngredients()) {
			if (ingr.hasNoAmount()) {
				out.print("        ");
			} else {
				if (Configuration.getBooleanProperty("USE_FRACTIONS_IN_AMOUNTS")) {
					out.printf(Locale.US, "%7s ", FormattingUtils.formatNumber(ingr.getAmount()));
				} else {
					// MasterCook need numbers to be formated with a '.' not a ',' 
					out.printf(Locale.US, "%7.2f ", ingr.getAmount());
				}
			}
			String ingrString = "";
			if (ingr.hasNoUnit()) {
				out.print("   ");
			} else {
				String mappedUnit = mapUnitToMealMaster(ingr.getUnit().getName());
				if (mappedUnit==null) {
					ingrString = ingr.getUnit().getName();
				} else {
					out.printf("%2s ", mappedUnit);
				}
			}
			if (ingr.getIngredient()!=null) {
				ingrString = ingr.getIngredient().getName();
			}
			if (ingr.getProcessing()!=null && ingr.getProcessing().length()>0) {
				ingrString += " -- " + ingr.getProcessing();
			}
			out.println(splitStringWithIndent(ingrString, 11, 29));
		}
		out.println();
		out.println(recipe.getDirectionsAsString());
		if (recipe.getTipsAsString().length() > 0) {
			out.println("\nTips:\n  \""+recipe.getTipsAsString()+"\"");
		}
		if (recipe.getNutritionalInfo().length() > 0) {
			out.println("\nNutritional Information:\n  \""+recipe.getNutritionalInfo()+"\"");
		}
		if (recipe.getSource()!=null && recipe.getSource().length() > 0) {
			out.println("\nSource:\n  \""+recipe.getSource()+"\"");
		}
		if (recipe.getNote()!=null && recipe.getNote().length() > 0) {
			out.println("Notes:\n  \""+recipe.getNote()+"\"");
		}
		if (recipe.getYield()!=null && recipe.getYield().length() > 0) {
			out.printf("Yield:\n  %s\"\n", recipe.getYield());
		}
		
		for (String attName : recipe.getTextAttributes().keySet()) {
			out.printf("S(\"%s\")\n  \"%s\"\n", attName, recipe.getTextAttributes().get(attName));
		}
		
		out.println("\nMMMMM\n\n");
	}

	private static HashMap<String, String> unitMap = new HashMap<String, String>();
	static {
		unitMap.put("mg", "mg");
		unitMap.put("g", "g");
		unitMap.put("kg", "kg");
		unitMap.put("cm", "cm");
		unitMap.put("ml", "ml");
		unitMap.put("cl", "cl");
		unitMap.put("dl", "dl");
		unitMap.put("l",  "l");

		// ----------- mealmaster -------------
		unitMap.put("bn", "bn");
		unitMap.put( "c",  "c");
		unitMap.put("cc", "cc");
		unitMap.put("cg", "cg");
		unitMap.put("dg", "dg");
		unitMap.put("cl", "cl");
		unitMap.put("cn", "cn");
		unitMap.put("ct", "ct");
		unitMap.put("dl", "dl");
		unitMap.put("dr", "dr");
		unitMap.put("ds", "ds");
		unitMap.put("ea", "ea");
		unitMap.put("fl", "fl");
		unitMap.put("g", "g");
		unitMap.put("ga", "ga");
		unitMap.put("kg", "kg");
		unitMap.put("l", "l");
		unitMap.put("lb", "lb");
		unitMap.put("lg", "lg");
		unitMap.put("md", "md");
		unitMap.put("oz", "oz");
		unitMap.put("pk", "pk");
		unitMap.put("pn", "pn");
		unitMap.put("pt", "pt");
		unitMap.put("qt", "qt");
		unitMap.put("sl", "sl");
		unitMap.put("sm", "sm");
		unitMap.put("t", "t");
		unitMap.put("tb", "tb");
		unitMap.put("ts", "ts");
		unitMap.put("x", "x");

		// ----------- english ----------------
		unitMap.put("bag", "bag");
		unitMap.put("bottle", "bottle");
		unitMap.put("box", "box");
		unitMap.put("bunch", "bn");
		unitMap.put("can", "cn");
		unitMap.put("chunk", "chunk");
		unitMap.put("clove", "cl");
		unitMap.put("container", "ct");
		unitMap.put("cube", "cube");
		unitMap.put("cup", "c");
		unitMap.put("cubic centimeter", "cc");
		unitMap.put("cubic centimeters", "cc");
		unitMap.put("dozen", "dozen");
		unitMap.put("drop", "drop");
		unitMap.put("dash", "dash");
		unitMap.put("dessertspoon", "dessertspoon");
		unitMap.put("each", "ea");
		unitMap.put("envelope", "envelope");
		unitMap.put("fluid ounce", "fl");
		unitMap.put("gallon", "ga");
		unitMap.put("handful", "handful");
		unitMap.put("head", "head");
		unitMap.put("jar", "jar");
		unitMap.put("knob", "knob");
		unitMap.put("large", "lg");
		unitMap.put("loaf", "loaf");
		unitMap.put("medium", "md");
		unitMap.put("ounce", "oz");
		unitMap.put("pound", "lb");
		unitMap.put("package", "pk");
		unitMap.put("piece", "piece");
		unitMap.put("pinch", "pinch");
		unitMap.put("pint", "pt");
		unitMap.put("piece", "piece");
		unitMap.put("part", "part");
		unitMap.put("pun", "pun");
		unitMap.put("recipe", "recipe");
		unitMap.put("quart", "quart");
		unitMap.put("scoop", "scoop");
		unitMap.put("sheet", "sheet");
		unitMap.put("shot", "shot");
		unitMap.put("slice", "sl");
		unitMap.put("small", "sm");
		unitMap.put("spring", "spring");
		unitMap.put("square", "square");
		unitMap.put("stalk", "stalk");
		unitMap.put("stick", "stick");
		unitMap.put("tablespoon", "tb");
		unitMap.put("teaspoon", "ts");
		unitMap.put("tin", "tin");
		unitMap.put("tub", "tub");
		unitMap.put("whole", "x");
	}
	
	String mapUnitToMealMaster(String name) {
		if (! unitMap.containsKey(name)) {
			return null;
		}
		String mmUnit = unitMap.get(name);
		if (mmUnit.length() > 2) {
			return null;
		}
		return mmUnit;
	}

	private static final String SPACES = "                                                            ";
	String splitStringWithIndent(String str, int indent, int maxLength) {
		if (str.length()<=maxLength) {
			return str;
		}
		
		StringBuilder original = new StringBuilder(str);
		StringBuilder formatted = new StringBuilder();
		boolean first = true;
		
		while (original.length()>0) {
			int end = 0;
			if (! first) {
				end = Math.min(maxLength-2, original.length());
				formatted.append('\n');
				formatted.append(SPACES.substring(0, indent));
				formatted.append("- ");
			} else {
				end = Math.min(maxLength, original.length());
				first = false;
			}
			formatted.append(original.subSequence(0, end));
			original.delete(0, end);
		}
		
		return formatted.toString();
	}
	
	/**
	 * @return a pattern to recognize this type of recipes.
	 */
    @Override
	public Pattern getRecognizePattern() {
		return Pattern.compile("^[M\\-]{5,}.*Meal-Master", Pattern.CASE_INSENSITIVE+Pattern.MULTILINE);
	}

	/**
	 * @return the strictIngredientFormat
	 */
	public boolean isStrictIngredientFormat() {
		return strictIngredientFormat;
	}

	/**
	 * @param strictIngredientFormat the strictIngredientFormat to set
	 */
	public void setStrictIngredientFormat(boolean strictIngredientFormat) {
		this.strictIngredientFormat = strictIngredientFormat;
	}
	
	
}
