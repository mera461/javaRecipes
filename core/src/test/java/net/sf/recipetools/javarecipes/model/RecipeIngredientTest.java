/**
 * 
 */
package net.sf.recipetools.javarecipes.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;


/**
 * @author ft
 *
 */
public class RecipeIngredientTest {

	@Before
	public void setUp() {
		Configuration.setIntProperty("TITLE_CASE_PREFERENCE", 1);
		Configuration.setIntProperty("SUBTITLE_CASE_PREFERENCE", 2);
		Configuration.setBooleanProperty("CALCULATE_WEIGHT_FROM_AMOUNT_TIMES_PACKAGE_SIZE", true);
		Configuration.setBooleanProperty("CAPITALIZE_INGREDIENTS_WITHOUT_AMOUNT_UNIT", false);
		Configuration.setBooleanProperty("DETECT_AND_MARK_SERVINGS_FROM_DIRECTIONS", true);
		Configuration.setBooleanProperty("DETECT_AND_MARK_SERVINGS_FROM_YIELD", true);
		Configuration.setBooleanProperty("EXPAND_UNIT_ABBREVIATIONS", true);
		Configuration.setBooleanProperty("INGREDIENT_PROCESSING_WORDS_TO_PREPARATION", true);
		Configuration.setBooleanProperty("MERGE_INGREDIENT_LINE_CONTINUATIONS", true);
		Configuration.setBooleanProperty("MOVE_NOTES_IN_DIRECTIONS_TO_NOTES", true);
		Configuration.setBooleanProperty("MOVE_PARENTHESIS_IN_INGREDIENTS_TO_PREPARATION", true);
		Configuration.setBooleanProperty("MOVE_SMALL_MED_LARGE_TO_INGREDIENTS", false);
		Configuration.setBooleanProperty("MOVE_TEXT_AFTER_COMMAS_TO_PREPARATION", true);
		Configuration.setBooleanProperty("MOVE_TEXT_AFTER_SEMICOLON_TO_PREPARATION", true);
		Configuration.setBooleanProperty("PLURALISE_UNITS", true);
		Configuration.setBooleanProperty("REMOVE_NON_RECIPE_COMMENTS_FROM_DIRECTIONS", true);
		Configuration.setBooleanProperty("REMOVE_NUTRITION_INFORMATION_FROM_DIRECTIONS", false);
		Configuration.setBooleanProperty("SPLIT_INGREDIENT_LINES_WITH_PLUS", true);
		Configuration.setBooleanProperty("USE_WEIGHT_NOT_PACKAGE_SIZE", true);
		Configuration.loadSafeCommaStructuresFromFile("commas.txt");
		System.out.println("*** FINISHED SETUP");
	}

	/**
	 * Test method for {@link net.sf.recipetools.javarecipes.model.RecipeIngredient#normalizeProcessing()}.
	 */
	@Test
	public void testNormalizeProcessing() {
		RecipeIngredient ingr = new RecipeIngredient(0.0f, null, "Ingredient", "processing");
		ingr.normalizeProcessing();
		assertEquals("Ingredient", ingr.getIngredient().getName());
		assertEquals("processing", ingr.getProcessing());

		// Mastercook style splitting
		ingr = new RecipeIngredient(0.0f, null, "Ingredient -- boiled", "processing");
		ingr.normalizeProcessing();
		assertEquals("Ingredient", ingr.getIngredient().getName());
		assertEquals("boiled; processing", ingr.getProcessing());

		ingr = new RecipeIngredient(0.0f, null, "Ingredient --- boilxed", "processing");
		ingr.normalizeProcessing();
		assertEquals("Ingredient --- boilxed", ingr.getIngredient().getName());
		assertEquals("processing", ingr.getProcessing());

		// Splitting at ';'
		ingr = new RecipeIngredient(0.0f, null, "Ingredient ; boiled", "processing");
		ingr.normalizeProcessing();
		assertEquals("Ingredient", ingr.getIngredient().getName());
		assertEquals("boiled; processing", ingr.getProcessing());

		// "to taste"
		Configuration.setBooleanProperty("MOVE_TO_TASTE_TO_PREPARATION", true);
		ingr = new RecipeIngredient(0.0f, null, "Ingredient to taste", "processing");
		ingr.normalizeProcessing();
		assertEquals("Ingredient", ingr.getIngredient().getName());
		assertEquals("(to taste); processing", ingr.getProcessing());
		
		ingr = new RecipeIngredient(0.0f, null, "Ingredient or more to taste", "processing");
		ingr.normalizeProcessing();
		assertEquals("Ingredient", ingr.getIngredient().getName());
		assertEquals("(or more to taste); processing", ingr.getProcessing());

		// without moving to taste
		Configuration.setBooleanProperty("MOVE_TO_TASTE_TO_PREPARATION", false);
		ingr = new RecipeIngredient(0.0f, null, "Ingredient to taste", "processing");
		ingr.normalizeProcessing();
		assertEquals("Ingredient to taste", ingr.getIngredient().getName());
		assertEquals("processing", ingr.getProcessing());
		
		// Ending '-'
		ingr = new RecipeIngredient(0.0f, null, "of baking soda - (5ml)", "processing");
		ingr.normalizeProcessing();
		assertEquals("of baking soda", ingr.getIngredient().getName());
		assertEquals("(5ml) processing", ingr.getProcessing());

		// check for common words
		ingr = new RecipeIngredient(0.0f, null, "Ingredient boiled", "processing");
		ingr.normalizeProcessing();
		assertEquals("Ingredient", ingr.getIngredient().getName());
		assertEquals("boiled; processing", ingr.getProcessing());

		ingr = new RecipeIngredient(0.0f, null, "boiled Ingredient", "processing");
		ingr.normalizeProcessing();
		assertEquals("Ingredient", ingr.getIngredient().getName());
		assertEquals("boiled; processing", ingr.getProcessing());
		
		
	}

