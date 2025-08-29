/**
 * 
 */
package net.sf.recipetools.javarecipes.format;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import net.sf.recipetools.javarecipes.model.Configuration;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Frank
 *
 */
public class RezKonvExportTest {

	RezKonvExport mc = new RezKonvExport();
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
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

	@Test
	public void testBasic() {
		LineNumberReader bufReader = null;
		try {
			bufReader = new LineNumberReader(new InputStreamReader(new FileInputStream("src/test/data/rezkonv/BohnenSpeckPasta.rk"), mc.getDefaultCharacterSet()));
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new RecipeFoxException(e);
		}

		List<Recipe> recipes = null;
		recipes = mc.readRecipes(bufReader);
		assertNotNull(recipes);
		assertEquals(1, recipes.size());
		Recipe r = recipes.get(0);
		assertEquals("Bohnen-Speck-Pasta", r.getTitle());
		assertEquals(1,r.getCategories().size());
		assertEquals("Pasta",r.getCategories().get(0).getName());
		assertEquals("2 Portionen",r.getYield());
		assertEquals(12,r.getIngredients().size());
		assertEquals("essen&trinken Für jeden Tag, Heft 10/2011 Torsten Svensson",r.getSource());
		assertTrue(r.getDirectionsAsString().startsWith("1. Speck in"));
		assertEquals(6,r.getDirections().size());
	}


	@Test
	public void testMultiRecipes() {
		LineNumberReader bufReader = null;
		try {
			bufReader = new LineNumberReader(new InputStreamReader(new FileInputStream("src/test/data/rezkonv/BIOLEK4.RK"), mc.getDefaultCharacterSet()));
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new RecipeFoxException(e);
		}

		int count = 0;
		List<Recipe> recipes = mc.readRecipes(bufReader);
		List<Recipe> all = new ArrayList<Recipe>();
		while (recipes != null) {
			all.add(recipes.get(0));
			count++;
			recipes = mc.readRecipes(bufReader);
		}
		assertEquals(69, count);
		Recipe r = all.get(68);
		assertEquals("SEMMELKNÖDEL - ALFREDISSIMO", r.getTitle());
		assertEquals(3, r.getCategories().size());
	}
}
