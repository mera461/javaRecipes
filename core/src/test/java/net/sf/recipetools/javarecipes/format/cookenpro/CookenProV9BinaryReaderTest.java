/**
 * 
 */
package net.sf.recipetools.javarecipes.format.cookenpro;


import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import net.sf.recipetools.javarecipes.format.LivingCookbookFDX;
import net.sf.recipetools.javarecipes.format.MasterCookArchive;
import net.sf.recipetools.javarecipes.model.Configuration;
import net.sf.recipetools.javarecipes.model.Folder;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;
import net.sf.recipetools.javarecipes.model.RecipeIngredient;

import org.junit.Before;
import org.junit.Test;


/**
 * @author Frank
 *
 */
public class CookenProV9BinaryReaderTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		Folder.clear();
		Configuration.clear();
		Configuration.setBooleanProperty("USE_FRACTIONS_IN_AMOUNTS", false);
		Configuration.setBooleanProperty("PLURALISE_UNITS", true);
        Configuration.setBooleanProperty("EXPAND_UNIT_ABBREVIATIONS", true);
        Configuration.setBooleanProperty("USE_WEIGHT_NOT_PACKAGE_SIZE", false);
	}
	
	@Test
	public void testReadSimpleRecipe() {
		Configuration.setBooleanProperty("PLURALISE_UNITS", true);
		Configuration.setBooleanProperty("EXPAND_UNIT_ABBREVIATIONS", true);
		CookenProV9BinaryReader reader = new CookenProV9BinaryReader(new File("src/test/data/CookenProV9Binary/t.dvo"));
		List<Recipe> recipes = reader.readAllRecipes();
		assertEquals(367, recipes.size());

		Recipe r = recipes.get(213);
		assertThat(r.getId(), is(400259L));
		assertEquals("Riverside Butter Cake", r.getTitle());
		assertEquals(16, r.getServings());
		assertEquals("", r.getYield());
		assertEquals(0, r.getPreparationTime());
		assertEquals("", r.getNote());
		assertTrue(r.getDirectionsAsString().startsWith("Preheat oven to"));
		assertEquals(13, r.getIngredients().size());
		RecipeIngredient ingr = r.getIngredients().get(0);
		assertEquals(1.5f, ingr.getAmount(), 0.01f);
		assertEquals("cup", ingr.getUnit().getName());
		assertEquals("butter", ingr.getIngredient().getName());
		assertEquals("or margarine, softened", ingr.getProcessing());
		// chapter
		assertEquals("Cook'n Deluxe", r.getFolder().getName());
		assertEquals("The DVO.com Cookbook", r.getFolder().getRoot().getName());
		// images
		assertEquals(1, recipes.get(3).getImages().size());
	}

	@Test
	public void testNewerFormatWithoutMediaUsageTable() {
		CookenProV9BinaryReader reader = new CookenProV9BinaryReader(new File("src/test/data/CookenProV9Binary/testing.dvo"));
		List<Recipe> recipes = reader.readAllRecipes();
		assertEquals(15, recipes.size());

		Recipe r = recipes.get(11);
		assertEquals("Smoked Cheddar Waldorf Salad", r.getTitle());
		assertEquals(1, r.getImages().size());
		
		//
		reader = new CookenProV9BinaryReader(new File("src/test/data/CookenProV9Binary/Sides.dvo"));
		recipes = reader.readAllRecipes();
		assertEquals(21, recipes.size());
		//
		reader = new CookenProV9BinaryReader(new File("src/test/data/CookenProV9Binary/Main dishes.dvo"));
		recipes = reader.readAllRecipes();
		assertEquals(31, recipes.size());
	}

	@Test
	public void testNullIngredientsAndIngredientOrder() {
		CookenProV9BinaryReader reader = new CookenProV9BinaryReader(new File("src/test/data/CookenProV9Binary/ingr-test.dvo"));
		List<Recipe> recipes = reader.readAllRecipes();
		assertEquals(670, recipes.size());
		
		Recipe r = recipes.get(286);
		assertEquals("Acorn Squash Feta Casserole Recipe", r.getTitle());
		Configuration.setBooleanProperty("PLURALISE_UNITS", true);
        Configuration.setBooleanProperty("USE_WEIGHT_NOT_PACKAGE_SIZE", false);
        //Configuration.setBooleanProperty("EXPAND_UNIT_ABBREVIATIONS", true);
        System.out.println(Configuration.dump());
		
		List<RecipeIngredient> ingr = r.getIngredients();
		assertEquals(13, ingr.size());
		assertEquals("2 large acorn squash -- (about 1-1/ pounds each)", ingr.get(0).toString());
		assertEquals("1 medium onion -- chopped", ingr.get(1).toString());
		assertEquals("2 cloves garlic -- minced", ingr.get(2).toString());
		assertEquals("3 tablespoons unsalted butter", ingr.get(3).toString());
		assertEquals("0.5 cup green pepper -- chopped", ingr.get(4).toString());
		assertEquals("0.5 cup sweet red pepper -- chopped", ingr.get(5).toString());
		assertEquals("2 eggs", ingr.get(6).toString());
		assertEquals("1 (8-ounce) container plain yogurt", ingr.get(7).toString());
		assertEquals("1 (4-ounce) container crumbled feta cheese", ingr.get(8).toString());
	}
	
	@Test
	public void testChapters() {
		Folder.clear();
		CookenProV9BinaryReader reader = new CookenProV9BinaryReader(new File("src/test/data/CookenProV9Binary/ingr-test.dvo"));
		List<Recipe> recipes = reader.readAllRecipes();
		assertEquals(670, recipes.size());
		
		// check chapters
		List<Folder> folders = Folder.getAll();
		assertEquals(25, folders.size());
		// cookbook
		assertEquals("Dave's Cookbook", folders.get(0).getName());
		// chapters
		assertEquals("Eggs, Coffee Cakes and Breakfast", folders.get(1).getName());
		assertEquals("Frostings, and Sweet Fillings, Sauces and Toppings", folders.get(2).getName());
	}

	@Test
	public void testCookbookWithImages() {
		Folder.clear();
		CookenProV9BinaryReader reader = new CookenProV9BinaryReader(new File("src/test/data/CookenProV9Binary/Webers Big Book of Grilling.dvo"));
		List<Recipe> recipes = reader.readAllRecipes();
		assertEquals(487, recipes.size());
		
		reader = new CookenProV9BinaryReader(new File("src/test/data/CookenProV9Binary/Thousand Recipe Chinese Cookbook.dvo"));
		recipes = reader.readAllRecipes();
		assertEquals(1407, recipes.size());
	}
	
	
	
	@Test
	public void convertToFdx() {
		CookenProV9BinaryReader reader = new CookenProV9BinaryReader(new File("src/test/data/CookenProV9Binary/ingr-test.dvo"));
		List<Recipe> recipes = reader.readAllRecipes();
		LivingCookbookFDX writer = new LivingCookbookFDX();
		try {
			PrintWriter out = new PrintWriter("C:\\temp\\convert\\cookenProV9.fdx", "UTF-8");
			writer.writeFileHeader(out);
			writer.writeRecipe(out, recipes);
			writer.writeFileTail(out);
			out.close();
		} catch (FileNotFoundException e) {
            throw new RecipeFoxException(e);
		} catch (UnsupportedEncodingException e) {
            throw new RecipeFoxException(e);
		} 
		MasterCookArchive mca = new MasterCookArchive();
		mca.startFile(new File("C:\\temp\\convert\\cookenProV9.mz2"));
		mca.write(recipes);
		mca.endFile();
	}

	@Test
	public void testFilesWithProblems() {
		Folder.clear();
		CookenProV9BinaryReader reader = new CookenProV9BinaryReader(new File("src/test/data/CookenProV9Binary/Dr. OZ.DVO"));
		List<Recipe> recipes = reader.readAllRecipes();
		assertEquals(8, recipes.size());
		
		reader = new CookenProV9BinaryReader(new File("src/test/data/CookenProV9Binary/NOURISHING TRADITIONS.DVO"));
		recipes = reader.readAllRecipes();
		assertEquals(28, recipes.size());

		reader = new CookenProV9BinaryReader(new File("src/test/data/CookenProV9Binary/Mary Ledoux - BREAKFAST.DVO"));
		recipes = reader.readAllRecipes();
		assertEquals(147, recipes.size());
	}
	
	
}