	@Test
	public void testGetNumber() {
		assertEquals(0.75f, RecipeIngredient.getNumber("0.75"), 0.001);
		assertEquals(0.75f, RecipeIngredient.getNumber(".75"), 0.001);
		assertEquals(1.5f, RecipeIngredient.getNumber("1.5"), 0.001);
		assertEquals(0f, RecipeIngredient.getNumber(""), 0.001);
		assertEquals(0f, RecipeIngredient.getNumber(" "), 0.001);
		assertEquals(0f, RecipeIngredient.getNumber(null), 0.001);
		assertEquals(1.5f, RecipeIngredient.getNumber(" 1.5"), 0.001);
		assertEquals(1.5f, RecipeIngredient.getNumber(" 1.5   "), 0.001);
		assertEquals(0.75f, RecipeIngredient.getNumber("3/4"), 0.001);
		assertEquals(1.5f, RecipeIngredient.getNumber("1 1/2"), 0.001);
		assertEquals(1.5f, RecipeIngredient.getNumber("1 1 /2"), 0.001);
		assertEquals(1.5f, RecipeIngredient.getNumber("1 1 / 2"), 0.001);
		assertEquals(0.5f, RecipeIngredient.getNumber("½"), 0.001);
		assertEquals(0.25f, RecipeIngredient.getNumber("¼"), 0.001);
	}
	
	@Test
	public void testCreateFromString() {
		Configuration.setBooleanProperty("EXPAND_UNIT_ABBREVIATIONS", true);
		Configuration.setBooleanProperty("DETECT_AND_MARK_SUBTITLES", true);

		RecipeIngredient ingr = null;
		// note that the lines are NOT normalized.
		ingr = new RecipeIngredient("24/7 Seasoning or salt and pepper");
		compareIngredient(ingr, 0, "", "24/7 Seasoning or salt and pepper", null);
		ingr = new RecipeIngredient("5 tsp water");
		compareIngredient(ingr, 5.0f, "tsp", "water", null);
		ingr = new RecipeIngredient("5 1/2 tsp water");
		compareIngredient(ingr, 5.5f, "tsp", "water", null);
		ingr = new RecipeIngredient("5 tsp. water");
		compareIngredient(ingr, 5.0f, "tsp.", "water", null);
		ingr = new RecipeIngredient("10 apples");
		compareIngredient(ingr, 10.0f, "", "apples", null);
		ingr = new RecipeIngredient("this is a subtitle:");
		compareIngredient(ingr, 0.0f, "", "this is a subtitle:", null);
		assertEquals(RecipeIngredient.TYPE_SUBTITLE, ingr.getType());
		ingr = new RecipeIngredient(" === this is a subtitle ===");
		assertEquals(RecipeIngredient.TYPE_SUBTITLE, ingr.getType());
		ingr = new RecipeIngredient(" --- this is a subtitle ---");
		assertEquals(RecipeIngredient.TYPE_SUBTITLE, ingr.getType());
		ingr = new RecipeIngredient("1 can(s) (28-ounce) tomato purée");
		compareIngredient(ingr, 1.0f, "can(s) (28-ounce)", "tomato purée", null);
		ingr = new RecipeIngredient("3/4 tsp. each kosher salt and pepper");
		compareIngredient(ingr, 0.75f, "tsp.", "each kosher salt and pepper", null);
		

		
		// no units?
		ingr = new RecipeIngredient("5 eggs");
		compareIngredient(ingr, 5.0f, null, "eggs", null);
	}
	
	@Test
	public void testCheckIngredientAliasese() {
		RecipeIngredient ingr = null;
		ingr = new RecipeIngredient(3.0f, "sheets", "of  baking soda", "");
		ingr.checkIngredientAliases();
		assertEquals("baking soda", ingr.getIngredient().getName());
		
		ingr = new RecipeIngredient(3.0f, null, "kiwis", "");
		ingr.checkIngredientAliases();
		assertEquals("kiwi", ingr.getIngredient().getName());

		ingr = new RecipeIngredient(3.0f, null, "kiwi fruits", "");
		ingr.checkIngredientAliases();
		assertEquals("kiwi", ingr.getIngredient().getName());
	}

	@Test
	public void testCombinedUnits() {
		RecipeIngredient ingr = null;
		ingr = new RecipeIngredient(3.0f, "14 oz cans", "tomato", "");
		ingr.normalizeEmbeddedUnits();
		compareIngredient(ingr, 42f, "ounce", "tomato", "");

		ingr = new RecipeIngredient(3.0f, "14-oz-can", "tomato", "");
		ingr.normalizeEmbeddedUnits();
		compareIngredient(ingr, 42f, "ounce", "tomato", "");

/*		ingr = new RecipeIngredient(3.0f, "oz-can", "tomato", "");
		ingr.normalizeEmbeddedUnits();
		compareIngredient(ingr, 3f, "ounce", "tomato", "");
*/
		ingr = new RecipeIngredient(3.0f, "(12 oz) can", "some-ingredient", "");
		ingr.normalizeEmbeddedUnits();
		compareIngredient(ingr, 36f, "ounce", "some-ingredient", "");

		ingr = new RecipeIngredient(3.0f, "(150 g each) pkg", "some-ingredient", "");
		ingr.normalizeEmbeddedUnits();
		compareIngredient(ingr, 450f, "g", "some-ingredient", "");

		ingr = new RecipeIngredient(3.0f, "(150 g each) pkg.", "some-ingredient", "");
		ingr.normalizeEmbeddedUnits();
		compareIngredient(ingr, 450f, "g", "some-ingredient", "");
		
		ingr = new RecipeIngredient(3.0f, "(2 litres) bottle", "some-ingredient", "");
		ingr.normalizeEmbeddedUnits();
		compareIngredient(ingr, 6f, "litres", "some-ingredient", "");

		ingr = new RecipeIngredient(3.0f, "(15 oz.) box", "seeded raisins", null);
		ingr.normalizeEmbeddedUnits();
		compareIngredient(ingr,45f, "ounce", "seeded raisins", null);
	
		ingr = new RecipeIngredient("1 10-ounce can crushed pineapple");
		ingr.normalizeEmbeddedUnits();
		compareIngredient(ingr, 10f, "ounce", "crushed pineapple", null);

		ingr = new RecipeIngredient("1 (10-ounce) can crushed pineapple");
		ingr.normalizeEmbeddedUnits();
		compareIngredient(ingr, 10f, "ounce", "crushed pineapple", null);
		
		// TEST WITH CONFIG OFF
		Configuration.setBooleanProperty("CALCULATE_WEIGHT_FROM_AMOUNT_TIMES_PACKAGE_SIZE", false);
		ingr = new RecipeIngredient("1 (10-ounce) can crushed pineapple");
		ingr.normalize();
		compareIngredient(ingr, 1f, "(10-ounce) can", "pineapple", "crushed");

		
	}
	

	
	@Test
	public void testNormalizeUnits() {
		RecipeIngredient ingr = new RecipeIngredient(3.0f, "heaped tsp", "sugar", "");
		ingr.normalizeUnits();
		compareIngredient(ingr, 4.5f, "tsp", "sugar", "");

		ingr = new RecipeIngredient(3.0f, "heaped  tsp", "sugar", "");
		ingr.normalizeUnits();
		compareIngredient(ingr, 4.5f, "tsp", "sugar", "");

		ingr = new RecipeIngredient(3.0f, "leveled tsp", "sugar", "");
		ingr.normalizeUnits();
		compareIngredient(ingr, 3.0f, "tsp", "sugar", "");

		ingr = new RecipeIngredient(3.0f, "squares", "milkchocolate", "");
		ingr.normalizeUnits();
		compareIngredient(ingr, 3.0f, "ounce", "milkchocolate", "");
		
		ingr = new RecipeIngredient(3.0f, "sticks", "butter", "");
		ingr.normalizeUnits();
		compareIngredient(ingr, 12.0f, "ounce", "butter", "");

		ingr = new RecipeIngredient(3.0f, "sticks", "margarine", "");
		ingr.normalizeUnits();
		compareIngredient(ingr, 12.0f, "ounce", "margarine", "");

	}
	
