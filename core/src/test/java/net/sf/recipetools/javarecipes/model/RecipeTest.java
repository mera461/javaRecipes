/**
 * 
 */
package net.sf.recipetools.javarecipes.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;


/**
 * @author ft
 *
 */
public class RecipeTest {

	@Before
	public void setUp() {
		Configuration.setIntProperty("TITLE_CASE_PREFERENCE", 1);
		Configuration.setIntProperty("SUBTITLE_CASE_PREFERENCE", 1);
		Configuration.setBooleanProperty("PLURALISE_UNITS", false);
		Configuration.setBooleanProperty("MERGE_INGREDIENT_LINE_CONTINUATIONS", true);
		Configuration.setBooleanProperty("SPLIT_INGREDIENT_LINES_WITH_PLUS", true);
		Configuration.setBooleanProperty("MARK_ALTERNATE_INGREDIENT_LINES_AS_TEXT", true);
		Configuration.setBooleanProperty("DETECT_AND_MARK_SERVINGS_FROM_YIELD", true);
		Configuration.setBooleanProperty("MOVE_NOTES_IN_DIRECTIONS_TO_NOTES", true);
		Configuration.setBooleanProperty("REMOVE_NON_RECIPE_COMMENTS_FROM_DIRECTIONS", true);
		Configuration.setBooleanProperty("REMOVE_NUTRITION_INFORMATION_FROM_DIRECTIONS", false);
		Configuration.setBooleanProperty("DETECT_AND_MARK_SERVINGS_FROM_DIRECTIONS", true);
		Configuration.setBooleanProperty("EXPAND_UNIT_ABBREVIATIONS", true);
		Configuration.setBooleanProperty("CALCULATE_WEIGHT_FROM_AMOUNT_TIMES_PACKAGE_SIZE", true);
		Configuration.setBooleanProperty("USE_WEIGHT_NOT_PACKAGE_SIZE", true);
		Configuration.setBooleanProperty("INGREDIENT_PROCESSING_WORDS_TO_PREPARATION", true);
		Configuration.setBooleanProperty("PLURALISE_UNITS", true);
		Configuration.setBooleanProperty("MOVE_TEXT_AFTER_COMMAS_TO_PREPARATION", true);
		Configuration.setBooleanProperty("MOVE_TEXT_AFTER_SEMICOLON_TO_PREPARATION", true);
		Configuration.setBooleanProperty("USE_FRACTIONS_IN_AMOUNTS", true);
		Configuration.loadSafeCommaStructuresFromFile("commas.txt");
	
	}
	
	/**
	 * Test method for {@link net.sf.recipetools.javarecipes.model.Recipe#directionsMoveDateAndFrom()}.
	 */
	@Test
	public void testDirectionsMoveDateAndFrom() {
	}

	@Test
	public void testDirectionsRemoveConvertedBy() {
		String[] testStrings = new String[] {
			"abc  Converted by MMCONV vers. 1.00  def",
			"abc Converted by MM_Buster v2.0l. def",
//			"abc Converted by MM_Buster v2.0l. Converted by MC_Buster. def",
			"abc converted by MC_Buster. def",
			"abc Converted by MC_Buster. def",
			"abc def",
		};
		Recipe recipe = new Recipe();
		for (int i = 0; i < testStrings.length; i++) {
			recipe.setDirections(testStrings[i]);
			recipe.directionsRemoveConvertedBy();
			assertEquals("string nr "+i, "abc def", recipe.getDirectionsAsString());
		}
	}
	
	@Test
	public void testTitleCasing() {
		assertEquals("Test", Recipe.toTitleCase(" test "));
		assertEquals("This Is a Test", Recipe.toTitleCase(" this is a test "));
		assertEquals("This and or the of for with a Test", Recipe.toTitleCase(" this  and  or  the  of  for  with a test"));
		assertEquals("This Is a Test", Recipe.toTitleCase(" THIS  IS  A TEST "));
		assertEquals("Fruit-and-Nut Breakfast Bars", Recipe.toTitleCase(" FRUIT-AND-NUT BreakFast BARS"));
	}

	@Test
	public void testNormalizeIngredientLines() {
		Unit unit = new Unit("kg");
		Recipe recipe = new Recipe();
		recipe.addIngredient(new RecipeIngredient(1.0f, unit, new Ingredient("ingredient 1"), "processing 1"));
		recipe.addIngredient(new RecipeIngredient(2.0f, unit, new Ingredient("-ingredient 2"), "processing 2"));
		recipe.normalizeIngredientLinesWithContinuations();
		assertEquals(2, recipe.getIngredients().size());

		recipe = new Recipe();
		recipe.addIngredient(new RecipeIngredient(1.0f, unit, new Ingredient("ingredient 1"), "processing 1"));
		recipe.addIngredient(new RecipeIngredient(0.0f, unit, new Ingredient("-ingredient 2"), "processing 2"));
		recipe.normalizeIngredientLinesWithContinuations();
		assertEquals(2, recipe.getIngredients().size());

		recipe = new Recipe();
		recipe.addIngredient(new RecipeIngredient(1.0f, unit, new Ingredient("ingredient 1"), "processing 1"));
		recipe.addIngredient(new RecipeIngredient(0.0f, null, new Ingredient("-ingredient 2"), "processing 2"));
		recipe.normalizeIngredientLinesWithContinuations();
		assertEquals(1, recipe.getIngredients().size());
		assertEquals(1.0f, recipe.getIngredients().get(0).getAmount(), 0.01);
		assertEquals("kg", recipe.getIngredients().get(0).getUnit().getName());
		assertEquals("ingredient 1 ingredient 2", recipe.getIngredients().get(0).getIngredient().getName());
		assertEquals("processing 1 processing 2", recipe.getIngredients().get(0).getProcessing());
		
	}
	
