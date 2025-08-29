/**
 * 
 */
package net.sf.recipetools.javarecipes.format;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

import net.sf.recipetools.javarecipes.model.Recipe;

import org.junit.Test;

import junit.framework.TestCase;


/**
 * @author Frank
 *
 */
public class RecipeProcessor2000FormatTest extends TestCase {
	
	private RecipeProcessor2000Format mm = null; 
	protected void setUp() throws Exception {
		super.setUp();
		mm = new RecipeProcessor2000Format();
	}
	
	@Test
	public void testReadingTheFirst() throws FileNotFoundException {
		LineNumberReader bufReader = null;
		bufReader = new LineNumberReader(new FileReader("src/test/data/rpw/fav.txt"));

		List<Recipe> recipes = null;
		recipes = mm.readRecipes(bufReader);
		assertNotNull(recipes);
		assertEquals(1, recipes.size());
		
		Recipe r = recipes.get(0);
		assertEquals("COUPON NET - HATTIE DELK", r.getSource());
		assertEquals(1, r.getServings());
		assertEquals(1, r.getCategories().size());
		assertEquals("CAKES", r.getCategories().get(0).getName());
		assertEquals(6, r.getIngredients().size());
		
		assertTrue(r.getDirectionsAsString().startsWith("Cream butter"));
		
	}

	@Test
	public void testReadingAll() throws FileNotFoundException {
		LineNumberReader bufReader = null;
		List<Recipe> recipes = new ArrayList<Recipe>();
		bufReader = new LineNumberReader(new FileReader("src/test/data/rpw/fav.txt"));
		int count = 0;
		List<Recipe> read = null;
		do {
			read = mm.readRecipes(bufReader);
			if (read != null) count++;
		} while (read != null);
		assertEquals(244, count);
	}
}
