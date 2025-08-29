/*
 * Created on 26-10-2004
 *
 */
package net.sf.recipetools.javarecipes.format;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import net.sf.recipetools.javarecipes.fileprocessor.BinaryInputProcessor;
import net.sf.recipetools.javarecipes.fileprocessor.FileProcessor;
import net.sf.recipetools.javarecipes.fileprocessor.OutputProcessor;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;
import net.sf.recipetools.javarecipes.model.RecipeIngredient;

/**
 * @author ft
 *
 */
public class MacGourmetXmlTest {

	/*
	 * @see TestCase#setUp()
	 */
	private MacGourmetXml mxml = null;
	int count = 0;
	
	@Before
	public void setUp() throws Exception {
		mxml = new MacGourmetXml();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	@After
	public void tearDown() throws Exception {
		mxml = null;
	}
	
	List<Recipe> readFromFile(String filename) {
		LineNumberReader bufReader = null;
		try {
			bufReader = new LineNumberReader(new InputStreamReader(new FileInputStream(new File(filename)), "UTF-8"));
		} catch (IOException e) {
            throw new RecipeFoxException(e);
		}
		
		return mxml.readRecipes(bufReader);
	}

	@Test
	public void testReadRecipe1() {
		List<Recipe> all = readFromFile("src/test/data/MacGourmet/Banana Chocolate Chip Bread.mgourmet");
		assertEquals(1, all.size());
		Recipe recipe = all.get(0);
		assertTrue(recipe != null);
		assertEquals(3, recipe.getCategories().size());
		assertEquals("Breads", recipe.getCategories().get(0).getName());
		assertEquals("Breakfast", recipe.getCategories().get(1).getName());
		assertEquals("Dessert", recipe.getCategories().get(2).getName());
		assertTrue(recipe.getDirectionsAsString().contains("Blend butter, cream"));
		assertEquals(1, recipe.getImages().size());
		assertEquals(11, recipe.getIngredients().size());
		List<RecipeIngredient> ingr = recipe.getIngredients();
		assertEquals(2.0f, ingr.get(0).getAmount(), 0.01f);
		assertEquals("sticks", ingr.get(0).getUnit().getName());
		assertEquals("butter", ingr.get(0).getIngredient().getName());
		assertEquals("at room temperature", ingr.get(0).getProcessing());
		assertEquals(RecipeIngredient.TYPE_INGREDIENT, ingr.get(0).getType());
		assertEquals(0.5f, ingr.get(5).getAmount(), 0.01f);
		assertEquals("cup", ingr.get(5).getUnit().getName());
		assertEquals("sour cream", ingr.get(5).getIngredient().getName());
		assertEquals("", ingr.get(5).getProcessing());
		assertEquals(RecipeIngredient.TYPE_INGREDIENT, ingr.get(5).getType());
		
		assertEquals("Banana Chocolate Chip Bread", recipe.getTitle());
		assertEquals("Only took 1 hour to bake. ", recipe.getNote());
		assertTrue(recipe.getDirectionsAsString().contains("400 cals"));
		assertEquals(20, recipe.getServings());
		assertEquals("Daisy Brand Sour Cream", recipe.getSource());
	}
	
	@Test
	public void testImages() {
		List<Recipe> all = readFromFile("src/test/data/MacGourmet/Almost_Flourless_Chocolate_Cake-542.mgourmet");
		all.get(0).getImages().get(0).saveAs("/temp/t2.jpg");
		all = readFromFile("src/test/data/MacGourmet/Banana Chocolate Chip Bread.mgourmet");
		all.get(0).getImages().get(0).saveAs("/temp/t1.jpg");
	}


	@Test
	public void testIngredientTree() {
		List<Recipe> all = readFromFile("src/test/data/MacGourmet/195 recipes.mgourmet4");
		assertEquals(195, all.size());
		Recipe r = all.get(2);
		assertEquals("American Beef Pot Roast & Cheesy Mashed Potatoes", r.getTitle());
		assertEquals(4, r.getServings());
		assertEquals("National Beef Cook-Off® 2003", r.getSource());
		assertEquals("Beef; Main", r.getCategoriesAsString());
		assertEquals(4, r.getDirections().size());
		assertTrue(r.getDirections().get(2).contains("Transfer beef mixture to platter"));
		assertEquals(8, r.getIngredients().size());
	}

	@Test
	public void testPrepTimes() {
		List<Recipe> all = readFromFile("src/test/data/MacGourmet/MacGourmet Export Test for Prep.mgourmet");
		assertEquals(24, all.size());
		assertTrue(all.get(0).getDirectionsAsString().contains("Beat: 59 seconds 29 seconds"));
		assertTrue(all.get(1).getDirectionsAsString().contains("Boil: 1 week"));
		assertTrue(all.get(2).getDirectionsAsString().contains("Brown: 13 days"));
		assertTrue(all.get(3).getDirectionsAsString().contains("Chill: 5 minutes"));
		assertTrue(all.get(4).getDirectionsAsString().contains("Freeze: 5 minutes -10 \u176C"));

		all = readFromFile("src/test/data/MacGourmet//195 recipes.mgourmet4");
		// check cook
		assertEquals(45, all.get(48).getCookTime());
		// check prep
		assertEquals(5, all.get(65).getPreparationTime());
		// check total time
		assertEquals(30, all.get(183).getTotalTime());
	}

	int n = 0;
	
	//@Test
	public void testAllMyFiles() {
		final MacGourmetXml reader = new MacGourmetXml();
		n = 0;
		BinaryInputProcessor ip = new BinaryInputProcessor() {
			public List<Recipe> read(File f) {
				System.out.println("Reading from: " + f.getName());
				if (f.getName().endsWith("zip") || f.getName().endsWith("mgourmet") || f.getName().endsWith("mgourmet4")) {
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
					n++;
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
		fileProcessor.process("Y:/data/opskrifter/MacGourmet");
		System.out.println("Found: "+n+" recipes");
	}
	
	
}