	@Test
	public void testNormalizeWithEmptyLines() {
		// test with empty lines
		Recipe recipe = new Recipe();
		recipe.addIngredient("1 tsp salt");
		recipe.addIngredient(new RecipeIngredient(0.0f, (Unit)null, null, null));
		recipe.addIngredient("2 tsp water");
		recipe.normalize();
		assertEquals(3, recipe.getIngredients().size());
		
		// more empty lines
		recipe = new Recipe();
		recipe.addIngredient("2 tablespoons triple sec or other orange liqueur");
		recipe.addIngredient("");
		recipe.addIngredient("¼ cup finely chopped mint leaves");
		recipe.normalize();
		assertEquals(3, recipe.getIngredients().size());
		
		// empty lines in the start and end
		recipe = new Recipe();
		recipe.addIngredient("");
		recipe.addIngredient("2 tablespoons triple sec or other orange liqueur");
		recipe.addIngredient("");
		recipe.addIngredient("¼ cup finely chopped mint leaves");
		recipe.addIngredient("");
		recipe.normalize();
		assertEquals(5, recipe.getIngredients().size());
		
		
	}
	
	@Test
	public void testDirectionsExtractNotes() {
	}

	@Test
	public void testDirectionsDeleteNutritionalInfo() {
	}
	
	@Test
	public void testDirectionsRemoveDoubleColons() {
		Recipe recipe = new Recipe();
		recipe.setDirections("start by ...\n\nFor the cake::\n\nDo something more");
		recipe.directionsRemoveDoubleColons();
		assertEquals("For the cake:", recipe.getDirections().get(1));
	}
	
	@Test
	public void testDirectionsExtractServings() {
		assertEquals(4, extractServings("pepper.  4 servings.  560 calories."));
		assertEquals(8, extractServings("brown slightly.  Serves 8."));
		assertEquals(11, extractServings("Serves 10 to 12."));
		assertEquals(5, extractServings("Serves at least 4 to 6. "));
		assertEquals(9, extractServings(" 8 to 10 servings."));
		assertEquals(9, extractServings("Serves 8-10 people."));
		assertEquals(5, extractServings("4-6 servings."));
		assertEquals(5, extractServings("Makes 4 to 6 servings."));
		assertEquals(9, extractServings("Yields: 8 to 10 servings."));
		assertEquals(6, extractServings("Each casserole serves 6."));
		assertEquals(4, extractServings("Serves about 4.		"));
	}

	@Test
	public void testSetPreparationTime() {
		Recipe recipe = new Recipe();
		recipe.setPreparationTime("1:14");
		assertEquals(74, recipe.getPreparationTime());
		recipe.setPreparationTime("preptime: 1:14 ");
		assertEquals(74, recipe.getPreparationTime());
		recipe.setPreparationTime("prept: 24 min ");
		assertEquals(24, recipe.getPreparationTime());
		recipe.setPreparationTime("24 min ");
		assertEquals(24, recipe.getPreparationTime());
		recipe.setPreparationTime("1 hour, 41 minutes ");
		assertEquals(101, recipe.getPreparationTime());
		recipe.setPreparationTime("1 1/2 hours ");
		assertEquals(90, recipe.getPreparationTime());
        recipe.setPreparationTime("1 day 2 hours 17 minutes ");
        assertEquals(1577, recipe.getPreparationTime());
		// side effect or bug
		recipe.setPreparationTime("25 to 35 minutes, Cooking time: 25 minutes");
		assertEquals(60, recipe.getPreparationTime());
	}
	
	public int extractServings(String str) {
		Recipe recipe = new Recipe();
		recipe.setDirections(str);
		recipe.extractServings(recipe.getDirectionsAsString());
		return recipe.getServings();
	}
	
	@Test
	public void testSetServings() {
		Recipe recipe = new Recipe();
		recipe.setServings("14");
		assertEquals(14, recipe.getServings());
		recipe.setServings("15 servings");
		assertEquals(15, recipe.getServings());
		recipe.setServings("16-18");
		assertEquals(17, recipe.getServings());
		recipe.setServings("18-20 servings");
		assertEquals(19, recipe.getServings());
	}

	@Test
	public void testSetYield() {
		Recipe recipe = new Recipe();
		recipe.setYield("make 12 cakes");
		assertEquals("12 cakes", recipe.getYield());
		recipe.setYield("makes: 12 cakes");
		assertEquals("12 cakes", recipe.getYield());
		recipe.setYield("Make 6 servings (about 1/2 cup each)");
		assertEquals("6 servings (about 1/2 cup each)", recipe.getYield());
		
		/*** TODO: Test when splitting it 
		recipe.setYield("7");
		assertEquals("7", recipe.getYield());
		recipe.setYield("7 3/4");
		assertEquals("7 3/4", recipe.getYield());
		recipe.setYield("7 3/10 cups");
		assertEquals(7.3, recipe.getYield(), 0.01);
		assertEquals("cups", recipe.getYieldUnit());
		recipe.setYield("8.5 oz");
		assertEquals(8.5, recipe.getYield(), 0.01);
		assertEquals("oz", recipe.getYieldUnit());
		recipe.setYield("  9.5  oz  ");
		assertEquals(9.5, recipe.getYield(), 0.01);
		assertEquals("oz", recipe.getYieldUnit());
		recipe.setYield("  12 7-inch pitas.  ");
		assertEquals(12, recipe.getYield(), 0.01);
		assertEquals("7-inch pitas.", recipe.getYieldUnit());
		recipe.setYield("Make 6 servings (about 1/2 cup each)");
		assertEquals(6f, recipe.getYield(), 0.01);
		assertEquals("servings (about 1/2 cup each)", recipe.getYieldUnit());
		//
		recipe.setYield("Makes: 3 dozen");
		assertEquals(3, recipe.getYield(), 0.01);
		assertEquals("dozen", recipe.getYieldUnit());
		recipe.setYield("Makes 15 servings");
		assertEquals(15, recipe.getYield(), 0.01);
		assertEquals("servings", recipe.getYieldUnit());
		recipe.setYield("Makes 6 to 8 cups");
		assertEquals(6, recipe.getYield(), 0.01);
		assertEquals("to 8 cups", recipe.getYieldUnit());
*/		
/*		recipe.setYield("");
		assertEquals(0, recipe.getYield(), 0.01);
		assertEquals("", recipe.getYieldUnit());
*/	}	
	
