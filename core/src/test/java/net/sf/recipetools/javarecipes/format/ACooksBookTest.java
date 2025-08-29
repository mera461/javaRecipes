/**
 * 
 */
package net.sf.recipetools.javarecipes.format;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import net.sf.recipetools.javarecipes.model.Category;
import net.sf.recipetools.javarecipes.model.Configuration;
import net.sf.recipetools.javarecipes.model.Folder;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeIngredient;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Frank
 *
 */
public class ACooksBookTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		Folder.clear();
		Configuration.setBooleanProperty("USE_FRACTIONS_IN_AMOUNTS", false);
	}
	
//	@Test
	public void testSimpleComplete() {
		ACooksBook reader = new ACooksBook(new File("src/test/data/ACooksBook/Test Recipe for Export.ACBK"));
		List<Recipe> recipes = reader.readAllRecipes();
		assertEquals(1, recipes.size());
		Recipe r = recipes.get(0);
		assertEquals("Test Recipe for Export", r.getTitle());
		List<Category> cats = r.getCategories();
		assertEquals(4, cats.size());
		assertEquals("Asian", cats.get(0).getName());
		assertEquals("Beef", cats.get(1).getName());
		assertEquals("Sauces", cats.get(2).getName());
		assertEquals("Course", cats.get(3).getName());
		assertEquals("The publication (11/8/2013), page 45", r.getTextAttribute("Publication"));
		assertEquals("The author", r.getAuthor());
		assertEquals(15, r.getPreparationTime());
		assertEquals(30, r.getCookTime());
		assertEquals(4, r.getServings());
		assertEquals("Hard", r.getTextAttribute("Difficulty"));
		assertTrue(r.getDirectionsAsString().startsWith("These are the detailed instructions"));
		assertTrue(r.getNote().startsWith("Here is a recipe note."));
		//ingredients
		List<RecipeIngredient> ingr = r.getIngredients();
		assertEquals(4, ingr.size());
		assertEquals("1 cup Brown sugar -- packed", ingr.get(0).toString());
		assertEquals("1 cup Flour -- sifted", ingr.get(1).toString());
		assertEquals("1 clove Garlic -- minced", ingr.get(2).toString());
		assertEquals("1 tablet Cinnamon -- ground", ingr.get(3).toString());
		// photo
		assertEquals(1, r.getImages().size());
		assertEquals("Good serving suggestion.", r.getServingIdeas());
		assertEquals("Good wine suggestion.", r.getWine());
		assertEquals("4 cups", r.getYield());
		assertEquals("Equipment,10-inch tube pan", r.getTextAttribute("Equipment"));
		assertEquals("The source of the recipe", r.getSource());
		assertEquals("This is the Recipe Description.", r.getDescription());
	}

	// @Test
	public void testBillsFile() {
		ACooksBook reader = new ACooksBook(new File("src/test/data/ACooksBook/Bill's Crowd Pleasers.ACBK"));
		List<Recipe> recipes = reader.readAllRecipes();
		assertEquals(1, recipes.size());
		// check UTF-8 numbers
		//assertEquals("1/4 cup milk", recipes.get(0).getIngredients().get(2).toString());
		
	}
	
	@Test
	public void testKankelsFile() {
		ACooksBook reader = new ACooksBook(new File("src/test/data/ACooksBook/Kankel Recipes S_to_Z.ACBK"));
		//List<Recipe> recipes = reader.readAllRecipes();
		//assertEquals(1, recipes.size());
		// check UTF-8 numbers
		//assertEquals("1/4 cup milk", recipes.get(0).getIngredients().get(2).toString());
		
	}
	
}