	@Test
	public void testCheckForBetterUnits() {
		
		// TODO: What about these? Should they be changed?
		RecipeIngredient ingr = new RecipeIngredient(3.0f, "cups", "cookies -- crushed (about 12 oz.)", "");
		ingr.checkForBetterUnits();
		compareIngredient(ingr, 3f, "cups", "cookies -- crushed", "(about 12 oz.)");

		ingr = new RecipeIngredient(3.0f, "can", "(12 oz) some-ingredient", "");
		ingr.checkForBetterUnits();
		compareIngredient(ingr, 36f, "ounce", "some-ingredient", "(12 oz)");

		ingr = new RecipeIngredient(3.0f, "cups", "(1 pt.) some-ingredient", "");
		ingr.checkForBetterUnits();
		compareIngredient(ingr, 3f, "cups", "some-ingredient", "(1 pt.)");
		
		ingr = new RecipeIngredient("1 1/2 cup (6 oz) cheese");
		ingr.checkForBetterUnits();
		compareIngredient(ingr, 1.5f, "cup", "cheese", "(6 oz)");
		
		ingr = new RecipeIngredient("1 cup brown mushrooms (or substitute with 1 can whole straw mushrooms, drained)");
		ingr.checkForBetterUnits();
		compareIngredient(ingr, 1.0f, "cup", "brown mushrooms (or substitute with 1 can whole straw mushrooms, drained)", null);

		ingr = new RecipeIngredient(3.0f, "cup", "(2 sticks) butter", "");
		ingr.checkForBetterUnits();
		compareIngredient(ingr, 3f, "cup", "butter", "(2 sticks)");
	}

	public void compareIngredient(RecipeIngredient ingr, String amount, String unit, String ingrName, String processing) {
		float value = 0;
		if (amount.length()>0) {
			value = Float.valueOf(amount);
		}
		compareIngredient(ingr, value, unit, ingrName, processing);
	}
	
	
	public void compareIngredient(RecipeIngredient ingr, float amount, String unit, String ingrName, String processing) {
		assertEquals("For ingredient:"+ingr, amount, ingr.getAmount(), 0.001);
		if (unit==null || unit.length()==0) {
			assertTrue("For ingredient:"+ingr, ingr.hasNoUnit());
		} else {
			if (ingr.getUnit() == null) {
				assertEquals("For ingredient:"+ingr, unit, ingr.getUnit());
			} else {
				assertEquals("For ingredient:"+ingr, unit, ingr.getUnit().getName());
			}
		}
		if (ingrName==null) {
			assertEquals("For ingredient:"+ingr, ingrName, ingr.getIngredient());
		} else {
			assertEquals("For ingredient:"+ingr, ingrName, ingr.getIngredient().getName());
		}
		assertEquals("For ingredient:"+ingr, processing, ingr.getProcessing());
	}
	