	@Test
	public void testYieldSplitting() {
		Configuration.setBooleanProperty("USE_FRACTIONS_IN_AMOUNTS", false);

		Recipe recipe = new Recipe();
		String[] parts = null;
		recipe.setYield("make 12 cakes");
		parts = recipe.splitYield();
		assertEquals("12", parts[0]);
		assertEquals("cakes", parts[1]);

		recipe.setYield("makes: 12 cakes");
		parts = recipe.splitYield();
		assertEquals("12", parts[0]);
		assertEquals("cakes", parts[1]);

		recipe.setYield("Make 6 servings (about 1/2 cup each)");
		parts = recipe.splitYield();
		assertEquals("6", parts[0]);
		assertEquals("servings (about 1/2 cup each)", parts[1]);

		recipe.setYield("7");
		parts = recipe.splitYield();
		assertEquals("7", parts[0]);
		assertEquals("", parts[1]);

		recipe.setYield("7 3/4");
		parts = recipe.splitYield();
		assertEquals("7.75", parts[0]);
		assertEquals("", parts[1]);

		recipe.setYield("7 3/10 cups");
		parts = recipe.splitYield();
		assertEquals("7.3", parts[0]);
		assertEquals("cups", parts[1]);

		recipe.setYield("3/4 cup");
		parts = recipe.splitYield();
		assertEquals("0.75", parts[0]);
		assertEquals("cup", parts[1]);

		recipe.setYield("8.5 oz");
		parts = recipe.splitYield();
		assertEquals("8.5", parts[0]);
		assertEquals("oz", parts[1]);

		recipe.setYield("  9.5  oz  ");
		parts = recipe.splitYield();
		assertEquals("9.5", parts[0]);
		assertEquals("oz", parts[1]);
		
		recipe.setYield("  12 7-inch pitas.  ");
		parts = recipe.splitYield();
		assertEquals("12", parts[0]);
		assertEquals("7-inch pitas.", parts[1]);

		recipe.setYield("Make 6 servings (about 1/2 cup each)");
		parts = recipe.splitYield();
		assertEquals("6", parts[0]);
		assertEquals("servings (about 1/2 cup each)", parts[1]);
		//
		recipe.setYield("Makes: 3 dozen");
		parts = recipe.splitYield();
		assertEquals("3", parts[0]);
		assertEquals("dozen", parts[1]);

		recipe.setYield("Makes 15 servings");
		parts = recipe.splitYield();
		assertEquals("15", parts[0]);
		assertEquals("servings", parts[1]);

		recipe.setYield("Makes 6 to 8 cups");
		parts = recipe.splitYield();
		assertEquals("6", parts[0]);
		assertEquals("to 8 cups", parts[1]);
		Configuration.setBooleanProperty("USE_FRACTIONS_IN_AMOUNTS", true);

	}
	
