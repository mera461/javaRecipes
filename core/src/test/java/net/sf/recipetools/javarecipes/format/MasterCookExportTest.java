/*
 * Created on 26-10-2004
 *
 */
package net.sf.recipetools.javarecipes.format;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.List;

import junit.framework.TestCase;
import net.sf.recipetools.javarecipes.model.Configuration;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;

/**
 * @author ft
 *
 */
public class MasterCookExportTest extends TestCase {

	/*
	 * @see TestCase#setUp()
	 */
	private MasterCookExport mc = new MasterCookExport(); 
	protected void setUp() throws Exception {
		super.setUp();
		Configuration.setBooleanProperty("PLURALISE_UNITS", false);
		Configuration.setIntProperty("TITLE_CASE_PREFERENCE", 1);
		Configuration.setBooleanProperty("MERGE_INGREDIENT_LINE_CONTINUATIONS", true);
		Configuration.setBooleanProperty("SPLIT_INGREDIENT_LINES_WITH_PLUS", true);
		Configuration.setBooleanProperty("DETECT_AND_MARK_SERVINGS_FROM_YIELD", true);
		Configuration.setBooleanProperty("MOVE_NOTES_IN_DIRECTIONS_TO_NOTES", true);
		Configuration.setBooleanProperty("REMOVE_NON_RECIPE_COMMENTS_FROM_DIRECTIONS", true);
		Configuration.setBooleanProperty("REMOVE_NUTRITION_INFORMATION_FROM_DIRECTIONS", false);
		Configuration.setBooleanProperty("DETECT_AND_MARK_SERVINGS_FROM_DIRECTIONS", true);
		Configuration.setBooleanProperty("EXPAND_UNIT_ABBREVIATIONS", true);
		Configuration.setBooleanProperty("CALCULATE_WEIGHT_FROM_AMOUNT_TIMES_PACKAGE_SIZE", true);
		Configuration.setBooleanProperty("USE_WEIGHT_NOT_PACKAGE_SIZE", true);
		Configuration.setBooleanProperty("INGREDIENT_PROCESSING_WORDS_TO_PREPARATION", true);
		Configuration.setBooleanProperty("DETECT_AND_MARK_SUBTITLES", true);
		Configuration.setBooleanProperty("PLURALISE_UNITS", true);

	
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		mc = null;
	}

	public void testReadRecipe1() {
		LineNumberReader bufReader = null;
		try {
			bufReader = new LineNumberReader(new FileReader("src/test/data/a_wok_in_the_pork.txt"));
		} catch (FileNotFoundException e) {
            throw new RecipeFoxException(e);
		}

		List<Recipe> recipes = null;
		recipes = mc.readRecipes(bufReader);
		assertNotNull(recipes);
		assertEquals(1, recipes.size());
		assertEquals("A_Wok_in_the_Pork", recipes.get(0).getTitle());
		assertEquals("Lowfat, Low Calorie Savory", recipes.get(0).getCategories().get(0).getName());
		assertEquals("Janet and Greta Podleski", recipes.get(0).getAuthor());
		assertEquals("Crazy Plates- ISBN # 0968063128", recipes.get(0).getSource());
		assertEquals(4, recipes.get(0).getServings());
		assertEquals(15, recipes.get(0).getIngredients().size());
		assertTrue(recipes.get(0).getDirectionsAsString().contains("1. Heat olive oil"));
		assertTrue(recipes.get(0).getNote().contains("Loonyspoons/Crazy Plates"));
	}
	
	public void testCategories() {
		LineNumberReader bufReader = null;
		try {
			bufReader = new LineNumberReader(new FileReader("src/test/data/a_wok_in_the_pork.txt"));
		} catch (FileNotFoundException e) {
            throw new RecipeFoxException(e);
		}

		List<Recipe> recipes = null;
		recipes = mc.readRecipes(bufReader);
		recipes = mc.readRecipes(bufReader);
		assertNotNull(recipes);
		Recipe recipe = recipes.get(0);
		assertEquals("Almond Biscotti #3", recipe.getTitle());
		assertEquals(4, recipe.getCategories().size());
		assertEquals("06/99", recipe.getCategories().get(0).getName());
		assertEquals("Cookies & Bars", recipe.getCategories().get(1).getName());
		assertEquals("Eat-Lf Mailing List", recipe.getCategories().get(2).getName());
		assertEquals("Vegetarian", recipe.getCategories().get(3).getName());
		assertEquals(1, recipe.getServings());
		assertEquals(9, recipe.getIngredients().size());
		
	}

 	public void testReadRecipeCount() {
		LineNumberReader bufReader = null;
		try {
			bufReader = new LineNumberReader(new FileReader("src/test/data/AllAppetizerRecipes.txt"));
		} catch (FileNotFoundException e) {
            throw new RecipeFoxException(e);
		}
		
		
		int count = 0;
		long start = System.currentTimeMillis();
		List<Recipe> recipes = mc.readRecipes(bufReader);
		while (recipes != null) {
			count++;
			recipes = mc.readRecipes(bufReader);
		}
		assertEquals(103, count);
		long time = System.currentTimeMillis() - start;
		System.out.println("Read 103 recipes in "+(time/1000.0)+ " seconds");
}

