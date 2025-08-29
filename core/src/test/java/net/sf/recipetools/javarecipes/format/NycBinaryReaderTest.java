/**
 * 
 */
package net.sf.recipetools.javarecipes.format;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import net.sf.recipetools.javarecipes.fileprocessor.BinaryInputProcessor;
import net.sf.recipetools.javarecipes.fileprocessor.FileProcessor;
import net.sf.recipetools.javarecipes.fileprocessor.OutputProcessor;
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
public class NycBinaryReaderTest {

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
		NycBinaryReader reader = new NycBinaryReader(new File("src/test/data/nycBinary/12cook02.gcf"));
		List<Recipe> recipes = reader.readAllRecipes();
		assertEquals(48, recipes.size());
		// check title on first and last
		Recipe r = recipes.get(0);
		assertEquals("2000 01 GIANT’S THUMBPRINT BUTTER COOKIES", r.getTitle());
		assertEquals("Gale Gand", r.getNote());
		assertEquals("18 large cookie", r.getYield());
		assertTrue(r.getDirectionsAsString().startsWith("Preheat the oven to 350"));
		assertTrue(r.getDirectionsAsString().endsWith("Store in an airtight container."));
		assertEquals("cookies; desserts", r.getCategoriesAsString());
		List<RecipeIngredient> ingrs = r.getIngredients();
		assertEquals(6, ingrs.size());
		assertEquals("8 oz unsalted butter -- softened, room temperatur", ingrs.get(0).toString());
		assertEquals("0.6667 cup sugar", ingrs.get(1).toString());
		assertEquals("0.25 ea vanilla bean, halved lengthwise -- soft insides scraped out", ingrs.get(2).toString());
		assertEquals("0.125 teaspoon salt", ingrs.get(3).toString());
		
		// check cookbook
		assertEquals("Food TV's 12 Days of Cookies Recipes by YEAR", r.getFolder().getName());
		
		// check photo
		assertEquals(1, r.getImages().size());
		assertEquals("2000 01 GIANT’S THUMBPRINT BUTTER COOKIES.jpg", r.getImages().get(0).getName());
		// the last one with photo
		assertEquals("2003 01 Almond Snowball Cookies", recipes.get(47).getTitle());
		assertEquals(1, recipes.get(47).getImages().size());
		assertEquals("2003 01 Almond Snowball Cookies.jpg", recipes.get(47).getImages().get(0).getName());
		
		// check times
		assertEquals("2000 02 GALE’S FAMOUS TRUFFLES", recipes.get(1).getTitle());
		assertEquals(13*60, recipes.get(1).getPreparationTime());
		
		// check accented chars
		assertEquals("crème fraîche", recipes.get(1).getIngredients().get(0).getIngredient().getName());
		
		// test that normalize them don't throw exception 
		for (Recipe r1: recipes) {
			r1.normalize();
		}
	}

	@Test
	public void testZipFile() {
		NycBinaryReader reader = null;
		List<Recipe> recipes = null;
		reader = new NycBinaryReader(new File("src/test/data/nycBinary/casseroles.zip"));
		recipes = reader.readAllRecipes();
		assertEquals(32, recipes.size());
		assertEquals("Chicken With Basil", recipes.get(0).getTitle());
		assertEquals(11, recipes.get(0).getIngredients().size());
		assertEquals("4 tablespoons chopped chiles", recipes.get(0).getIngredients().get(0).toString());
		assertEquals("2 pounds chicken breast, no skin, no bone, R -- cut in 1/4\" strips", recipes.get(0).getIngredients().get(10).toString());
		
		reader = new NycBinaryReader(new File("src/test/data/nycBinary/entrees beef.zip"));
		recipes = reader.readAllRecipes();
		assertEquals(73, recipes.size());
		reader = new NycBinaryReader(new File("src/test/data/nycBinary/entrees pork.zip"));
		recipes = reader.readAllRecipes();
		assertEquals(60, recipes.size());
		reader = new NycBinaryReader(new File("src/test/data/nycBinary/entrees poultry.zip"));
		recipes = reader.readAllRecipes();
		assertEquals(71, recipes.size());
		reader = new NycBinaryReader(new File("src/test/data/nycBinary/mexican.zip"));
		recipes = reader.readAllRecipes();
		assertEquals(3843, recipes.size());
	}
	
	@Test
	public void testWithOnlyTheGCFfile() {
		NycBinaryReader reader = new NycBinaryReader(new File("src/test/data/nycBinary/bonaire.gcf"));
		List<Recipe> recipes = reader.readAllRecipes();
		assertEquals(26, recipes.size());
		reader = new NycBinaryReader(new File("src/test/data/nycBinary/casseroles.gcf"));
		recipes = reader.readAllRecipes();
		assertEquals(32, recipes.size());
	}
	
	
	@Test
	public void testConvert() {
		if (false)
			return;
		NycBinaryReader reader = new NycBinaryReader(new File("src/test/data/nycBinary/12cook02.gcf"));
		List<Recipe> recipes = reader.readAllRecipes();
		MasterCookArchive writer = new MasterCookArchive();
		writer.setTheMasterCookProgram(new File("C:\\Program Files\\MasterCook 14\\Program\\Mastercook14.exe"));
		writer.startFile(new File("c:/temp/12cook02.mz2"));
		writer.write(recipes);
		writer.endFile();
	}
	
	// @Test
	public void testAllMyFiles() {
		if (true)
			return;
		final NycBinaryReader reader = new NycBinaryReader();
		BinaryInputProcessor ip = new BinaryInputProcessor() {
			public List<Recipe> read(File f) {
				System.out.println("Reading from: " + f.getName());
				if (f.getName().endsWith("zip") || f.getName().endsWith("gcf")) {
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
		fileProcessor.process("Y:/data/opskrifter/NowYouAreCooking - Binary");
	}
	
}