	@Test
	public void testPlusIngredients() {
		Recipe recipe = new Recipe();
		List<RecipeIngredient> ingr = null;

		// delete old ingredients
		recipe.getIngredients().clear();
		recipe.addIngredient(new RecipeIngredient("12 pimiento-stuffed green olives, halved, plus 1 Tbsp. liquid from jar"));
		recipe.normalize();
		// test the result
		ingr = recipe.getIngredients();
		assertEquals(2, ingr.size());
		compareIngredient(ingr.get(0), 12f, "", "pimiento-stuffed green olives", "halved");
		compareIngredient(ingr.get(1), 1f, "tablespoon", "liquid from jar", "");
		
		// delete old ingredients
		recipe.getIngredients().clear();
		recipe.addIngredient(new RecipeIngredient("1 tablespoon plus 1 teaspoon dill seeds, crushed"));
		recipe.normalizeIngredientsWithPlus();
		// test the result
		ingr = recipe.getIngredients();
		assertEquals(2, ingr.size());
		compareIngredient(ingr.get(0), 1.0f, "tablespoon", "dill seeds, crushed", null);
		compareIngredient(ingr.get(1), 1.0f, "teaspoon", "dill seeds, crushed", null);

		// test with '+' sign
		recipe.getIngredients().clear();
		recipe.addIngredient(new RecipeIngredient("1 tablespoon + 1 teaspoon dill seeds, crushed"));
		recipe.normalizeIngredientsWithPlus();
		// test the result
		ingr = recipe.getIngredients();
		assertEquals(2, ingr.size());
		compareIngredient(ingr.get(0), 1.0f, "tablespoon", "dill seeds, crushed", null);
		compareIngredient(ingr.get(1), 1.0f, "teaspoon", "dill seeds, crushed", null);
		
		// test with 'plus' sign
		recipe.getIngredients().clear();
		recipe.addIngredient(new RecipeIngredient("1 tbsp. plus 1/4 tsp. salt, divided"));
		recipe.normalizeIngredientsWithPlus();
		// test the result
		ingr = recipe.getIngredients();
		assertEquals(2, ingr.size());
		compareIngredient(ingr.get(0), 1.0f, "tbsp.", "salt, divided", null);
		compareIngredient(ingr.get(1), 0.25f, "tsp", "salt, divided", null);

		// test with processing
		recipe.getIngredients().clear();
		recipe.addIngredient(new RecipeIngredient(1.0f, "tbsp.", "plus 1/4 tsp. salt", "divided"));
		recipe.normalizeIngredientsWithPlus();
		// test the result
		ingr = recipe.getIngredients();
		assertEquals(2, ingr.size());
		compareIngredient(ingr.get(0), 1.0f, "tbsp.", "salt", "divided");
		compareIngredient(ingr.get(1), 0.25f, "tsp", "salt", "divided");

		// delete old ingredients
		recipe.getIngredients().clear();
		recipe.addIngredient(new RecipeIngredient("8 tablespoon plus 2 1/2 teaspoon dill seeds, crushed"));
		recipe.normalizeIngredientsWithPlus();
		// test the result
		ingr = recipe.getIngredients();
		assertEquals(2, ingr.size());
		compareIngredient(ingr.get(0), 8.0f, "tablespoon", "dill seeds, crushed", null);
		compareIngredient(ingr.get(1), 2.5f, "teaspoon", "dill seeds, crushed", null);

		// delete old ingredients
		recipe.getIngredients().clear();
		recipe.addIngredient(new RecipeIngredient("2 cups. plus 3 tablespoons water, divided"));
		recipe.normalizeIngredientsWithPlus();
		// test the result
		ingr = recipe.getIngredients();
		assertEquals(2, ingr.size());
		compareIngredient(ingr.get(0), 2.0f, "cups.", "water, divided", null);
		compareIngredient(ingr.get(1), 3.0f, "tablespoons", "water, divided", null);
		
		 
		// delete old ingredients
		recipe.getIngredients().clear();
		recipe.addIngredient(new RecipeIngredient("1/4 cup plus 2 tablespoons hot mango chutney"));
		recipe.normalizeIngredientsWithPlus();
		// test the result
		ingr = recipe.getIngredients();
		assertEquals(2, ingr.size());
		compareIngredient(ingr.get(0), 0.25f, "cup", "hot mango chutney", null);
		compareIngredient(ingr.get(1), 2.0f, "tablespoons", "hot mango chutney", null);
		
		 		
		// delete old ingredients
		recipe.getIngredients().clear();
		recipe.addIngredient(new RecipeIngredient("1/4 cup plus 2 tablespoons finely chopped red bell pepper"));
		recipe.normalizeIngredientsWithPlus();
		// test the result
		ingr = recipe.getIngredients();
		assertEquals(2, ingr.size());
		compareIngredient(ingr.get(0), 0.25f, "cup", "finely chopped red bell pepper", null);
		compareIngredient(ingr.get(1), 2.0f, "tablespoons", "finely chopped red bell pepper", null);
		
		
		// delete old ingredients
		recipe.getIngredients().clear();
		recipe.addIngredient(new RecipeIngredient("1 cup plus 2 tbsp. dry sherry, divided"));
		recipe.normalizeIngredientsWithPlus();
		// test the result
		ingr = recipe.getIngredients();
		assertEquals(2, ingr.size());
		compareIngredient(ingr.get(0), 1f, "cup", "dry sherry, divided", null);
		compareIngredient(ingr.get(1), 2.0f, "tbsp", "dry sherry, divided", null);
		
		
		// test with normalizing everything

		// delete old ingredients
		recipe.getIngredients().clear();
		recipe.addIngredient(new RecipeIngredient("1/4 cup plus 2 tablespoons finely chopped red bell pepper"));
		recipe.normalize();
		// test the result
		ingr = recipe.getIngredients();
		assertEquals(2, ingr.size());
		compareIngredient(ingr.get(0), 0.25f, "cup", "red bell pepper", "finely chopped");
		compareIngredient(ingr.get(1), 2.0f, "tablespoon", "red bell pepper", "finely chopped");
		
		// delete old ingredients
		recipe.getIngredients().clear();
		recipe.addIngredient(new RecipeIngredient("1 cup water -- PLUS"));
		recipe.addIngredient(new RecipeIngredient("2 tbsp water"));
		recipe.normalize();
		// test the result
		ingr = recipe.getIngredients();
		assertEquals(2, ingr.size());
		compareIngredient(ingr.get(0), 1f, "cup", "water", "PLUS");
		compareIngredient(ingr.get(1), 2.0f, "tablespoon", "water", "");
		
		
		/*
		// delete old ingredients
		recipe.getIngredients().clear();
		recipe.addIngredient(new RecipeIngredient("2 eggs plus 3 egg whites, lightly beaten"));
		recipe.normalizeIngredientsWithPlus();
		// test the result
		ingr = recipe.getIngredients();
		assertEquals(2, ingr.size());
		compareIngredient(ingr.get(0), 2.0f, "", "eggs, lightly beaten", null);
		compareIngredient(ingr.get(1), 3f, "", "egg whites, lightly beaten", null);
		*/
		
		// delete old ingredients
		recipe.getIngredients().clear();
		recipe.addIngredient(new RecipeIngredient("2 tsp minced chipotle in adobo plus 1 tsp adobo sauce"));
		recipe.normalizeIngredientsWithPlus();
		// test the result
		ingr = recipe.getIngredients();
		assertEquals(2, ingr.size());
		compareIngredient(ingr.get(0), 2f, "tsp", "minced chipotle in adobo", null);
		compareIngredient(ingr.get(1), 1f, "tsp", "adobo sauce", null);

		// delete old ingredients
		recipe.getIngredients().clear();
		recipe.addIngredient(new RecipeIngredient("2 whole eggs, plus 2 tablespoons water"));
		recipe.normalizeIngredientsWithPlus();
		// test the result
		ingr = recipe.getIngredients();
		assertEquals(2, ingr.size());
		compareIngredient(ingr.get(0), 2f, "whole", "eggs", null);
		compareIngredient(ingr.get(1), 2f, "tablespoons", "water", null);

		/* OTHER CASES TO TEST:
		-- basic: two unit following each other
		3 cups plus 2 tablespoons flour, divided
		1/2 cup plus 2 tablespoons fine dry unseasoned breadcrumbs
		3 lb plus 11/4 cups packed light brown sugar
		-- two different ingr
		2 whole eggs, plus 2 tablespoons water
		2 tsp minced chipotle in adobo plus 1 tsp adobo sauce
		1/2 teaspoon kosher salt, plus 1/4 teaspoon kosher salt
		-- each
		1 tablespoon each dijon style mustard, catsup and Worcestershire sauce
		1/4 cup each packed brown sugar and sugar
		-- with alternative measures.
		1/3 cup plus 1 tablespoon Vegetable shortening (75 g)
		2 egg whites (about 1/4 cup) plus 4 egg whites (about 1/2 cup)
	    */
		
		
	}

	
	public void compareIngredient(RecipeIngredient ingr, String amount, String unit, String ingrName, String processing) {
		float value = 0;
		if (amount.length()>0) {
			value = Float.valueOf(amount);
		}
		compareIngredient(ingr, value, unit, ingrName, processing);
	}
	
	
	public void compareIngredient(RecipeIngredient ingr, float amount, String unit, String ingrName, String processing) {
		assertEquals(amount, ingr.getAmount(), 0.001);
		if (unit==null || unit.length()==0) {
			assertTrue(ingr.hasNoUnit());
		} else {
			assertEquals(unit, ingr.getUnit().getName());
		}
		assertEquals(ingrName, ingr.getIngredient().getName());
		assertEquals(processing, ingr.getProcessing());
	}
	