	@Test
	public void testNormalize1() {
		String[][] inputOutput = new String[][] {
//				{"1 98 grams pac semolina", 			"1", "98 grams package", "semolina", ""},
//				{"", 			"", "", "", ""},
//				{"", 			"", "", "", ""},
				{"1/25 1/2 ounce can  black beans, rinsed", 			"0.02", "ounce", "black beans", "rinsed"},
				{"4 leaves red leaf lettuce", 			"4", "leaf", "red leaf lettuce", ""},
				{"1 cup roasted macadamia nuts - (abt 1 1/4 cups) -- ground", "1", "cup", "roasted macadamia nuts", "ground (abt 1 1/4 cups)"},
				{"1 small onion -- PLUS", 			"1", "small", "onion", "PLUS"},
				{"1 small onion -- chopped", 			"1", "small", "onion", "chopped"},
				{"1 small pinch salt", 			"1", "small pinch", "salt", ""},
				{"1 rounded tbsp salt", 			"1", "rounded tablespoon", "salt", ""},
				{"2 tablespoon(s) sugar", 			"2", "tablespoon", "sugar", ""},
				{"1/4 teaspoon Spanish paprika (pimentón) or Hungarian paprika", 			"0.25", "teaspoon", "Spanish paprika or Hungarian paprika", "(pimentón)"},
				{"2 (rounded) tsp sugar", 			"2", "(rounded) teaspoon", "sugar", ""},
				{"12 (6-inch) corn tortillas", 			"12", "(6-inch)", "corn tortillas", ""},
				{"1 (center-cut) slice ham, cut 3/4 inch thick and fat trimmed", "1", "(center-cut) slice", "ham", "cut 3/4 inch thick and fat trimmed"},
				{"1 (10 to 13-pound) turkey", 			"1", "(10 to 13-pound)", "turkey", ""},
				{"1 (10-to 12-pound) turkey", 			"1", "(10-to 12-pound)", "turkey", ""},
				{"about 1 tsp salt", 			"1", "teaspoon", "salt", "(about)"},
				{"6 fresh seafood sausages (about 1 1/2 lbs.)", 			"1.5", "pound", "seafood sausages", "fresh; (about 1 1/2 lbs.)"},
				{"1 cup water -- PLUS", 			"1", "cup", "water", "PLUS"},
				{"1 onion -- plus", 			"1", "", "onion", "plus"},
				{"1 tsp.. sugar", 			"1", "teaspoon", "sugar", ""},
				{"a few onions", 			"", "a few", "onions", ""},
				{"a couple of onions", 			"", "a couple", "onions", ""},
				{"a large handful of sugar", 			"", "a large handful", "sugar", ""},
				{"a handful of sugar", 			"", "a handful", "sugar", ""},
				{"200 g 1/2\" thick cod fillet", 			"200", "g", "cod fillet", "1/2\" thick"},
				{"1 cup diced 1/4 inch onion", 			"1", "cup", "onion", "diced 1/4 inch"},
				{"1 cup 1/4 inch diced onion", 			"1", "cup", "onion", "1/4 inch diced"},
				{"2.0 cups sliced 1-inch celery", 			"2", "cup", "celery", "sliced 1-inch"},
				{"1 pound sliced 1/4 inch thick raw turkey cutlets", 			"1", "pound", "raw turkey cutlets", "sliced 1/4 inch thick"},
				{"1 pound sliced 1/4\"-thk raw turkey cutlets", 			"1", "pound", "raw turkey cutlets", "sliced 1/4\"-thk"},
				{"1 cup coarsely-chopped avocado", 			"1", "cup", "avocado", "coarsely-chopped"},
				{"1 ripe but firm avocado", 			"1", "", "avocado", "ripe but firm"},
				{"1 cup drained and chopped tomatoes", 			"1", "cup", "tomatoes", "drained and chopped"},
//TODO:				{"drained and chopped", 			"", "", "", "drained and chopped"},
				{"1 cup tomatoes -- drained and chopped", 			"1", "cup", "tomatoes", "drained and chopped"},
				{"1 cup finely-chopped tomatoes", 			"1", "cup", "tomatoes", "finely-chopped"},
				{"3.75 cup tomato; cut", 			"3.75", "cup", "tomato", "cut"},
				{"3.75 cup tomato", 			"3.75", "cup", "tomato", ""},
				{"1 teaspoon baking powder",	"1", "teaspoon", "baking powder", ""},
				{"0.5 teaspoons salt", 			"0.5", "teaspoon", "salt", ""},
				{"1 cup magarine, softened", 	"1", "cup", "magarine", "softened"},
				{"1.5 cups white sugar",		"1.5", "cup", "white sugar", ""},
				{"2 eggs",						"2", "", "eggs", ""},
				{"2 teaspoons vanilla extract", "2", "teaspoon", "vanilla extract", ""},
				{"1 Tablespoon margarine", 		"1", "tablespoon", "margarine", ""},
				{"Salt and freshly ground black pepper", "", "", "Salt and freshly ground black pepper", ""},
				{"Sprinkles, for garnish, optional", "", "", "Sprinkles", "for garnish, optional"},
				{"Chopped nuts, for garnish, optional", "", "", "nuts", "Chopped; for garnish, optional"},
				{"Colored nonpareils, for garnish, optional", "", "", "Colored nonpareils", "for garnish, optional"},
				{"Coconut, for garnish, optional", "", "", "Coconut", "for garnish, optional"},
				{"Crushed cookies, for garnish, optional", "", "", "cookies", "Crushed; for garnish, optional"},
				{"2 (2-pound) flank steaks",	"4", "pound", "flank steaks", ""},
				{"2 (2-pound) can flank steaks", "4", "pound", "flank steaks", ""},
				{"1 9-ounce package fresh spinach leaves", "9", "ounce", "spinach leaves", "fresh"},
				{"1 (750-milliliter) bottle milk", "750", "ml", "milk", ""},
				{"1 1/2 cups mostaccioli (tube-shaped pasta) uncooked (4 ounces)", "1.5", "cup", "mostaccioli", "uncooked; (tube-shaped pasta) (4 ounces)"},
				{"1 (3-pound) rabbit or chicken, cut with a cleaver through bones into 2-inch pieces", "3", "pound", "rabbit or chicken", "cut with a cleaver through bones into 2-inch pieces"},
//TODO:				{"4 medium-sized or 3 large potatoes", "4", "medium", "potatoes", ""},
//TODO:				{"5 cups small cauliflower florets (from 1 large head)", "5", "cup", "", ""},
//				{"", "", "", "", ""},
		};

		testIngredientLines(inputOutput);
	}

	private void testIngredientLines(String[][] inputOutput) {
		RecipeIngredient ingr = null;
		for(int i=0; i<inputOutput.length;i++) {
			ingr = new RecipeIngredient(inputOutput[i][0]);
			ingr.normalize();
			compareIngredient(ingr, inputOutput[i][1], inputOutput[i][2], inputOutput[i][3], inputOutput[i][4]);
		}
	}

