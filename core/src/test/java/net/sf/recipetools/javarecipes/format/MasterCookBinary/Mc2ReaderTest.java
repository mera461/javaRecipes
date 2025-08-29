/**
 * 
 */
package net.sf.recipetools.javarecipes.format.MasterCookBinary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import net.sf.recipetools.javarecipes.fileprocessor.BinaryInputProcessor;
import net.sf.recipetools.javarecipes.fileprocessor.FileProcessor;
import net.sf.recipetools.javarecipes.fileprocessor.OutputProcessor;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeIngredient;

import org.junit.Test;

/**
 * @author Frank
 * 
 */
public class Mc2ReaderTest {
	@Test
	public void testReadingRecipes() {
		Mc2Reader mc2reader = new Mc2Reader();
		mc2reader.setExtractImages(false);
		List<Recipe> recipes = mc2reader.read(new File("src/test/data/MC/Breadworld.mc2"));
		assertEquals(3, recipes.size());
		recipes = mc2reader.read(new File("src/test/data/MC/Vegetarian Food Network Canada.mc2"));
		assertEquals(1124, recipes.size());
		recipes = mc2reader.read(new File("src/test/data/MC/Alfred Bell's Cookbook.mc2"));
		assertEquals(244, recipes.size());
		mc2reader.close();
	}

	@Test
	public void testWithDiffBlockSize() {
		Mc2Reader mc2reader = new Mc2Reader();
		mc2reader.setExtractImages(false);
		// test one with different blocksize
		List<Recipe> recipes = mc2reader.read(new File("src/test/data/MC/misc.mc2"));
		assertEquals(233, recipes.size());
		mc2reader.close();
	}

	@Test
	public void testWithRecipeDataBlockZero() {
		Mc2Reader mc2reader = new Mc2Reader();
		mc2reader.setExtractImages(false);
		List<Recipe> recipes = mc2reader.read(new File("src/test/data/MC/Joyce's Meatballs and Meatloaf.mc2"));
		assertEquals(200, recipes.size());
		mc2reader.close();
	}

	@Test
	public void testWithWrongRecipeNumber() {
		Mc2Reader mc2reader = new Mc2Reader();
		mc2reader.setExtractImages(false);
		List<Recipe> recipes = mc2reader.read(new File("src/test/data/MC/My Cookbook 2.mc2"));
		assertEquals(1, recipes.size());
		mc2reader.close();
	}

	//
	@Test
	public void testWithImages() {
		Mc2Reader mc2reader = new Mc2Reader();
		mc2reader.setExtractImages(true);
		List<Recipe> recipes = mc2reader.read(new File("src/test/data/MC/Betty Crocker's Best of Baking.MC2"));
		assertEquals(367, recipes.size());
		mc2reader.close();
	}

	@Test
	public void testReadingCorrectTitles() {
		Mc2Reader mc2reader = new Mc2Reader();
		mc2reader.setExtractImages(false);
		List<Recipe> recipes = mc2reader.read(new File("src/test/data/MC/Back of the Box.mc2"));
		assertEquals(1537, recipes.size());
		assertEquals("1 Step Chicken Parmesan", recipes.get(0).getTitle());
		assertEquals("10 Minute Santa Fe Rice", recipes.get(1).getTitle());
		mc2reader.close();
	}