	@Test
	public void testSubtitles() {
		Configuration.setBooleanProperty("DETECT_AND_MARK_SUBTITLES", true);
		
		Recipe recipe = new Recipe();
		recipe.addIngredient(new RecipeIngredient("filling:"));
		// test with no change=0
		Configuration.setIntProperty("SUBTITLE_CASE_PREFERENCE", 0);
		recipe.normalize();
		RecipeIngredient ingr = recipe.getIngredients().get(0);
		compareIngredient(ingr, 0.0f, "", "Filling:", null);
		// test with no change=1
		Configuration.setIntProperty("SUBTITLE_CASE_PREFERENCE", 1);
		recipe.normalize();
		ingr = recipe.getIngredients().get(0);
		compareIngredient(ingr, 0.0f, "", "Filling:", null);
		// test with no change=0
		Configuration.setIntProperty("SUBTITLE_CASE_PREFERENCE", 2);
		recipe.normalize();
		ingr = recipe.getIngredients().get(0);
		compareIngredient(ingr, 0.0f, "", "FILLING:", null);
	}

	@Test
	public void testExtraSpaceInDirection() {
		Recipe recipe = new Recipe();
		recipe.addIngredient(new RecipeIngredient("Filling:"));
		recipe.setDirections("Test\nTest1");
		recipe.normalize();
		assertEquals("Test\nTest1", recipe.getDirectionsAsString());
	}
	
	@Test
	public void testRemoveLineBreaks() {
		Recipe recipe = new Recipe();
		assertEquals("test1 test2", recipe.removeLineBreaks("test1\ntest2\n"));
		assertEquals("test1\n\ntest2 test3", recipe.removeLineBreaks("test1\n\ntest2\ntest3"));
		assertEquals("test1\ntest2", recipe.removeLineBreaks("test1;;\ntest2\n"));
		assertEquals("test1:\ntest2", recipe.removeLineBreaks("test1:\ntest2\n"));
		assertEquals("test1.\ntest2", recipe.removeLineBreaks("test1.\ntest2\n"));
	}

	@Test
	public void testExtractTime() {
		assertEquals(17, Recipe.extractTime("17"));
		assertEquals(65, Recipe.extractTime("1:05"));
		assertEquals(65, Recipe.extractTime("1:5"));
		assertEquals(74, Recipe.extractTime("preptime: 1:14 "));
		assertEquals(24, Recipe.extractTime("prept: 24 min "));
		assertEquals(24, Recipe.extractTime("24 min "));
		assertEquals(120, Recipe.extractTime("2 hours"));
		assertEquals(130, Recipe.extractTime("2 hours 10 minutes"));
		assertEquals(130, Recipe.extractTime("2 hr 10 min"));
		assertEquals(36*60, Recipe.extractTime("P1DT12H"));
		assertEquals(105, Recipe.extractTime("PT1H45M12S"));
		assertEquals(30, Recipe.extractTime("PT0.5H"));
	}

	@Test
	public void testLinesEndingWithOr() {
		Recipe recipe = new Recipe();
		// a simple case with or
		recipe.getIngredients().clear();
		recipe.addIngredient(new RecipeIngredient("8 tablespoon salt or"));
		recipe.addIngredient(new RecipeIngredient("4 tablespoon peber"));
		recipe.normalizeIngredientsWithOr();
		assertEquals(RecipeIngredient.TYPE_INGREDIENT, recipe.getIngredients().get(0).getType());
		assertEquals(RecipeIngredient.TYPE_TEXT, recipe.getIngredients().get(1).getType());

		// two lines with or
		recipe.getIngredients().clear();
		recipe.addIngredient(new RecipeIngredient("8 tablespoon salt or"));
		recipe.addIngredient(new RecipeIngredient("4 tablespoon peber or"));
		recipe.addIngredient(new RecipeIngredient("2 tablespoon something else"));
		recipe.normalizeIngredientsWithOr();
		assertEquals(RecipeIngredient.TYPE_INGREDIENT, recipe.getIngredients().get(0).getType());
		assertEquals(RecipeIngredient.TYPE_TEXT, recipe.getIngredients().get(1).getType());
		assertEquals(RecipeIngredient.TYPE_TEXT, recipe.getIngredients().get(2).getType());
		
		// ending with =or=
		recipe.getIngredients().clear();
		recipe.addIngredient(new RecipeIngredient("8 tablespoon salt =or="));
		recipe.addIngredient(new RecipeIngredient("4 tablespoon peber"));
		recipe.normalizeIngredientsWithOr();
		assertEquals(RecipeIngredient.TYPE_INGREDIENT, recipe.getIngredients().get(0).getType());
		assertEquals(RecipeIngredient.TYPE_TEXT, recipe.getIngredients().get(1).getType());
		
		// ending with =or=
		recipe.getIngredients().clear();
		recipe.addIngredient(new RecipeIngredient("8 tablespoon salt -=or=-"));
		recipe.addIngredient(new RecipeIngredient("4 tablespoon peber"));
		recipe.normalizeIngredientsWithOr();
		assertEquals(RecipeIngredient.TYPE_INGREDIENT, recipe.getIngredients().get(0).getType());
		assertEquals(RecipeIngredient.TYPE_TEXT, recipe.getIngredients().get(1).getType());
		
		// processing ending with =or=
		recipe.getIngredients().clear();
		recipe.addIngredient(new RecipeIngredient("8 tablespoon salt -- more -=or=-"));
		recipe.addIngredient(new RecipeIngredient("4 tablespoon peber"));
		recipe.normalizeIngredientsWithOr();
		assertEquals(RecipeIngredient.TYPE_INGREDIENT, recipe.getIngredients().get(0).getType());
		assertEquals(RecipeIngredient.TYPE_TEXT, recipe.getIngredients().get(1).getType());
	}

	@Test
	public void testLinebreaks() {
		//x = System.getProperty("line.separator");
		String x = "test\rtest1";
		//dump(x);
		String x1 = Recipe.normalizeLineBreaks(x);
		//dump(x1);
		assertEquals("test\ntest1", x1);
		assertEquals("test\ntest1", Recipe.normalizeLineBreaks("test\rtest1"));
		assertEquals("test\ntest1", Recipe.normalizeLineBreaks("test\ntest1"));
		assertEquals("test\ntest1", Recipe.normalizeLineBreaks("test\r\ntest1"));
		assertEquals("\ntest1", Recipe.normalizeLineBreaks("\rtest1"));
		assertEquals("\ntest1", Recipe.normalizeLineBreaks("\ntest1"));
		assertEquals("\ntest1", Recipe.normalizeLineBreaks("\r\ntest1"));
	}
	
