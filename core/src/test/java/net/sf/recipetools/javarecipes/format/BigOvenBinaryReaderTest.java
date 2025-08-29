/**
 * 
 */
package net.sf.recipetools.javarecipes.format;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import net.sf.recipetools.javarecipes.fileprocessor.BinaryInputProcessor;
import net.sf.recipetools.javarecipes.fileprocessor.FileProcessor;
import net.sf.recipetools.javarecipes.fileprocessor.OutputProcessor;
import net.sf.recipetools.javarecipes.model.Configuration;
import net.sf.recipetools.javarecipes.model.Folder;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeIngredient;


/**
 * @author Frank
 *
 */
public class BigOvenBinaryReaderTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		Folder.clear();
		Configuration.setBooleanProperty("USE_FRACTIONS_IN_AMOUNTS", false);
		Configuration.setBooleanProperty("PLURALISE_UNITS", true);
	}
	
	@Test
	public void testReadSimpleRecipe() {
		BigOvenBinaryReader reader = new BigOvenBinaryReader(new File("src/test/data/BigOven/Appetizers.crb"));
		List<Recipe> recipes = reader.readAllRecipes();
		assertEquals(1690, recipes.size());
		// check title on first and last
		Recipe r = recipes.get(0);
		assertEquals("Cheese and Mushroom En Croute", r.getTitle());
		assertEquals("Zucchini-And-Basil Filo", recipes.get(1689).getTitle());
		assertEquals("10 Servings", r.getYield());
		assertTrue(r.getDirectionsAsString().contains("Unroll cresent rolls. Place"));
		assertTrue(r.getDirectionsAsString().contains("by dandelion@edeneast.com on Mar 2, 1998"));
		assertEquals("Mushrooms", r.getCategoriesAsString()); // main ingr
		assertEquals("Uncategorized", r.getCuisine());
		List<RecipeIngredient> ingrs = r.getIngredients();
		assertEquals(4, ingrs.size());
		assertEquals("1 8-count can cresent rolls", ingrs.get(0).toString());
		assertEquals("2 ts Dried minced onion", ingrs.get(1).toString());
		assertEquals("1 4-oz can sliced mushrooms;", ingrs.get(2).toString());
		assertEquals("1 12-oz round Gouda cheese", ingrs.get(3).toString());
		
		// check source
		assertEquals("A Bundle of Brie", recipes.get(934).getTitle());
		assertEquals("Canadian Living Magazine", recipes.get(934).getSource());
		
		// check times
		assertEquals("Spring rolls (3 points per serving)", recipes.get(1475).getTitle());
		assertEquals(20, recipes.get(1475).getPreparationTime());
		assertEquals(20, recipes.get(1475).getTotalTime());
		
		// TODO: check categories
		
		// test normalize them
		for (Recipe r1: recipes) {
			r1.normalize();
		}
	}

	@Test
	public void testZipFile() {
		BigOvenBinaryReader reader = new BigOvenBinaryReader(new File("src/test/data/BigOven/Salads.zip"));
		List<Recipe> recipes = reader.readAllRecipes();
		assertEquals(2628, recipes.size());
		// check title on first and last
		Recipe r = recipes.get(0);
		assertEquals("Cheese and Ham Tabbouleh Salad", r.getTitle());
		assertEquals("Zuppa Matta (Bread Salad with Tomatoes, Fenne", recipes.get(2627).getTitle());
		
		// check photo
		int id = 1722;
		assertEquals("Reduced Fat Vegetarian Taco Salad", recipes.get(id).getTitle());
		assertEquals(1, recipes.get(id).getImages().size());

		// check rating
		id = 1985;
		assertEquals("Caesar Salad ala Steve", recipes.get(id).getTitle());
		assertEquals(5, recipes.get(id).getRating("TasteRating", 5));
		assertEquals(2, recipes.get(id).getRating("AppearanceRating"),5);
		assertEquals(3, recipes.get(id).getRating("EffortRating"),5);
		assertEquals(2, recipes.get(id).getRating("AffordableRating"),5);
	}
	
	@Test
	public void testOtherFormat1() {
		// format without the categories...
		BigOvenBinaryReader reader = new BigOvenBinaryReader(new File("src/test/data/BigOven/johnson2.crb"));
		List<Recipe> recipes = reader.readAllRecipes();
		assertEquals(153, recipes.size());
		// test normalize them
		for (Recipe r: recipes) {
			r.normalize();
		}
		// other format with problems
		reader = new BigOvenBinaryReader(new File("Y:/data/opskrifter/BigOven - Binary/standard.crb"));
		recipes = reader.readAllRecipes();
		assertEquals(2000, recipes.size());
	}

	// @Test
	public void testAllMyFiles() {
		if (true)
			return;
		final BigOvenBinaryReader reader = new BigOvenBinaryReader();
		BinaryInputProcessor ip = new BinaryInputProcessor() {
			public List<Recipe> read(File f) {
				System.out.println("Reading from: " + f.getName());
				if (f.getName().endsWith("zip") || f.getName().endsWith("crb")) {
					return reader.read(f);
				} else {
					return null;
				}
			}
		};
		OutputProcessor op = new OutputProcessor() {
			public void write(List<Recipe> recipes) {
				// test normalize them
				for (Recipe r: recipes) {
					r.normalize();
				}
			}

			public void startFile(String name) {
				startFile(new File(name));
			}
			public void startFile(File f) {
				System.out.println("Reading from: " + f.getName());
			}
			public void endFile(){}
		};
		FileProcessor fileProcessor = new FileProcessor(ip, op);
		fileProcessor.process("Y:/data/opskrifter/BigOven - Binary");
	}
	
}
