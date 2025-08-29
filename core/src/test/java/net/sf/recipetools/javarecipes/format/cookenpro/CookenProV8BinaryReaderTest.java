/**
 * 
 */
package net.sf.recipetools.javarecipes.format.cookenpro;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;

import org.junit.Before;

import net.sf.recipetools.javarecipes.model.Configuration;
import net.sf.recipetools.javarecipes.model.Folder;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeIngredient;


/**
 * @author Frank
 *
 */
public class CookenProV8BinaryReaderTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		Folder.clear();
		Configuration.setBooleanProperty("USE_FRACTIONS_IN_AMOUNTS", false);
	}
	
	public void test1() {
		CookenProV8BinaryReader reader = new CookenProV8BinaryReader(new File("src/test/data/CookenProBinary/t1"));
		reader.describeTable("BOOKX");
		reader.describeTable("BRANDX");
		reader.describeTable("CATEGX");
		reader.describeTable("CATX");
		reader.describeTable("CHAPTERX");
		reader.describeTable("FOODX");
		reader.describeTable("RECIPE");
		reader.describeTable("UNITX");
	}

	public void listContentOfDatabase() {
		CookenProV8BinaryReader reader = new CookenProV8BinaryReader(new File("src/test/data/CookenProBinary/t1"));
		reader.listTable("BOOKX");
		
		reader.listTable("BRANDX");
		reader.listTable("CATEGX");
		reader.listTable("CATX");
		reader.listTable("CHAPTERX");
		reader.listTable("FOODX");
		reader.listTable("UNITX");
		reader.listTable("RECIPE8X");
	}

	//@Test
	public void testReadSimpleRecipe() {
		CookenProV8BinaryReader reader = new CookenProV8BinaryReader(new File("src/test/data/CookenProBinary/t1"));
		List<Recipe> recipes = reader.readAllRecipes();
		assertEquals(1, recipes.size());
		Recipe r = recipes.get(0);
		assertEquals("Brennah's Fabulous Apple Dumplings", r.getTitle());
		assertEquals(16, r.getServings());
		assertEquals("", r.getYield());
		assertEquals(0, r.getPreparationTime());
		assertEquals("", r.getNote());
		assertTrue(r.getDirectionsAsString().startsWith("Take each piece"));
		assertEquals(7, r.getIngredients().size());
		RecipeIngredient ingr = r.getIngredients().get(0);
		assertEquals(3.0, ingr.getAmount(), 0.01f);
		assertTrue(ingr.hasNoUnit());
		assertEquals("Yellow Delicious apples", ingr.getIngredient().getName());
		assertEquals("peeled, cored, and diced", ingr.getProcessing());
		// check units
		assertEquals("cup", r.getIngredients().get(2).getUnit().getName());
		// chapter
		assertEquals("Desserts", r.getFolder().getName());
		assertEquals("DVO.COM Recipe Collection", r.getFolder().getRoot().getName());
	}

	//@Test
	public void testVersion65() {
		CookenProV8BinaryReader reader = new CookenProV8BinaryReader(new File("src/test/data/CookenProBinary/t2"));
		//reader.describeTable("RECIPE");
		reader.listTable("RECIPE");
		List<Recipe> recipes = reader.readAllRecipes();
		assertEquals(5, recipes.size());
		Recipe r = recipes.get(0);
		assertEquals("Caramel Apple Pie", r.getTitle());
		assertEquals(8, r.getServings());
		assertEquals("Preheat oven to 375 degrees F (190 degrees C)", r.getNote());
		assertTrue(r.getDirectionsAsString().startsWith("To Make Taffy"));
		assertEquals(11, r.getIngredients().size());
		RecipeIngredient ingr = r.getIngredients().get(0);
		assertEquals(1.0, ingr.getAmount(), 0.01f);
		assertTrue(ingr.hasNoUnit());
		assertEquals("recipe pastry for a 9 inch", ingr.getIngredient().getName());
		assertEquals("double crust deep dish pie", ingr.getProcessing());
		// check units
		assertEquals("0.5 cup packed brown sugar", r.getIngredients().get(1).toString());
		assertEquals("0.25 cup butter -- melted", r.getIngredients().get(2).toString());
		assertEquals("0.3333 cup all-purpose flour", r.getIngredients().get(3).toString());
		// [5 cup thinly sliced Granny Smith apples, 0.6667 cup white sugar, 3 tablespoon all-purpose flour, 2 teaspoon ground cinnamon, 1 teaspoon lemon juice, 15 caramels -- halved, 2 tablespoon milk]
		// chapter
		assertEquals("Desserts & Pies", r.getFolder().getName());
		assertEquals("DVO.COM Recipe Collection", r.getFolder().getRoot().getName());
	}

	//@Test
	public void testReadingDVOfile() {
		CookenProV8BinaryReader reader = new CookenProV8BinaryReader(new File("src/test/data/CookenProBinary/caramel_apple_pie.dvo"));
		List<Recipe> recipes = reader.readAllRecipes();
		assertEquals(5, recipes.size());
	}

	//@Test
	public void testUnpack() {
		File f = new File("src/test/data/CookenProBinary/c1-witherror.dvo");
		try {
			CookenProV8BinaryReader reader = new CookenProV8BinaryReader();
			reader.unpackDvo(f, new File("src/test/data/CookenProBinary/t3"));
			fail("expected an error from ARQ");
		} catch (RuntimeException e) {
			assertTrue(e.getMessage().contains("CRC-32 error"));
		}
	}

	//@Test
	public void testFilesWithProblems() {
		CookenProV8BinaryReader reader = new CookenProV8BinaryReader(new File("src/test/data/CookenProBinary/Cook'n Crepes.dvo"));
		List<Recipe> recipes = reader.readAllRecipes();
		assertEquals(143, recipes.size());
	}
	
	//@Test
	public void testWithImages() {
		CookenProV8BinaryReader reader = new CookenProV8BinaryReader(new File("src/test/data/CookenProBinary/Cook'n Lite & Healthy/Cook'n Lite & Healthy.dvo"));
		List<Recipe> recipes = reader.readAllRecipes();
		assertEquals(292, recipes.size());
		assertEquals(1, recipes.get(4).getImages().size());
	}
	
}
