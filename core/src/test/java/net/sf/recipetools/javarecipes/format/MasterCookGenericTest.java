/**
 * 
 */
package net.sf.recipetools.javarecipes.format;

import net.sf.recipetools.javarecipes.model.Recipe;

import java.util.List;

import junit.framework.TestCase;

/**
 * @author Frank
 *
 */
public class MasterCookGenericTest extends TestCase {
	private MasterCookGeneric mc = new MasterCookGeneric();
	
	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		mc = null;
	}

	public void testSimple() {
		String lines="\nline2\n@@@@@\ntitle\n\n1 tsp sugar\n\ndirections\n-----\n";
		List<Recipe> recipes = mc.readRecipes(lines);
		assertEquals(1, recipes.size());
		assertEquals("title", recipes.get(0).getTitle());
		assertEquals(1, recipes.get(0).getIngredients().size());
		assertEquals("directions", recipes.get(0).getDirectionsAsString());
	}
	
	public void testWithExtraNewlines() {
		String lines="\nline2\n@@@@@\n\n\ntitle\n\n\n\n1 tsp sugar\n\n\n\ndirections\n\n\n-----\n";
		List<Recipe> recipes = mc.readRecipes(lines);
		assertEquals(1, recipes.size());
		assertEquals("title", recipes.get(0).getTitle());
		assertEquals(1, recipes.get(0).getIngredients().size());
		assertEquals("directions", recipes.get(0).getDirectionsAsString());
	}
	
	public void testWithNoBlankLines() {
		String lines="\nline2\n@@@@@\ntitle\n1 tsp sugar\n\ndirections";
		List<Recipe> recipes = mc.readRecipes(lines);
		assertEquals(1, recipes.size());
		assertEquals("title", recipes.get(0).getTitle());
		assertEquals(1, recipes.get(0).getIngredients().size());
		assertEquals("directions", recipes.get(0).getDirectionsAsString());
	}
	
	public void testWithDirParagraphs() {
		String lines="@@@@@\ntitle\n1 tsp sugar\n\ndir1\ndir2\n\ndir3";
		List<Recipe> recipes = mc.readRecipes(lines);
		assertEquals(1, recipes.size());
		assertEquals("title", recipes.get(0).getTitle());
		assertEquals(1, recipes.get(0).getIngredients().size());
		assertEquals(2, recipes.get(0).getDirections().size());
		assertEquals("dir1\ndir2", recipes.get(0).getDirections().get(0));
		assertEquals("dir3", recipes.get(0).getDirections().get(1));
	}
	
	
}
