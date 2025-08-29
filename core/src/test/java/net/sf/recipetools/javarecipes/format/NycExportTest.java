/**
 * 
 */
package net.sf.recipetools.javarecipes.format;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.List;

import junit.framework.TestCase;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;
import net.sf.recipetools.javarecipes.model.RecipeIngredient;


/**
 * @author Frank
 *
 */
public class NycExportTest extends TestCase {
	/*
	 * @see TestCase#setUp()
	 */
	private NycExport mm = null; 
	protected void setUp() throws Exception {
		super.setUp();
		mm = new NycExport();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		mm = null;
	}

	public void testReadRecipe1() {
		LineNumberReader bufReader = null;
		try {
			bufReader = new LineNumberReader(new FileReader("src/test/data/nycexport/cake.txt"));
		} catch (FileNotFoundException e) {
            throw new RecipeFoxException(e);
		}

		Recipe recipe = null;
		recipe = mm.readRecipes(bufReader).get(0);
		assertTrue(recipe != null);
		assertEquals("14-CARAT CAKE WITH VANILLA CREAM CHEESE FROST", recipe.getTitle());
		assertEquals("cakes", recipe.getCategories().get(0).getName());
		assertEquals("deserts", recipe.getCategories().get(1).getName());
		assertEquals("12 servings", recipe.getYield());
		RecipeIngredient ingr = recipe.getIngredients().get(0); 
		assertEquals("flour", ingr.getIngredient().getName());
		assertEquals(2, ingr.getAmount(), 0.01);
		assertEquals("cup", ingr.getUnit().getName());
		assertTrue(recipe.getDirectionsAsString().startsWith("Sift together flour,"));
		assertTrue(recipe.getDirectionsAsString().endsWith("of sheet cake.\n"));
	}

	public void testReadRecipeCount() {
		LineNumberReader bufReader = null;
		try {
			bufReader = new LineNumberReader(new FileReader("src/test/data/nycexport/cake.txt"));
		} catch (FileNotFoundException e) {
            throw new RecipeFoxException(e);
		}
		
		
		int count = 0;
		long start = System.currentTimeMillis();
		List<Recipe> recipes = mm.readRecipes(bufReader);
		while (recipes!= null && recipes.size() != 0) {
			count+=recipes.size();
			recipes = mm.readRecipes(bufReader);
		}
		assertEquals(897, count);
		long time = System.currentTimeMillis() - start;
		System.out.println("Read 1000 recipes in "+(time/1000.0)+ " seconds");
	}

}