 	public void testOutput() {
		LineNumberReader bufReader = null;
		try {
			bufReader = new LineNumberReader(new FileReader("src/test/data/a_wok_in_the_pork.txt"));
		} catch (FileNotFoundException e) {
            throw new RecipeFoxException(e);
		}

		List<Recipe> recipes = null;
		recipes = mc.readRecipes(bufReader);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PrintWriter out = new PrintWriter(bos);
		Configuration.setBooleanProperty("PLURALISE_UNITS", false);
		mc.writeRecipe(out, recipes);
		out.close();
		
		System.out.print(bos.toString());
//		return;
		
		bufReader = new LineNumberReader(new CharArrayReader(bos.toString().toCharArray()));
		Recipe recipe1 = mc.readRecipes(bufReader).get(0);
		
		// TODO: compare recipe and recipe1
		Recipe recipe = recipes.get(0);
		assertEquals(recipe.getAuthor(), recipe1.getAuthor());
//		assertEquals(recipe.getDirectionsAsString(), recipe1.getDirectionsAsString());
		assertEquals(recipe.getNote(), recipe1.getNote());
		assertEquals(recipe.getPreparationTime(), recipe1.getPreparationTime());
		assertEquals(recipe.getServings(), recipe1.getServings());
		assertEquals(recipe.getSource(), recipe1.getSource());
		assertEquals(recipe.getTitle(), recipe1.getTitle());
		assertEquals(recipe.getYield(), recipe1.getYield());
		assertEquals(recipe.getIngredients().size(), recipe1.getIngredients().size());

		for (int i=0; i<recipe.getIngredients().size(); i++) {
			assertEquals(recipe.getIngredients().get(i).getAmount(),
						 recipe1.getIngredients().get(i).getAmount(),0.01);
			if (recipe.getIngredients().get(i).getUnit() == null) {
				assertEquals(recipe.getIngredients().get(i).getUnit(),
						recipe1.getIngredients().get(i).getUnit());
			} else {
				assertEquals(recipe.getIngredients().get(i).getUnit().getName(),
						recipe1.getIngredients().get(i).getUnit().getName());
			}
			assertEquals(recipe.getIngredients().get(i).getIngredient().getName(),
					 recipe1.getIngredients().get(i).getIngredient().getName());
			assertEquals(recipe.getIngredients().get(i).getProcessing(),
					 recipe1.getIngredients().get(i).getProcessing());
		}
		
 	}
 	
 	public void testExtraFields() {
		LineNumberReader bufReader = null;
		try {
			bufReader = new LineNumberReader(new FileReader("src/test/data/AllFields.mxp"));
		} catch (FileNotFoundException e) {
            throw new RecipeFoxException(e);
		}

		List<Recipe> recipes = null;
		recipes = mc.readRecipes(bufReader);
		assertNotNull(recipes);
		Recipe recipe = recipes.get(0);
		assertEquals("DescriptionField", recipe.getDescription());
		assertEquals("SourceField", recipe.getSource());
		assertEquals("Cuisine", recipe.getCuisine());
		assertEquals("AltSourceFIeld", recipe.getUrl());
		assertEquals("CopyrightField", recipe.getCopyright());
		assertEquals("222 Yieldunit", recipe.getYield());
		assertEquals(78, recipe.getTotalTime());
		assertEquals(79, recipe.getTime("baking time"));
		assertEquals("WineField", recipe.getWine());
		assertEquals(8, recipe.getRatings().size());
		assertEquals(0.3f, recipe.getRating("Cholesterol Rating"), 0.001);
		assertEquals(3, recipe.getRating("Cholesterol Rating", 10));
		assertTrue(recipe.getServingIdeas().contains("ServingIdeasFIeld"));
		assertTrue(recipe.getServingIdeas().contains("SI line 4"));
		assertTrue(recipe.getNote().contains("NotesField"));
		assertTrue(recipe.getNote().contains("NF line 5"));
		assertTrue(! recipe.getDirectionsAsString().contains("Nutr. Assoc"));
		assertTrue(! recipe.getDirectionsAsString().contains("- - - - -"));
		assertTrue(! recipe.getDirectionsAsString().contains("Per servings"));
		assertTrue(recipe.getDirectionsAsString().endsWith("Direction-2"));
 		
 	}
 	
 	public void testFractions() {
		Configuration.setBooleanProperty("USE_FRACTIONS_IN_AMOUNTS", true);
		Recipe r = new Recipe("title");
		r.setIngredients("0.25 dl water");
		
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PrintWriter out = new PrintWriter(bos);
		mc.writeRecipe(out, r);
		out.close();
		String str = bos.toString();
		
		assertTrue(str.contains("1/4"));

		// AS DECIMALS:
		Configuration.setBooleanProperty("USE_FRACTIONS_IN_AMOUNTS", false);
		out = new PrintWriter(bos);
		mc.writeRecipe(out, r);
		out.close();
		str = bos.toString();
		assertTrue(str.contains("0.25"));
 	}
}