	void dump(String str) {
		System.out.print(str);
		System.out.print(" =");
		for (byte i : str.getBytes()) {
			System.out.print(" "+i);
		}
		System.out.println();
	}

	@Test
	public void testDirectionsRemoveParagraphHeader() {
		Configuration.setBooleanProperty("REMOVE_DIRECTION_STEP_NUMBERS", true);

		Recipe r = new Recipe();
		r.setDirections("1. test1\n\n2. test2");
		r.directionsRemoveParagraphHeader();
		assertEquals("test1\n\ntest2", r.getDirectionsAsString() );

		r.setDirections("* test1\n\n* test2");
		r.directionsRemoveParagraphHeader();
		assertEquals("test1\n\ntest2", r.getDirectionsAsString() );
		
		r.setDirections("1. heat to 200 degrees\n\n2. mix a and b\n\n3. stir c in.\n");
		r.normalize();
		assertEquals("heat to 200 degrees\n\nmix a and b\n\nstir c in.\n", r.getDirectionsAsString() );
		
		Configuration.setBooleanProperty("REMOVE_DIRECTION_STEP_NUMBERS", false);
		r.setDirections("1. heat to 200 degrees\n\n2. mix a and b\n\n3. stir c in.\n");
		r.normalize();
		assertEquals("1. heat to 200 degrees\n\n2. mix a and b\n\n3. stir c in.\n", r.getDirectionsAsString() );
		
	}

	@Test
	public void testNormalizeIngredientLinesWithMatchingParens() {
		Recipe r = new Recipe();
		r.addIngredient(new RecipeIngredient("1 cup tomatoes ( "));
		r.addIngredient(new RecipeIngredient("with no"));
		r.addIngredient(new RecipeIngredient("seeds)"));
		r.normalizeIngredientLinesWithMatchingParens();
		assertEquals(1, r.getIngredients().size());
		assertEquals("tomatoes (with no seeds)", 
				r.getIngredients().get(0).getIngredient().getName());

		// another
		r.getIngredients().clear();
		r.addIngredient(new RecipeIngredient(1.0f, "cup", "tomatoes", "drained ( "));
		r.addIngredient(new RecipeIngredient("yellow tomatoes"));
		r.addIngredient(new RecipeIngredient("without seeds)"));
		r.normalizeIngredientLinesWithMatchingParens();
		assertEquals(1, r.getIngredients().size());
		assertEquals("drained (yellow tomatoes without seeds)", 
				r.getIngredients().get(0).getProcessing());
		
		// another
		r.getIngredients().clear();
		r.addIngredient(new RecipeIngredient("salt"));
		r.addIngredient(new RecipeIngredient("(leaves)"));
		r.normalize();
		assertEquals(2, r.getIngredients().size());

	}

