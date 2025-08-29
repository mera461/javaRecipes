/*
 * Created on 25-10-2004
 *
 */
package net.sf.recipetools.javarecipes.format;

import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import net.sf.recipetools.javarecipes.model.Category;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;
import net.sf.recipetools.javarecipes.model.RecipeIngredient;


/**
 * @author ft
 */
public class MasterCookGeneric extends RecipeTextFormatter {
	
	private static Pattern recipeSeparatorPattern = Pattern.compile("^\\s*@@@@@\\s*$");
	private static Pattern recipeEndPattern = Pattern.compile("^\\s*-----\\s*$");
	private static Pattern blankLine = Pattern.compile("^\\s*$");
	
	/**
	 * 
	 */
	public MasterCookGeneric() {
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
			
			String line = "";
			// skip empty line
			skipEmptyLines(in);

			// get the title
			line = in.readLine();
			recipe.setTitle(line.trim());

			// skip empty line
			skipEmptyLines(in);

			// read the ingredients until next blank line
			line = in.readLine();
			while (line != null && !blankLine.matcher(line).matches()) {
				RecipeIngredient ingr = new RecipeIngredient(line);
				recipe.addIngredient(ingr);
				line = in.readLine();
			}

			if (line == null) return Arrays.asList(recipe);
			
			// skip empty line
			skipEmptyLines(in);
			
			// read the directions until recipe separator
			line = in.readLine();
			StringBuilder directions = new StringBuilder();
			while (line != null
					&& !(recipeEndPattern.matcher(line).matches()
						 || recipeSeparatorPattern.matcher(line).matches())) {
				directions.append(line);
				directions.append('\n');
				line = in.readLine();
			}
			recipe.setDirections(directions.toString().trim());
			
		} catch (Exception e) {
			throw new RecipeFoxException("*** ERROR in title: "+recipe.getTitle(), e);
		}
		
		return Arrays.asList(recipe);
	}


	// ---------------------------------------------------------------
	//
	//          writing 
	//
	// ---------------------------------------------------------------
    @Override
	public void writeRecipe(PrintWriter out, Recipe recipe) {
		out.print("                   *  Exported from  MasterCook  *");
		newLine(out);
		newLine(out);
		out.print("                    "+recipe.getTitle());
		newLine(out);
		newLine(out);
		out.printf("Recipe By     : %s", recipe.getAuthor());
		newLine(out);
		out.printf ("Serving Size  : %5d Preparation Time : %d:%02d", recipe.getServings(), recipe.getPreparationTime()/60, recipe.getPreparationTime() % 60);
		newLine(out);
		List<String> categoryNames = new ArrayList<String>();
		for(Category c : recipe.getCategories()) {
			String name = c.getName().replace(' ', '_');
			categoryNames.add(name);
		}
		out.print("Categories    : ");
		for (int i = 0; i < categoryNames.size(); i++) {
			// start of a new line?
			if (i!=0 && i%2==0) {
				newLine(out);
				out.printf("                ");
			}
			out.printf("%-31s", categoryNames.get(i));
		} 
		newLine(out);  // end of categories
		newLine(out);// empty line
		out.print("  Amount  Measure       Ingredient -- Preparation Method");
		newLine(out);
		out.print("--------  ------------  --------------------------------");
		newLine(out);
		for (RecipeIngredient ingr : recipe.getIngredients()) {
			if (ingr.getUnit()==null && ingr.getAmount()<0.001) {
				out.print("          ");
			} else {
				// MasterCook need numberes to be formated with a '.' not a ',' 
				out.printf(Locale.US, "%8.2f  ", ingr.getAmount());
			}
			if (ingr.hasNoUnit()) {
				out.print("              ");
			} else {
				out.printf("%12s  ", ingr.getPluralisedUnitName());
			}
			if (ingr.getIngredient()!=null) {
				out.print(ingr.getIngredient().getName());
			}
			if (ingr.getProcessing()!=null && ingr.getProcessing().length()>0) {
				out.print(" -- " + ingr.getProcessing());
			}
			newLine(out);
		}
		newLine(out);
		out.print(recipe.getDirectionsAsString());
		newLine(out);
		newLine(out);
		if (recipe.getDescription()!=null && recipe.getDescription().length() > 0) {
			out.print("Description:");
			newLine(out);
			out.print("  \""+recipe.getSource()+"\"");
			newLine(out);
		}
		if (recipe.getSource()!=null && recipe.getSource().length() > 0) {
			out.print("Source:");
			newLine(out);
			out.print("  \""+recipe.getSource()+"\"");
			newLine(out);
		}
		for (String attName : recipe.getTextAttributes().keySet()) {
			out.printf("S(\"%s\")", attName);
			newLine(out);
			out.printf("  \"%s\"", recipe.getTextAttributes().get(attName));
			newLine(out);
		}
		if (recipe.getCopyright()!=null && recipe.getCopyright().length() > 0) {
			out.print("Copyright:");
			newLine(out);
			out.print("  \""+recipe.getCopyright()+"\"");
			newLine(out);
		}
		if (recipe.getYield()!=null && recipe.getYield().length() > 0) {
			out.print("Yield:");
			newLine(out);
			out.printf("  \"%s %s\"", (Object[]) recipe.splitYield());
			newLine(out);
		}
		if (recipe.getTotalTime() != 0) {
			out.print("Start to Finish Time:");
			newLine(out);
			out.printf("  \"%d:%0d\"", recipe.getTotalTime()/60, recipe.getTotalTime()%60);
			newLine(out);
		}
		// check if any other times.
		// Since MC only supports one time, give priority to Cooking time
		if (recipe.getCookTime() != 0) {
			out.printf("T(Cook):");
			newLine(out);
			out.printf("  \"%d:%02d\"", recipe.getCookTime()/60, recipe.getCookTime()%60);
			newLine(out);
		} else {
			for(String key : recipe.getTimes().keySet()) {
				if (! key.startsWith("TIME.")) {
					out.printf("T(%s):", key);
					newLine(out);
					out.printf("  \"%d:%02d\"", recipe.getTime(key)/60, recipe.getTime(key)%60);
					newLine(out);
					break;
				}
			}
		}
		out.println("- - - - - - - - - - - - - - - - - - -");
		if (recipe.getWine()!=null && recipe.getWine().length() > 0) {
			out.printf("Suggested Wine: %s", recipe.getWine());
			newLine(out);
			newLine(out);
		}
		
		if (recipe.getServingIdeas()!=null && recipe.getServingIdeas().length() > 0) {
			out.printf("Serving Ideas : %s", recipe.getServingIdeas());
			newLine(out);
			newLine(out);
		}

		if (recipe.getNote()!=null && recipe.getNote().length() > 0) {
			out.printf("NOTES : %s", recipe.getNote());
			newLine(out);
		}
		
		newLine(out);
	}
	
	/**
	 * The import in MasterCook only work correct if the lines are separated by \r (0x0a)
	 * 
	 * @param out the printwriter to write to
	 */
	void newLine(PrintWriter out) {
		out.print('\r');
	}
	
	/**
	 * @return a pattern to recognize this type of recipes.
	 */
    @Override
	public Pattern getRecognizePattern() {
		return recipeSeparatorPattern;
	}

}
