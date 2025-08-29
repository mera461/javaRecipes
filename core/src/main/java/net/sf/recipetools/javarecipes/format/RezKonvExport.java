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

import net.sf.recipetools.javarecipes.model.Category;
import net.sf.recipetools.javarecipes.model.Ingredient;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;
import net.sf.recipetools.javarecipes.model.RecipeIngredient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author ft
 *
 */
public class RezKonvExport extends RecipeTextFormatter {
	private static final Logger log = LoggerFactory.getLogger(RezKonvExport.class);
	
	private static Pattern recipeSeparatorPattern = Pattern.compile("^\\s*={5,}.*REZKONV", Pattern.CASE_INSENSITIVE);
	private static Pattern recipeEndPattern = Pattern.compile("^\\s*={5}\\s*$");
	private static Pattern titlePattern = Pattern.compile("Titel\\s*:\\s*(.*)$", Pattern.CASE_INSENSITIVE);
	private static Pattern categoriesPattern = Pattern.compile("Kategorien\\s*:\\s*(.*)$", 
			Pattern.CASE_INSENSITIVE);
	private static Pattern yieldPattern = Pattern.compile("Menge\\s*:\\s*(.*)$", 
			Pattern.CASE_INSENSITIVE);
	private static Pattern servingPattern = Pattern.compile("([\\d\\s\\-]*)\\s+person(?:en)?$", 
			Pattern.CASE_INSENSITIVE);

	private static Pattern ingredientHeaderPattern = Pattern.compile("^={5,}\\s*(.*?)\\s*={5,}\\s*$");
	private static Pattern sourcePattern = Pattern.compile("^\\s*(?:QUELLE|ERFASST.*\\s*VON)\\s*$");

	/**
	 * 
	 */
	public RezKonvExport() {
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
			
			// get the source
			readSource(in, recipe);

			// read the ingredients
			readDirections(in, recipe);
			
			extractExtraFieldsFromDirections(recipe);

		} catch (RuntimeException e) {
			throw new RecipeFoxException("*** ERROR in title: "+recipe.getTitle(), e);
		}
		