	@Test
	public void testNormalizeIngredientLinesWithEndings() {
		Recipe r = new Recipe();
		r.addIngredient(new RecipeIngredient("1 cup frozen spinach completely thawed,"));
		r.addIngredient(new RecipeIngredient("drained and chopped"));
		r.normalizeIngredientLinesWithEndings();
		assertEquals(1, r.getIngredients().size());
		assertEquals("frozen spinach completely thawed, drained and chopped", 
				r.getIngredients().get(0).getIngredient().getName());

		// last line with and/or
		r.getIngredients().clear();
		r.addIngredient(new RecipeIngredient("1 cup tomatoes"));
		r.addIngredient(new RecipeIngredient("1 cup sugar and"));
		r.normalizeIngredientLinesWithEndings();
		assertEquals(2, r.getIngredients().size());

		// ending with comma
		r.getIngredients().clear();
		r.addIngredient(new RecipeIngredient("1 cup tomatoes,"));
		r.addIngredient(new RecipeIngredient("room temperature"));
		r.normalizeIngredientLinesWithEndings();
		assertEquals(1, r.getIngredients().size());

		// TODO: ending with comma and amount interval
		/*
		r.getIngredients().clear();
		r.addIngredient(new RecipeIngredient("1-1 1/2 cups tomatoes,"));
		r.addIngredient(new RecipeIngredient("room temperature"));
		r.normalize();
		assertEquals(1, r.getIngredients().size());
		*/

		// just one line
		r.getIngredients().clear();
		r.addIngredient(new RecipeIngredient("1 cup sugar and"));
		r.normalizeIngredientLinesWithEndings();
		assertEquals(1, r.getIngredients().size());

		// next line with amount
		r.getIngredients().clear();
		r.addIngredient(new RecipeIngredient("3 oz Package cream cheese,"));
		r.addIngredient(new RecipeIngredient("2 tb Sugar,"));
		r.normalizeIngredientLinesWithEndings();
		assertEquals(2, r.getIngredients().size());

		// as processings
		r.getIngredients().clear();
		r.addIngredient(new RecipeIngredient(1f, "cup", "frozen spinach", "completely thawed,"));
		r.addIngredient(new RecipeIngredient("drained and chopped"));
		r.normalizeIngredientLinesWithEndings();
		assertEquals(1, r.getIngredients().size());
		assertEquals("completely thawed, drained and chopped", 
				r.getIngredients().get(0).getProcessing());

		
		// beginnings
		r.getIngredients().clear();
		r.addIngredient(new RecipeIngredient("3 oz Package cream cheese "));
		r.addIngredient(new RecipeIngredient("and herbs"));
		r.normalizeIngredientLinesWithEndings();
		assertEquals(1, r.getIngredients().size());
		assertEquals("cream cheese and herbs", 
				r.getIngredients().get(0).getIngredient().getName());
		
		// beginnings
		r.getIngredients().clear();
		r.addIngredient(new RecipeIngredient("3 cups dark brown sugar "));
		r.addIngredient(new RecipeIngredient("= (or Mexican brown sugar)"));
		r.normalizeIngredientLinesWithEndings();
		assertEquals(1, r.getIngredients().size());
		assertEquals("dark brown sugar = (or Mexican brown sugar)", 
				r.getIngredients().get(0).getIngredient().getName());

		// beginnings
		r.getIngredients().clear();
		r.addIngredient(new RecipeIngredient("and  Package cream cheese "));
		r.addIngredient(new RecipeIngredient("1 cup sugar"));
		r.normalizeIngredientLinesWithEndings();
		assertEquals(2, r.getIngredients().size());

		// beginnings
		r.getIngredients().clear();
		r.addIngredient(new RecipeIngredient("2 cups heavy cream"));
		r.addIngredient(new RecipeIngredient("to dissolve the gelatin"));
		r.normalizeIngredientLinesWithEndings();
		assertEquals(1, r.getIngredients().size());

		// beginnings
		r.getIngredients().clear();
		r.addIngredient(new RecipeIngredient("2 scallions - (to 3) -- trimmed, sliced"));
		r.addIngredient(new RecipeIngredient("lengthwise into ribbons, and then cut"));
		r.addIngredient(new RecipeIngredient("crosswise into 1&quot; lengths"));
		r.normalizeIngredientLinesWithEndings();
		assertEquals(1, r.getIngredients().size());

		// beginnings + merging parens
		r.getIngredients().clear();
		r.addIngredient(new RecipeIngredient(1f, "cup", "brown mushrooms", "fresh; sliced 1/4\" pieces"));
		r.addIngredient(new RecipeIngredient("(or substitute with 1 can whole straw"));
		r.addIngredient(new RecipeIngredient(0f, "", "mushrooms, drained)", ""));
		r.normalize();
		assertEquals(1, r.getIngredients().size());

		// line with completely processings
		r.getIngredients().clear();
		r.addIngredient(new RecipeIngredient("2 tomatoes"));
		r.addIngredient(new RecipeIngredient("trimmed, sliced"));
		r.normalizeIngredientLinesWithEndings();
		assertEquals(1, r.getIngredients().size());
		assertEquals("tomatoes trimmed, sliced", r.getIngredients().get(0).getIngredient().getName());
		
		// beginnings
/*		
		r.getIngredients().clear();
		r.addIngredient(new RecipeIngredient("3 oz Package cream cheese -- cut into 1/4\" "));
		r.addIngredient(new RecipeIngredient("stripes"));
		r.normalizeIngredientLinesWithEndings();
		assertEquals(1, r.getIngredients().size());

		// beginnings
		r.getIngredients().clear();
		r.addIngredient(new RecipeIngredient("3 oz Package cream cheese -- cut into "));
		r.addIngredient(new RecipeIngredient("1/4-inch stripes"));
		r.normalizeIngredientLinesWithEndings();
		assertEquals(1, r.getIngredients().size());
*/		
		
		// beginnings
		r.getIngredients().clear();
		r.addIngredient(new RecipeIngredient("3 oz Package cream cheese "));
		r.addIngredient(new RecipeIngredient(" = (in jars)"));
		r.normalizeIngredientLinesWithEndings();
		assertEquals(1, r.getIngredients().size());

		// beginnings
		r.getIngredients().clear();
		r.addIngredient(new RecipeIngredient("3 oz Package cream cheese "));
		r.addIngredient(new RecipeIngredient(" ==== Subtitle === "));
		r.normalizeIngredientLinesWithEndings();
		assertEquals(2, r.getIngredients().size());
		
		// Exception: beginnings with about
		r.getIngredients().clear();
		r.addIngredient(new RecipeIngredient("2 cups heavy cream"));
		r.addIngredient(new RecipeIngredient("about 1 tsp salt"));
		r.normalizeIngredientLinesWithEndings();
		assertEquals(2, r.getIngredients().size());
		
		// other cases
		r.getIngredients().clear();
		r.addIngredient(new RecipeIngredient("3/4 teaspoon salt"));
		r.addIngredient(new RecipeIngredient("Tortilla chips"));
		r.normalizeIngredientLinesWithEndings();
		assertEquals(2, r.getIngredients().size());
		assertEquals("3/4 teaspoon salt", r.getIngredients().get(0).toString());
	}

	@Test
	public void testNormalizeIngredientLinesWithOnlyAmounts() {
		Recipe r = new Recipe();
		
		// split after amount
		r.addIngredient(new RecipeIngredient("1"));
		r.addIngredient(new RecipeIngredient("oz water"));
		r.normalizeIngredientLinesWithOnlyAmounts();
		assertEquals(1, r.getIngredients().size());
		assertEquals("1 oz water", r.getIngredients().get(0).toString());

		// split after amount - with no unit
		r.getIngredients().clear();
		r.addIngredient(new RecipeIngredient("1"));
		r.addIngredient(new RecipeIngredient("apple"));
		r.normalizeIngredientLinesWithOnlyAmounts();
		assertEquals(1, r.getIngredients().size());
		assertEquals("1 apple", r.getIngredients().get(0).toString());

		// split after unit
		r.getIngredients().clear();
		r.addIngredient(new RecipeIngredient("1 oz"));
		r.addIngredient(new RecipeIngredient("water"));
		r.normalizeIngredientLinesWithOnlyAmounts();
		assertEquals(1, r.getIngredients().size());
		assertEquals("1 oz water", r.getIngredients().get(0).toString());
		
		// do a full normalize
		r.getIngredients().clear();
		r.addIngredient(new RecipeIngredient("1"));
		r.addIngredient(new RecipeIngredient("oz water"));
		r.normalize();
		assertEquals(1, r.getIngredients().size());
		assertEquals("1 ounce water", r.getIngredients().get(0).toString());
	}	
	
	
	@Test
	public void testRegexpr() {
		Recipe r = new Recipe();
		// regexp problems with the second line because of the spaces
		r.getIngredients().clear();
		r.addIngredient(new RecipeIngredient("3 oz Package cream cheese "));
		r.addIngredient(new RecipeIngredient("Peanuts -- chopped -                      Chocolate jimmies* ( opt )"));
		r.normalizeIngredientLinesWithEndings();
		assertEquals(2, r.getIngredients().size());
	}

	@Test
	public void testNormalizeIngredientsWithGraphicalChar() {
		Recipe r = new Recipe();
		r.getIngredients().clear();
		r.addIngredient(new RecipeIngredient("Cake 1:"));
		r.addIngredient(new RecipeIngredient("* 2 tbsp sugar"));
		r.addIngredient(new RecipeIngredient("* 3 dl water"));
		r.normalizeIngredientsWithGraphicalChar();
		assertEquals(3, r.getIngredients().size());
		RecipeIngredient ingr = r.getIngredients().get(0);
		assertEquals("Cake 1:", ingr.getIngredient().getName());
		ingr = r.getIngredients().get(1);
		assertEquals(2f, ingr.getAmount(), 0.01);
		assertEquals("tbsp", ingr.getUnit().getName());
		assertEquals("sugar", ingr.getIngredient().getName());
		ingr = r.getIngredients().get(2);
		assertEquals(3f, ingr.getAmount(), 0.01);
		assertEquals("dl", ingr.getUnit().getName());
		assertEquals("water", ingr.getIngredient().getName());
	}