	@Test
	public void testAllFields() {
		Mc2Reader mc2reader = new Mc2Reader();
		mc2reader.setExtractImages(false);
		List<Recipe> recipes = mc2reader.read(new File("src/test/data/MC/Cooking Light October 2006.mc2"));
		assertEquals(112, recipes.size());
		Recipe r = recipes.get(85);
		assertEquals("Jeanne Thiel Kelley", r.getAuthor());
		assertEquals("Cooking Light October 2006", r.getCategoriesAsString());
		assertEquals("© 2006 Cooking Light magazine", r.getCopyright());
		assertEquals(null, r.getCuisine());
		assertTrue(r.getDirectionsAsString().startsWith("To prepare pot roast"));
		assertTrue(r.getDirectionsAsString().contains("sauté 8 minute"));
		assertEquals(21, r.getIngredients().size());
		assertTrue(r.getNote().startsWith("To produce a successful"));
		assertEquals(10, r.getServings());
		assertEquals("Cooking Light, OCTOBER 2006", r.getSource());
		assertEquals("Slow Cooker Beef Pot Roast with Gremolata", r.getTitle());

		RecipeIngredient ingr = r.getIngredients().get(1);
		assertEquals(1f, ingr.getAmount(), 0.001);
		assertEquals("2 1/2 pound", ingr.getUnit().getName());
		assertEquals("boneless cross-rib chuck roast", ingr.getIngredient().getName());
		assertEquals("trimmed", ingr.getProcessing());
		assertEquals(null, r.getCuisine());
		mc2reader.close();
	}

	@Test
	public void testAllFields1() {
		Mc2Reader mc2reader = new Mc2Reader();
		mc2reader.setExtractImages(false);
		mc2reader.setAddCookbookTitleAsCategory(false);
		List<Recipe> recipes = mc2reader.read(new File("src/test/data/MC/a_test.w3hours.mc2"));
		assertEquals(1, recipes.size());
		Recipe r = recipes.get(0);
		// assertEquals("URL", r.getAltSourceLabel());
		// assertEquals("myUrl", r.getAltSourceText());
		assertEquals(240, r.getTime("baking"));
		assertEquals("Author", r.getAuthor());
		assertEquals("Category2; Category3", r.getCategoriesAsString());
		assertEquals("Copyright", r.getCopyright());
		assertEquals("Cuisine1", r.getCuisine());
		assertEquals(
				"description1description1description1description1description1description1description1description1description1description1description1description1",
				r.getDescription());
		assertEquals("Step1\n\nStep22\n\nStep333\n\nStep4444", r.getDirectionsAsString());
		// assertEquals("", r.getFileSource());
		assertEquals(0, r.getImages().size());
		assertEquals("NOtes-æøåáà", r.getNote());
		assertEquals(180, r.getPreparationTime());
		assertEquals("Serving ideas", r.getServingIdeas());
		assertEquals(5, r.getServings());
		assertEquals("Source", r.getSource());
		assertEquals("test1", r.getTitle());
		assertEquals(195, r.getTotalTime());
		// assertEquals("myUrl", r.getUrl());
		assertEquals("Wine", r.getWine());
		assertEquals("17 deciliters", r.getYield());

		// ingredient
		assertEquals(1, r.getIngredients().size());
		RecipeIngredient ingr = r.getIngredients().get(0);
		assertEquals(1.0f, ingr.getAmount(), 0.01);
		assertEquals("unit", ingr.getUnit().getName());
		assertEquals("ingredient", ingr.getIngredient().getName());
		assertEquals("preparation", ingr.getProcessing());

		mc2reader.close();
	}

	@Test
	public void testAccentedChars() {
		Mc2Reader mc2reader = new Mc2Reader();
		mc2reader.setExtractImages(false);
		mc2reader.setAddCookbookTitleAsCategory(false);
		List<Recipe> recipes = mc2reader.read(new File("src/test/data/MC/accent_test.mc2"));
		assertEquals(2, recipes.size());
		Recipe r = recipes.get(1);
		assertEquals("æøåé", r.getAuthor());
		assertEquals("æøåé", r.getCopyright());
		assertEquals("æøåé", r.getDescription());
		assertEquals("æøåé", r.getDirectionsAsString());
		assertEquals("æøåé", r.getNote());
		assertEquals("æøåé", r.getServingIdeas());
		assertEquals("æøåé", r.getSource());
		assertEquals("1 æøåé", r.getYield());
		assertEquals("æøåé", r.getTitle());

		// ingredient
		assertEquals(1, r.getIngredients().size());
		RecipeIngredient ingr = r.getIngredients().get(0);
		assertEquals(1.0f, ingr.getAmount(), 0.01);
		assertEquals("æøåé", ingr.getIngredient().getName());
		assertEquals("æøåé", ingr.getProcessing());

		mc2reader.close();
	}