	@Test
	public void testWithNoMoveProcessingWords() {
		String[][] inputOutput = new String[][] {
				// double units
//				{"", "", "", "", ""},
//				{"", "", "", "", ""},
//				{"2 cups (1-inch) cut green beans", "2", "cup", "(1-inch) cut green beans", ""},
//				{"", "", "", "", ""},
//				{"5 cups peeled, chopped, ripe fresh peaches -- (about 3 pounds), divided", "5", "cup", "peeled, chopped, ripe fresh peaches", "(about 3 pounds), divided"},
				{"A few drops of truffle oil (optional)", 			"", "a few drop", "truffle oil", "(optional)"},
				{"1/2 cup cooked bean threads (cellophane noodles, about 1 ounce uncooked)", 			"0.5", "cup", "cooked bean threads", "(cellophane noodles, about 1 ounce uncooked)"},
				{"3 cups cooked Rotini (corkscrew pasta),(cooked without salt or fat)", "3", "cup", "cooked Rotini", "(corkscrew pasta) (cooked without salt or fat)"},
				{"1 (16-rib) crown roast of lamb", "1", "(16-rib)", "crown roast of lamb", ""},
				{"3  Tbsp. balsamic vinegar", "3", "tablespoon", "balsamic vinegar", ""},
				{"1 tub butter", "1", "tub", "butter", ""},
				{"Four 8-in. skewers", "", "", "Four 8-in. skewers", ""},
				{"One 2-in. piece ginger, peeled and finely shredded into a mush", "", "", "One 2-in. piece ginger", "peeled and finely shredded into a mush"},
				{"9 whole-wheat lasagna noodles (about 8 oz.)", "9", "", "whole-wheat lasagna noodles", "(about 8 oz.)"}, 
				{"3 (6-inch) whole-wheat pita bread rounds, split", "3", "(6-inch)", "whole-wheat pita bread rounds", "split"},
				{"2 small, firm unpeeled ripe Bosc pears (about 3/4 pound), cored and cut lengthwise into 1/2-inch-thick slices", "2", "", "small, firm unpeeled ripe Bosc pears", "(about 3/4 pound) cored and cut lengthwise into 1/2-inch-thick slices"},
				{"4 medium, firm ripe pears (about 2 pounds)", "4", null, "medium, firm ripe pears", "(about 2 pounds)"},
				{"1 small onion", "1", "small", "onion", ""},
				{"2 cups small broccoli florets", "2", "cup", "small broccoli florets", ""},
				{"1 pound medium shrimp, peeled and deveined", "1", "pound", "medium shrimp", "peeled and deveined"},
//				{"2 tablespoons tub light cream cheese, softened", "2", "tablespoon", "tub light cream cheese", "softened"},
				{"2 tablespoons stick margarine, softened", "2", "tablespoon", "stick margarine", "softened"},
				{"1 (3-pound) lean, bone-in leg of lamb roast", "1", "(3-pound)", "lean, bone-in leg of lamb roast", ""},
				{"1 (3-pound) lean, bone-in leg of lamb roast", "1", "(3-pound)", "lean, bone-in leg of lamb roast", ""},
				{"1 pound medium, unpeeled plum tomatoes", "1", "pound", "medium, unpeeled plum tomatoes", ""},
				{"1/2 cup whole-wheat flour", "0.5", "cup", "whole-wheat flour", ""},
				{"1 (10-oz-can) sugar", 			"1", "(10-ounce-can)", "sugar", ""},
				{"1 (10-oz) can sugar", 			"1", "(10-ounce) can", "sugar", ""},
				{"1 can sugar (about 10 oz)", 		"1", "can", "sugar", "(about 10 oz)"},
				{"1 can sugar (150 g)", 			"1", "can", "sugar", "(150 g)"},
//				{"1 can (150 g) sugar", 			"1", "can", "sugar", "(150 g)"},
				{"10 oz (perhaps a little more) sugar ", 	"10", "ounce", "sugar", "(perhaps a little more)"},
				{"10 oz sugar (perhaps a little more)", 	"10", "ounce", "sugar", "(perhaps a little more)"},
				{"1 (8.8-ounce) can sugar", 			"1", "(8.8-ounce) can", "sugar", ""},
				{"1 (.75-ounce) package sugar", 		"1", "(.75-ounce) package", "sugar", ""},
				{"1 (.75-ounce) package sugar", 		"1", "(.75-ounce) package", "sugar", ""},
				{"3 (5-inch) mint sprigs", 			"3", "(5-inch)", "mint sprigs", ""},
				{"10 (1/4-inch-thick) slices tomatoes", "10", "(1/4-inch-thick) slice", "tomatoes", ""},
				{"4 (4-ounce) skinned, boned chicken breast halves", "4.0", "(4-ounce)", "skinned, boned chicken breast halves", ""},
		};
		
		// turn off embedded unit processing
		Configuration.setBooleanProperty("USE_WEIGHT_NOT_PACKAGE_SIZE", false);
		Configuration.setBooleanProperty("CALCULATE_WEIGHT_FROM_AMOUNT_TIMES_PACKAGE_SIZE", false);
		Configuration.setBooleanProperty("INGREDIENT_PROCESSING_WORDS_TO_PREPARATION", false);
		
		testIngredientLines(inputOutput);
	}

	@Test
	public void testWithExpandEmbeddedUnits() {
		String[][] inputOutput = new String[][] {
				// double units
				{"2 (2-pound) flank steaks",	"4", "pound", "flank steaks", ""},
		};
		
		// test with EMBEDDED UNITS = true
		Configuration.setBooleanProperty("USE_WEIGHT_NOT_PACKAGE_SIZE", true);
		Configuration.setBooleanProperty("CALCULATE_WEIGHT_FROM_AMOUNT_TIMES_PACKAGE_SIZE", true);
		Configuration.setBooleanProperty("INGREDIENT_PROCESSING_WORDS_TO_PREPARATION", false);
		Configuration.setBooleanProperty("PLURALISE_UNITS", true);

		testIngredientLines(inputOutput);
		
		String[][] inputOutput1 = new String[][] {
				// double units
				{"2 (2-pound) flank steaks",	"2", "(2-pound)", "flank steaks", ""},
		};
		Configuration.setBooleanProperty("CALCULATE_WEIGHT_FROM_AMOUNT_TIMES_PACKAGE_SIZE", false);

		testIngredientLines(inputOutput1);

		
	}

	
	
	
	@Test
	public void testWithParenthesisWithBetterUnits() {
		String[][] inputOutput = new String[][] {
				{"1 (10-oz-can) sugar", 			"10", "ounce", "sugar", ""},
				{"1 (10-oz) can sugar", 			"10", "ounce", "sugar", ""},
				{"1 can sugar (about 10 oz)", 		"10", "ounce", "sugar", "(about 10 oz)"},
				{"1 can sugar (150 g)", 			"150", "g", "sugar", "(150 g)"},
				{"1 can (150 g) sugar", 			"150", "g", "sugar", ""},
				{"10 oz (perhaps a little more) sugar ", 	"10", "ounce", "sugar", "(perhaps a little more)"},
				{"10 oz sugar (perhaps a little more)", 	"10", "ounce", "sugar", "(perhaps a little more)"},
				{"1 (8.8-ounce) can sugar", 			"8.8", "ounce", "sugar", ""},
				{"1 (.75-ounce) package sugar", 		"0.75", "ounce", "sugar", ""},
//TODO:				{"5 (1 1/2 x 1/2-inch) strip carrot", 	"5", "(1 1/2 x 1/2-inch) strip", "carrot", ""},
		};
		
		testIngredientLines(inputOutput);
	}

	@Test
	public void testWithoutBetterUnits() {
		String[][] inputOutput = new String[][] {
				{"1/25 1/2 ounce can  black beans, rinsed", 			"0.04", "1/2 ounce can", "black beans", "rinsed"},
				{"1 can (10 oz.) enchilada sauce", 			"1", "can (10 ounce)", "enchilada sauce", ""},
				{"1 (10-oz-can) sugar", 			"1", "(10-ounce-can)", "sugar", ""},
				
		};

		Configuration.setBooleanProperty("USE_WEIGHT_NOT_PACKAGE_SIZE", false);
		Configuration.setBooleanProperty("CALCULATE_WEIGHT_FROM_AMOUNT_TIMES_PACKAGE_SIZE", false);
		testIngredientLines(inputOutput);
	}