		return Arrays.asList(recipe);
	}

	public void extractExtraFieldsFromDirections(Recipe recipe) {
		String txt = recipe.getDirectionsAsString();
		if (txt==null || txt.length()==0) {
			return;
		}
		
	}

	private void readHeader(LineNumberReader in, Recipe recipe) {
		String line = "";
		try {
			// skip empty line
			skipEmptyLines(in);
			
			// get the title
			line = in.readLine();
			String[] matches = getMatchesOrThrow(line, titlePattern, "Title");
			if (matches[0]!=null) {
				recipe.setTitle(matches[0].trim());
			}
			
			// get the categories 
			line = in.readLine();
			matches = getMatchesOrThrow(line, categoriesPattern, "Categories");
			for (String name: matches[0].split("\\s*,\\s*")) {
				recipe.getCategories().add(new Category(name));
			}

			// get the Yield 
			line = in.readLine();
			matches = getMatchesOrThrow(line, yieldPattern, "Serving Size");
			if (matches[0]!=null) {
				// ignore the obvious yield: 1 Rezept
				if (! matches[0].equals("1 Rezept")) {
					recipe.setYield(matches[0]);
				}
				// set the servings if "2-3 personen"
				Matcher m = servingPattern.matcher(matches[0]);
				if (m.find()) {
					recipe.setServings(m.group(1));
				}
			}
		} catch (IOException e) {
			String msg = "Line number="+in.getLineNumber()+". RezKonvExport: error reading header from line=" + line; 
			log.info(msg);
			throw new RecipeFoxException(msg);
		}
		
	}
	
	private void readSource(LineNumberReader in, Recipe recipe) {
		String line = "";
		try {
			// skip empty line
			skipEmptyLines(in);

			// get the Source header
			in.mark(5000);
			line = in.readLine();
			if (line==null) return;
			
			// return if no header.
			Matcher m = ingredientHeaderPattern.matcher(line);
			if (! m.find()) {
				in.reset();
				return;
			}
			String text = m.group(1);

			// if not a Source header, then break
			m = sourcePattern.matcher(text);
			if (! m.find()) {
				in.reset();
				return;
			}
			
			// read until empty line
			// get the next line
			StringBuilder src = new StringBuilder();
			for (line = in.readLine(); line != null; in.mark(5000), line = in.readLine()) {
				// end of recipe ?
				if (line==null || line.length()==0) {
					break;
				} else if (recipeEndPattern.matcher(line).find()) {
					// put the line back
					in.reset();
					break;
				} else {
					// ignore lines like: Erfasst *RK* 27.06.2007 von
					if (! line.contains("Erfasst *RK*")) {
						src.append(line.trim().replaceAll("^\\s*-*", ""));
					}
				}
			}
			recipe.setSource(src.toString());
		} catch (IOException e) {
			String msg = "Line number="+in.getLineNumber()+". RezKonvExport: error reading header from line=" + line; 
			log.info(msg);
			throw new RecipeFoxException(msg);
		}
		
	}

	private void readIngredients(LineNumberReader in, Recipe recipe) {
		String line = "";
		try {

			// get the next line
			for (line = in.readLine(); line != null; in.mark(5000), line = in.readLine()) {
				// end of recipe ?
				if (recipeSeparatorPattern.matcher(line).find()) {
					// put the line back
					in.reset();
					return;
				}
				
				// ignore empty lines
				if (line.trim().length() < 2) {
					continue;
				}
				

				
				// linewrapped from previous line ?
/*
				if (line.length()<24) {
					// the first ingredient line ..... strange ???
					if (recipe.getIngredients().size()==0) {
						RecipeIngredient ingr = new RecipeIngredient(0f, null, new Ingredient(line.trim()));
						recipe.addIngredient(ingr);
					} else {
						RecipeIngredient ingr = recipe.getIngredients().get(recipe.getIngredients().size()-1);
						if (ingr.getProcessing() != null) {
							ingr.setProcessing(join(ingr.getProcessing(), ";", line.trim()));
						} else {
							ingr.getIngredient().setName(join(ingr.getIngredient().getName(), ";", line.trim()));
						}
					}
					break;
				}
*/
				
				// ingredient section (header) ?
				Matcher m = ingredientHeaderPattern.matcher(line);
				if (m.find()) {
					String text = m.group(1);

					// Source, then break
					if (sourcePattern.matcher(text).find()) {
						in.reset();
						break;
					}
					
					RecipeIngredient ingredient = new RecipeIngredient(); 
					ingredient.setIngredient(new Ingredient(text.trim()));
					ingredient.setType(RecipeIngredient.TYPE_SUBTITLE);
					recipe.addIngredient(ingredient);
					continue;
				}
				
				// First part of the line.
				RecipeIngredient ingredient = RecipeIngredient.createFromFixedPositionString(line, 0, 7, 10, 80);
				recipe.addIngredient(ingredient);
			}
		} catch (IOException e) {
			String msg = "RezKonvExport: error reading ingredients - " + line; 
			log.info(msg);
			throw new RecipeFoxException(msg);
		} finally {
		}
	}
	
	private void readDirections(LineNumberReader in, Recipe recipe) {
		skipEmptyLines(in);
		
		String line = "";
		StringBuilder directions = new StringBuilder();
		try {
			// get the next line
			for (line = in.readLine(); line != null; in.mark(5000), line = in.readLine()) {
				// end of recipe ?
				if (recipeEndPattern.matcher(line).find()
					|| recipeSeparatorPattern.matcher(line).find()) {
					// put the line back
					in.reset();
					break;
				}

				// directions
				if (directions.length() > 0) { 
					directions.append(linebreak);
				}
				directions.append(line.trim());
			}
			recipe.setDirections(directions.toString().replaceAll("\\r", ""));
		} catch (IOException e) {
			String msg = "RezKonvExport: error reading directions - " + line; 
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
		throw new RecipeFoxException("Writing in RezKonv export format is not yet supported");
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
		return Pattern.compile("^\\s*={5,}.*REZKONV", Pattern.CASE_INSENSITIVE+Pattern.MULTILINE);
	}

}