	@Test
	public void testTimes() {
		Mc2Reader mc2reader = new Mc2Reader();
		mc2reader.setExtractImages(false);
		List<Recipe> recipes = mc2reader.read(new File("src/test/data/MC/a_test2.mc2"));
		assertEquals(3, recipes.size());
		Recipe r= recipes.get(0);
		assertEquals("prep11-total101", r.getTitle());
		assertEquals(11, r.getPreparationTime());
		assertEquals(101, r.getTotalTime());
		assertEquals("prep13-total113", recipes.get(1).getTitle());
		assertEquals(13, recipes.get(1).getPreparationTime());
		assertEquals(113, recipes.get(1).getTotalTime());
		assertEquals("prep47-total147", recipes.get(2).getTitle());
		assertEquals(47, recipes.get(2).getPreparationTime());
		assertEquals(147, recipes.get(2).getTotalTime());
		mc2reader.close();
	}

	@Test
	public void testDirectiveImages() {
		Mc2Reader mc2reader = new Mc2Reader();
		mc2reader.setExtractImages(true);
		List<Recipe> recipes = mc2reader.read(new File("src/test/data/MC/a_images.mc2"));
		assertEquals(1, recipes.size());
		Recipe r = recipes.get(0);
		assertEquals(4, r.getDirections().size());
		assertEquals(1, r.getImages().size());
		assertEquals(3, r.getDirectionImages().size());
		assertTrue(null != r.getDirectionImage(0));
		assertTrue(null != r.getDirectionImage(1));
		assertTrue(null != r.getDirectionImage(2));
		assertEquals(null, r.getDirectionImage(3));
		mc2reader.close();
	}

	@Test
	public void testZipFile() {
		final Mc2Reader mc2reader = new Mc2Reader();
		mc2reader.setExtractImages(false);
		BinaryInputProcessor ip = new BinaryInputProcessor() {
			public List<Recipe> read(File f) {
				return mc2reader.read(f);
			}
		};
		OutputProcessor op = new OutputProcessor() {
			public void write(List<Recipe> recipes) {
				assertEquals(112, recipes.size());
			}

			public void startFile(File name) {}
			public void startFile(String name) {}
			public void endFile() {}
		};
		FileProcessor fileProcessor = new FileProcessor(ip, op);
		fileProcessor.process("src/test/data/MC/Cooking Light October 2006.zip");
		mc2reader.close();
	}

	@Test
	public void testAllOfThem() {
		if (true)
			return;
		final Mc2Reader mc2reader = new Mc2Reader();
		mc2reader.setExtractImages(true);
		BinaryInputProcessor ip = new BinaryInputProcessor() {
			public List<Recipe> read(File f) {
				return mc2reader.read(f);
			}
		};
		OutputProcessor op = new OutputProcessor() {
			public void write(List<Recipe> recipes) {
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
		fileProcessor.process("/pr/opskrifter/MasterCook - MC2/");

		mc2reader.close();
	}
	
	@Test
	public void testWithMc15newFormat() {
		Mc2Reader mc2reader = new Mc2Reader();
		List<Recipe> recipes = mc2reader.read(new File("src/test/data/MC/MC15-new/01 Test New.mc2"));
		assertEquals(1, recipes.size());
		Recipe r = recipes.get(0);
		assertEquals("http://www.seriouseats.com/recipes/2017/02/shrimp-fra-diavolo-pasta-recipe.html", r.getUrl());
		assertEquals("Shrimp Fra Diavolo (Spaghetti With Spicy Tomato Sauce) Recipe", r.getTitle());
		assertEquals(1, r.getImages().size());
	}

}