	@Test
	public void testWithCommas() {
		Configuration.loadSafeCommaStructuresFromFile("commas.txt");
		String[][] inputOutput = new String[][] {
				{"10 peeled, cored pears", 			"10", "", "peeled, cored pears", ""},
				{"10 apples, peeled and cored", 	"10", "", "apples", "peeled and cored"},
				{"100 g natural-style, reduced fat cheese", "100", "g", "natural-style, reduced fat cheese", "" },
				{"4 large seeded, sliced pepper", 	"4", "large", "seeded, sliced pepper", ""},
				{"4 large pepper, seeded, sliced", 	"4", "large", "pepper", "seeded, sliced"},
				{"4 large pepper, seeded and sliced", 	"4", "large", "pepper", "seeded and sliced"},
				{"4 lean, bone-in chicken", "4", "", "lean, bone-in chicken", ""},
				{"1 medium Rome apple, peeled, cored, and coarsely chopped", "1", "medium", "Rome apple", "peeled, cored, and coarsely chopped"},
		};
		
		Configuration.setBooleanProperty("INGREDIENT_PROCESSING_WORDS_TO_PREPARATION", false);
		testIngredientLines(inputOutput);
	}

	@Test
	public void testWithAndWithOutCommas() {
		Configuration.loadSafeCommaStructuresFromFile("commas.txt");
		Configuration.setBooleanProperty("INGREDIENT_PROCESSING_WORDS_TO_PREPARATION", false);
		Configuration.setBooleanProperty("MOVE_TEXT_AFTER_COMMAS_TO_PREPARATION", true);
		RecipeIngredient ingr = new RecipeIngredient("10 peeled, cored pears, sliced");
		ingr.normalize();
		compareIngredient(ingr, "10", "", "peeled, cored pears", "sliced");
		Configuration.setBooleanProperty("MOVE_TEXT_AFTER_COMMAS_TO_PREPARATION", false);
		ingr = new RecipeIngredient("10 peeled, cored pears, sliced");
		ingr.normalize();
		compareIngredient(ingr, "10", "", "peeled, cored pears, sliced", "");
	}
	
	@Test
	public void testWithAmountIntervals() {
		Configuration.loadSafeCommaStructuresFromFile("commas.txt");
		String[][] inputOutput = new String[][] {
				// 
				{"1-1 1/2 cups ricotta cheese,", 	"1", "cup", "ricotta cheese", "(1-1 1/2)"},
				{"1/3 - 1/2 cup (2 4/8 ounces) rum -- or other spirits", 	"0.333", "cup", "rum", "or other spirits (1/3 - 1/2) (2 4/8 ounces)"},
				{"1/3 - 1/2 cup rum -- or other spirits (2 5/8 ounces)", 	"0.333", "cup", "rum", "or other spirits (2 5/8 ounces) (1/3 - 1/2)"},
				{"10-12 apples, peeled -- Granny Smith", 	"10", "", "apples", "(10-12) Granny Smith peeled"},
				{"10 - 12 apples, peeled -- Granny Smith", 	"10", "", "apples", "(10 - 12) Granny Smith peeled"},
				{"10 to 12 apples, peeled -- Granny Smith", "10", "", "apples", "(10 to 12) Granny Smith peeled"},
				{"(10-12) apples, peeled -- Granny Smith", 	"10", "", "apples", "(10-12) Granny Smith peeled"},
				{"(10 to 12) apples, peeled -- Granny Smith","10", "", "apples", "(10 to 12) Granny Smith peeled"},
		};
		
		Configuration.setBooleanProperty("INGREDIENT_PROCESSING_WORDS_TO_PREPARATION", false);
		testIngredientLines(inputOutput);
		Configuration.setBooleanProperty("USE_WEIGHT_NOT_PACKAGE_SIZE", false);
		testIngredientLines(inputOutput);
	}

	@Test
	public void testWithAllOff() {
		Configuration.loadSafeCommaStructuresFromFile("commas.txt");
		String[][] inputOutput = new String[][] {
				{"1 (14-oz) tub sugar", 			"1", "(14-oz) tub", "sugar", ""},
				{"Powdered sugar, or Chocolate Glaze 1 (page 34), or frosting of your choice (optional)", "", "", "Powdered sugar", "or Chocolate Glaze 1 (page 34), or frosting of your choice (optional)"},
				{"2 Tbsp. olive oil","2", "Tbsp.", "olive oil", ""},
				{"1 (16.3-ounce) can refrigerated jumbo biscuits","1", "(16.3-ounce) can", "refrigerated jumbo biscuits", ""},
//				{"8 Tbsp. freshly grated Parmesan cheese","", "", "", ""},
//				{"4 tbsp. chopped fresh parsley","", "", "", ""},
///				{"","", "", "", ""},
//				1 (16.3-ounce) can refrigerated jumbo biscuits
//				
//				
//				
		};
		
		Configuration.setBooleanProperty("MERGE_INGREDIENT_LINE_CONTINUATIONS", false);
		Configuration.setBooleanProperty("SPLIT_INGREDIENT_LINES_WITH_PLUS", false);
		Configuration.setBooleanProperty("MOVE_PARENTHESIS_IN_INGREDIENTS_TO_PREPARATION", true);
		Configuration.setBooleanProperty("DETECT_AND_MARK_SERVINGS_FROM_YIELD", false);
		Configuration.setBooleanProperty("MOVE_NOTES_IN_DIRECTIONS_TO_NOTES", false);
		Configuration.setBooleanProperty("REMOVE_NON_RECIPE_COMMENTS_FROM_DIRECTIONS", false);
		Configuration.setBooleanProperty("REMOVE_NUTRITION_INFORMATION_FROM_DIRECTIONS", false);
		Configuration.setBooleanProperty("DETECT_AND_MARK_SERVINGS_FROM_DIRECTIONS", false);
		Configuration.setBooleanProperty("EXPAND_UNIT_ABBREVIATIONS", false);
		Configuration.setBooleanProperty("CALCULATE_WEIGHT_FROM_AMOUNT_TIMES_PACKAGE_SIZE", false);
		Configuration.setBooleanProperty("USE_WEIGHT_NOT_PACKAGE_SIZE", false);
		Configuration.setBooleanProperty("INGREDIENT_PROCESSING_WORDS_TO_PREPARATION", false);
		Configuration.setBooleanProperty("PLURALISE_UNITS", false);
		Configuration.setBooleanProperty("MOVE_TEXT_AFTER_COMMAS_TO_PREPARATION", true);
		Configuration.loadSafeCommaStructuresFromFile("commas.txt");

		testIngredientLines(inputOutput);
	}
	
	
	@Test
	public void testOrderOfPreparations() {
		Configuration.loadSafeCommaStructuresFromFile("commas.txt");
		String[][] inputOutput = new String[][] {
				{"10-12 apples (optional), peeled -- Granny Smith", 	"10", "", "apples", "(optional) (10-12) Granny Smith peeled"},
				{"10-12 apples (optional), peeled -- Granny Smith PLUS", 	"10", "", "apples", "(optional) (10-12) Granny Smith peeled PLUS"},
		};
		
		Configuration.setBooleanProperty("INGREDIENT_PROCESSING_WORDS_TO_PREPARATION", false);
		testIngredientLines(inputOutput);
	}