	@Test
	public void testNormalizeIngredientsWithGraphicalCharWithEmptyLine() {
		Recipe r = new Recipe();
		r.getIngredients().clear();
		r.addIngredient(new RecipeIngredient("Cake 1:"));
		r.addIngredient(new RecipeIngredient("    "));
		r.addIngredient(new RecipeIngredient("* 2 tbsp sugar"));
		r.normalizeIngredientsWithGraphicalChar();
		assertEquals(3, r.getIngredients().size());
	}

	
	@Test
	public void testNormalizeSimple() {
		Recipe recipe = new Recipe();
		recipe.setTitle("test");
		RecipeIngredient ingr = null;
		ingr = new RecipeIngredient("1-1.5 400g cans diced tomatoes (I used Meditteranean Tomatoes, with capers and black");
		recipe.addIngredient(ingr);
		ingr = new RecipeIngredient("olives)");
		recipe.addIngredient(ingr);
		recipe.normalize();
		
		// test for exceptions with no ingredients
		recipe.setIngredients((String) null);
		recipe.normalize();

		recipe.setIngredients("");
		recipe.normalize();

		recipe.setIngredients("  ");
		recipe.normalize();

		recipe.setIngredients((List<RecipeIngredient>)null);
		recipe.normalize();
		
	}

	@Test
	public void testSetCategory() {
		Configuration.setStringProperty("CATEGORY_SPLIT_CHARS", ";,");
		Recipe recipe = new Recipe();
		recipe.setCategories("test");
		assertEquals(1, recipe.getCategories().size());
		recipe.getCategories().clear();

		recipe.setCategories("test1, test2");
		assertEquals(2, recipe.getCategories().size());
		recipe.getCategories().clear();

		recipe.setCategories("test1; test2");
		assertEquals(2, recipe.getCategories().size());
		recipe.getCategories().clear();

		// invalid regexp
		Configuration.setStringProperty("CATEGORY_SPLIT_CHARS", "^");
		recipe.setCategories("test1; test2");
		assertEquals(2, recipe.getCategories().size());
		recipe.getCategories().clear();

		// empty
		Configuration.setStringProperty("CATEGORY_SPLIT_CHARS", "");
		recipe.setCategories("test1; test2");
		assertEquals(2, recipe.getCategories().size());
		recipe.getCategories().clear();
	}
	
	@Test
	public void testFixSpaces() {
		Recipe r = new Recipe();
		assertEquals("test\ntest", r.fixSpaces("  test  \n  test   "));
		assertEquals("test1, test2; test3: test4", r.fixSpaces("test1 , test2 ; test3 : test4"));
		assertEquals("(test)", r.fixSpaces(" ( test ) "));
		assertEquals("test test", r.fixSpaces("test    test"));
	}
	
	public void testDirectionsRestructureSentencses() {
		Recipe r = new Recipe();
		String text = "Squeeze juice from oranges to make 2 1/2 cups juice.\n"
				+ "Meanwhile, put sugar and 2 1.2 cups water in a large saucepan and cook\n"
				+ "over a low heat, stirring occasionally, until sugar has dissolved. Bring\n"
				+ "to boil\n"
				+ "and boil gently, uncovered, for 5 minutes. Add orange juice and boil\n"
				+ "gently for\n"
				+ "a further 5 minutes. leave to cool, then chill.\n"
				+ "To serve, put ice cubes in glasses with orange peel spirals and mint sprigs.\n";
		r.restructureSentences(text);
		assertEquals(3, r.getDirections().size());
		
		/*
Dir::Preparation time 15 minutes
Cooking time 5 minutes
Chilling time 2 hours
Freezing time Variable
Yield 8 servings

1. Combine ginger, sugar and water in heavy medium saucepan. Heat to boil
over medium-high heat; stir until sugar dissolves, about 5 minutes.
Refrigerate until cool, at least 2 hours or overnight.

2. Puree pineapple and ginger syrup in blender until smooth. Strain through
fine mesh strainer. Freeze mixture in ice cream machine according to
manufacturer' directions until slushy. Place in freezer until firm.

Nutrition information per serving
Calories ........... 200 Fat ............ 0.5 g Saturated fat .. 0 g
% calories from fat .. 2 Cholesterol ..... 0 mg Sodium ........ 3 mg
Carbohydrates ..... 52 g Protein 
---------------------------------------------------------

Dir::In a medium sauce pan, combine water and butter. Bring mixture to a boil.
Reduce heat to low. Add flour. Stir vigorously until mixture leaves sides
of pan and forms a smooth ball. Remove sauce pan from heat and allow to
cool slightly. Add eggs one at a time.

Beat with a wooden spoon after each addition. Beat until batter is smooth.

---------------------------------------------------------
Dir::Whip cream in a blender for 30-45 seconds. Add peaches and honey. Whip

 until smooth. Pour into molds, insert sticks, and freeze.
---------------------------------------------------------
Dir::Heat cream until bubbles just form. In a small saucepan, melt jam. Stir
mooth and add to warm cream. Cool and strain. Stir in almond extract.
Add rum/brandy. Stir freeze in ice cream maker. Makes about 1 quart.

from: Ice Cream! The Whole Scoop
---------------------------------------------------------
Dir::1. As above.
2. Prepare Ben's Chocolate Ice Cream.
3. Same as #4 above.
---------------------------------------------------------
Dir::This begins with vanilla ice cream - as do most fruit and berry flavors - so
that's why we start with the mother of them all (and, curiously, America's
Number One Favorite flavor-I'll bet you thought it was chocolate!). Use real
vanilla beans and freshly grated nutmeg for truly stunning flavor. Do Not
Reduce the Sugar! or the ice cream will taste flat and freeze too fast,
forming large crystals.
---------------------------------------------------------
---------------------------------------------------------


		 */
		
	}
}
