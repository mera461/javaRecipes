/**
 * 
 */
package net.sf.recipetools.javarecipes.format;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Test;

import net.sf.recipetools.javarecipes.model.Configuration;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeIngredient;


/**
 * @author Frank
 *
 */
public class PaprikaBinaryReaderTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Test
	public void testReadSimpleRecipe() {
		Configuration.setBooleanProperty("USE_FRACTIONS_IN_AMOUNTS", true);
		
		PaprikaBinaryReader reader = new PaprikaBinaryReader(new File("src/test/data/paprika/Beef Stir-Fry with Avocado Salad.paprikarecipes"));
		List<Recipe> recipes = reader.readAllRecipes();
		assertEquals(1, recipes.size());
		// check title on first and last
		Recipe r = recipes.get(0);
		assertEquals("Beef Stir-Fry with Avocado Salad", r.getTitle());
		assertEquals(4, r.getServings());
		assertTrue(r.getDirectionsAsString().contains("In a bowl, combine beef"));
		assertEquals("beans; beef; cheese", r.getCategoriesAsString()); // main ingr
		List<RecipeIngredient> ingrs = r.getIngredients();
		assertEquals(14, ingrs.size());
		assertEquals("12 ounces beef tenderloin, cut into thin strips", ingrs.get(0).toString());
		assertEquals("1/4 cup freshly squeezed lime juice", ingrs.get(1).toString());
		assertEquals("1 tablespoon plus 1/2 teaspoon chili powder", ingrs.get(2).toString());

		assertEquals("good for summer dinner", r.getNote());
		assertEquals("per serving: 436 calories, 28g protein, 30g carbohydrate, 26g fat (7g saturated), 10g fiber", r.getNutritionalInfo());
		assertEquals(10, r.getPreparationTime());
		assertEquals(10, r.getCookTime());
		assertEquals("Fitnessmagazine.com",r.getSource());
		assertEquals("http://www.fitnessmagazine.com/recipes/dinner/dinner-in-20-easy-healthy-dinner-recipes/",r.getUrl());
		assertEquals(1, r.getImages().size());
		assertEquals(3, r.getRating("Rating", 5));
		
		// test normalize them
		for (Recipe r1: recipes) {
			r1.normalize();
		}
	}


	@Test
	public void testMultipleRecipes() {
		Configuration.setBooleanProperty("USE_FRACTIONS_IN_AMOUNTS", true);
		
		PaprikaBinaryReader reader = new PaprikaBinaryReader(new File("src/test/data/paprika/Augustine_All Recipes.paprikarecipes"));
		List<Recipe> recipes = reader.readAllRecipes();
		assertEquals(619, recipes.size());
		
		recipes = reader.read(new File("src/test/data/paprika/Grilled Chicken with Arugula and Warm Chickpeas.paprikarecipes"));
		assertEquals(1, recipes.size());
		recipes = reader.read(new File("src/test/data/paprika/Instant Pot Tomato Soup with Roasted Tomatoes.paprikarecipes"));
		assertEquals(1, recipes.size());
		
	}


	@Test
	public void testWithNewAttributes() {
		Configuration.setBooleanProperty("USE_FRACTIONS_IN_AMOUNTS", true);
		
		PaprikaBinaryReader reader = new PaprikaBinaryReader(new File("src/test/data/paprika/Export 2018-03-10 16.34.04 All Recipes.paprikarecipes"));
		List<Recipe> recipes = reader.readAllRecipes();
		assertEquals(1008, recipes.size());
	}
}