	@Test
	public void testWithNoAmountUnit() {
		String[][] inputOutput = new String[][] {
				{"salt and peber", 	"", "", "salt and peber", ""},
				{"- continouation from prev line.", 	"", "", "- continouation from prev line.", ""},
		};
		
		Configuration.setBooleanProperty("MARK_INGREDIENTS_WITH_NO_AMOUNT_OR_UNIT_AS_TEXT", true);
		RecipeIngredient ingr = null;
		for(int i=0; i<inputOutput.length;i++) {
			ingr = new RecipeIngredient(inputOutput[i][0]);
			ingr.normalize();
			assertEquals(RecipeIngredient.TYPE_TEXT, ingr.getType());
		}

		// test with config turned off.
		Configuration.setBooleanProperty("MARK_INGREDIENTS_WITH_NO_AMOUNT_OR_UNIT_AS_TEXT", false);
		for(int i=0; i<inputOutput.length;i++) {
			ingr = new RecipeIngredient(inputOutput[i][0]);
			ingr.normalize();
			assertEquals(RecipeIngredient.TYPE_INGREDIENT, ingr.getType());
		}
	}

	@Test
	public void testMalformedLines() {
		RecipeIngredient ingr = new RecipeIngredient(0f, "", "1/2 small onion");
		ingr.normalize();
		compareIngredient(ingr, 0.5f, "small", "onion", "");

		ingr = new RecipeIngredient(0.5f, "", "small onion");
		ingr.normalize();
		compareIngredient(ingr, 0.5f, "small", "onion", "");
	}
	
	@Test
	public void testMoveParenthesisOrNot() {
		String[][] inputOutput = new String[][] {
				{"10 apples (green), peeled -- Granny Smith", 	"10", "", "apples", "(green) Granny Smith peeled"},
		};
		
		Configuration.setBooleanProperty("MOVE_PARENTHESIS_IN_INGREDIENTS_TO_PREPARATION", true);
		testIngredientLines(inputOutput);

		inputOutput = new String[][] {
				{"10 apples (green), peeled -- Granny Smith", 	"10", "", "apples (green)", "Granny Smith peeled"},
		};
				
		Configuration.setBooleanProperty("MOVE_PARENTHESIS_IN_INGREDIENTS_TO_PREPARATION", false);
		testIngredientLines(inputOutput);
	}
	
	@Test
	public void testIngredientTypes() {
		assertEquals(RecipeIngredient.TYPE_INGREDIENT, new RecipeIngredient("1 tsp sugar").getType());
		assertEquals(RecipeIngredient.TYPE_SUBTITLE, new RecipeIngredient("S>1 tsp sugar").getType());
		assertEquals(RecipeIngredient.TYPE_RECIPE, new RecipeIngredient("R>1 tsp sugar").getType());
		assertEquals(RecipeIngredient.TYPE_TEXT, new RecipeIngredient("T>1 tsp sugar").getType());
	}
	
	@Test
	public void testWithMoveNothing() {
		Configuration.setBooleanProperty("MERGE_INGREDIENT_LINE_CONTINUATIONS", false);
		Configuration.setBooleanProperty("SPLIT_INGREDIENT_LINES_WITH_PLUS", false);
		Configuration.setBooleanProperty("MOVE_PARENTHESIS_IN_INGREDIENTS_TO_PREPARATION", false);
		Configuration.setBooleanProperty("DETECT_AND_MARK_SERVINGS_FROM_YIELD", false);
		Configuration.setBooleanProperty("MOVE_NOTES_IN_DIRECTIONS_TO_NOTES", false);
		Configuration.setBooleanProperty("REMOVE_NON_RECIPE_COMMENTS_FROM_DIRECTIONS", false);
		Configuration.setBooleanProperty("REMOVE_NUTRITION_INFORMATION_FROM_DIRECTIONS", false);
		Configuration.setBooleanProperty("DETECT_AND_MARK_SERVINGS_FROM_DIRECTIONS", false);
		Configuration.setBooleanProperty("EXPAND_UNIT_ABBREVIATIONS", false);
		Configuration.setBooleanProperty("CALCULATE_WEIGHT_FROM_AMOUNT_TIMES_PACKAGE_SIZE", false);
		Configuration.setBooleanProperty("USE_WEIGHT_NOT_PACKAGE_SIZE", false);
		Configuration.setBooleanProperty("INGREDIENT_PROCESSING_WORDS_TO_PREPARATION", false);
		Configuration.setBooleanProperty("PLURALISE_UNITS", false);
		Configuration.setBooleanProperty("MOVE_TEXT_AFTER_COMMAS_TO_PREPARATION", false);
		Configuration.setBooleanProperty("MOVE_TEXT_AFTER_SEMICOLON_TO_PREPARATION", false);
		Configuration.loadSafeCommaStructuresFromFile("commas.txt");

		String[][] inputOutput = new String[][] {
				{"1/4 cup whipping cream (optional but fantastic; or use thick Mexican", "0.25", "cup", "whipping cream (optional but fantastic; or use thick Mexican", ""},
//				{"", "", "", "", ""},
		};
		
		testIngredientLines(inputOutput);
	}

	@Test
	public void testCommaInParenthesis() {
		Configuration.setBooleanProperty("MOVE_PARENTHESIS_IN_INGREDIENTS_TO_PREPARATION", true);
		Configuration.setBooleanProperty("MOVE_TEXT_AFTER_COMMAS_TO_PREPARATION", true);

		String[][] inputOutput = new String[][] {
				{"2 kg beef (brisket, round, or sirloing)", 	"2", "kg", "beef", "(brisket, round, or sirloing)"},
		};
		
		testIngredientLines(inputOutput);

		
		// without moving Parens...
		inputOutput = new String[][] {
				{"2 kg beef (brisket, round, or sirloing)", 	"2", "kg", "beef (brisket, round, or sirloing)", ""},
		};
				
		Configuration.setBooleanProperty("MOVE_PARENTHESIS_IN_INGREDIENTS_TO_PREPARATION", false);
		Configuration.setBooleanProperty("MOVE_TEXT_AFTER_COMMAS_TO_PREPARATION", true);
		testIngredientLines(inputOutput);

		// without moving commas...
		inputOutput = new String[][] {
				{"2 kg beef (brisket, round, or sirloing)", 	"2", "kg", "beef", "(brisket, round, or sirloing)"},
		};
				
		Configuration.setBooleanProperty("MOVE_PARENTHESIS_IN_INGREDIENTS_TO_PREPARATION", true);
		Configuration.setBooleanProperty("MOVE_TEXT_AFTER_COMMAS_TO_PREPARATION", false);
		testIngredientLines(inputOutput);
	
	}

	@Test
	public void testSmallMedLarge() {
		// Test with false
		String[][] inputOutput = new String[][] {
				{"2 small onions", 	"2", "small", "onions", ""},
				{"2 medium onions", 	"2", "medium", "onions", ""},
				{"2 large onions", 	"2", "large", "onions", ""},
		};
		
		Configuration.setBooleanProperty("MOVE_SMALL_MED_LARGE_TO_INGREDIENTS", false);
		testIngredientLines(inputOutput);

		// Test with true
		inputOutput = new String[][] {
				{"2 small onions", 	"2", "", "small onions", ""},
				{"2 medium onions", 	"2", "", "medium onions", ""},
				{"2 large onions", 	"2", "", "large onions", ""},
				{"Small berries", 	"", "", "small berries", ""},
		};
		
		Configuration.setBooleanProperty("MOVE_SMALL_MED_LARGE_TO_INGREDIENTS", true);
		testIngredientLines(inputOutput);
	}

	@Test
	public void testDetectSubtitle() {
		String text = "subtitle:";
		Configuration.setBooleanProperty("DETECT_AND_MARK_SUBTITLES", false);
		RecipeIngredient r = new RecipeIngredient(text);
		assertEquals(RecipeIngredient.TYPE_INGREDIENT, r.getType());
		
		Configuration.setBooleanProperty("DETECT_AND_MARK_SUBTITLES", true);
		r = new RecipeIngredient(text);
		assertEquals(RecipeIngredient.TYPE_SUBTITLE, r.getType());
	}

	@Test
	public void testSubtitleFormatting() {
		Configuration.setIntProperty("SUBTITLE_CASE_PREFERENCE", 0);
		RecipeIngredient r = new RecipeIngredient("S> 1 pound loaf -- prepared");
		assertEquals(RecipeIngredient.TYPE_SUBTITLE, r.getType());
		assertTrue(r.hasNoAmount());
		assertTrue(r.hasNoUnit());
		assertTrue(r.hasNoProcessing());
		assertEquals("1 pound loaf -- prepared", r.getIngredient().getName());
		r.normalize();
		assertTrue(r.hasNoAmount());
		assertTrue(r.hasNoUnit());
		assertTrue(r.hasNoProcessing());
		assertEquals("1 pound loaf -- prepared", r.getIngredient().getName());
	}

	@Test
	public void testIngredientCapitalization() {
		String[][] inputOutput = new String[][] {
				{"2 dl water", 	"2", "dl", "water", ""},
				{"2 onions", 	"2", "", "onions", ""},
				{"large onion", 	"", "large", "onion", ""},
				// positive cases
				{"water", 	"", null, "Water", ""},
				{"s>water", 	"", null, "Water", null},
				{"t>water", 	"", null, "Water", null},
		};
		
		Configuration.setIntProperty("SUBTITLE_CASE_PREFERENCE", 0);
		Configuration.setBooleanProperty("CAPITALIZE_INGREDIENTS_WITHOUT_AMOUNT_UNIT", true);
		testIngredientLines(inputOutput);
		
		
		
	}
	
	@Test
	public void testFormattedIngredients() {
		String[][] inputOutput = new String[][] {
				// with line headers
				{"I><q:2><u:dl><i:water><p:cooked>", 	"2", "dl", "water", "cooked"},
				{"R><q:2><u:dl><i:water><p:cooked>", 	"2", "dl", "water", "cooked"},
				{"S><q:2><u:dl><i:water><p:cooked>", 	"2", "dl", "water", "cooked"},
				{"T><q:2><u:dl><i:water><p:cooked>", 	"2", "dl", "water", "cooked"},
				// normal
				{"<q:2 1/2>", 	"2.5", null, null, null},
				{"<q:2><i:water>", 	"2", null, "water", null},
				{"<q:2><i:water><p:cooked>", 	"2", null, "water", "cooked"},
				{"<q:2><u:dl><i:water><p:cooked>", 	"2", "dl", "water", "cooked"},
				{"<Q:2><U:dl><I:water><P:cooked>", 	"2", "dl", "water", "cooked"},
				{" <Q:2>  <U:dl>  <I:water>  <P:cooked>  ", 	"2", "dl", "water", "cooked"},
				{"<u:dl><i:water><p:cooked>", 	"0", "dl", "water", "cooked"},
				{"<i:water>", 	"", null, "water", null},
				{"  <i:water>", 	"", null, "water", null},
				{"<i:water>    ", 	"", null, "water", null},
				{"<u: dl >", 	"0", "dl", null, null},
				{"<i: ingr >", 	"0", null, "ingr", null},
				{"<p: ingr >", 	"0", null, null, "ingr"},
				
		};
		
		RecipeIngredient ingr = null;
		for(int i=0; i<inputOutput.length;i++) {
			ingr = new RecipeIngredient(inputOutput[i][0]);
			compareIngredient(ingr, inputOutput[i][1], inputOutput[i][2], inputOutput[i][3], inputOutput[i][4]);
		}

	}
	

	@Test
	public void testDoubleColons() {
		// Test with false
		String[][] inputOutput = new String[][] {
				{"For the cake::", 	"", "", "For the cake:", ""},
		};
		
        Configuration.clear();
		testIngredientLines(inputOutput);
	}
	
	
}



