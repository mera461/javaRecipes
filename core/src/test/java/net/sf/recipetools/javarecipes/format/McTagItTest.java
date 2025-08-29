/**
 * 
 */
package net.sf.recipetools.javarecipes.format;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.List;

import junit.framework.TestCase;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;
import net.sf.recipetools.javarecipes.model.RecipeIngredient;

import org.junit.Test;



/**
 * @author Frank
 *
 */
public class McTagItTest extends TestCase {
	private McTagIt mm = null; 
	protected void setUp() throws Exception {
		super.setUp();
		mm = new McTagIt();
		mm.setImageDir(new File("src/test/data/mctagit"));
	}
	
	@Test
	public void testSimpleReading() {
		LineNumberReader bufReader = null;
		try {
			bufReader = new LineNumberReader(new FileReader("src/test/data/mctagit/simple.txt"));
		} catch (FileNotFoundException e) {
            throw new RecipeFoxException(e);
		}

		List<Recipe> recipes = null;
		recipes = mm.readRecipes(bufReader);
		assertNotNull(recipes);
		assertEquals(1, recipes.size());
		Recipe r = recipes.get(0);
		assertEquals("QUICK PICKLED GREEN BEANS", r.getTitle());
		assertEquals("Vegetables", r.getCategories().get(0).getName());
		assertEquals("8 Cups", r.getYield());
		assertEquals(10, r.getServings());
		assertEquals(20, r.getPreparationTime());
		assertEquals(80, r.getTotalTime());
		assertEquals("Mary Clifford, \"Memories of a Country Garden\"", r.getAuthor());
		assertEquals("Cooking Light Magazine, July/August 1993, page 69", r.getSource());
		assertEquals("1993, by Southern Living, Inc.", r.getCopyright());
		assertEquals("Formatted By", r.getAltSourceLabel());
		assertEquals("John Shotsky", r.getAltSourceText());
		assertEquals(10, r.getIngredients().size());
		assertTrue(r.getDirectionsAsString().startsWith("Wash beans"));
		assertEquals(1, r.getImages().size());
	}

	@Test
	public void testConvertSimple() {
		convertFile("src/test/data/mctagit/simple.txt", "src/test/data/mctagit/simple.mx2");
	}
	
	@Test
	public void testConvertTest() {
		convertFile("src/test/data/mctagit/test.txt", "src/test/data/mctagit/test.mx2");
	}
	
	public void convertFile(String input, String output) {
		LineNumberReader bufReader = null;
		try {
			bufReader = new LineNumberReader(new FileReader(input));
		} catch (FileNotFoundException e) {
            throw new RecipeFoxException(e);
		}
		List<Recipe> recipes = null;
		recipes = mm.readRecipes(bufReader);
		assertNotNull(recipes);

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(output);
		} catch (FileNotFoundException e) {
            throw new RecipeFoxException(e);
		}
		PrintWriter ps = new PrintWriter(fos);
		
		MasterCookXml exporter = new MasterCookXml();
		exporter.writeFileHeader(ps);
		exporter.writeRecipe(ps, recipes);
		exporter.writeFileTail(ps);
		ps.close();
	}
	
	public List<Recipe> readRecipesFromString(String str) {
		LineNumberReader reader = new LineNumberReader(new StringReader(str));
		return  mm.readRecipes(reader);
	}
	
	@Test
	public void testGlobalAttr() {
		String str = "[[[\n" +
				"GPT::0:21\n" +
				"T::Recipe 1\n" +
				"]]]\n" +
				"[[[\n" +
				"T::Recipe 2\n" +
				"]]]\n" +
				"[[[\n" +
				"T::Recipe 3\n" +
				"PT::0:22\n" +
				"]]]\n";
		List<Recipe> recipes = readRecipesFromString(str);
		assertNotNull(recipes);
		assertEquals(3, recipes.size());
		assertEquals("Recipe 1", recipes.get(0).getTitle());
		assertEquals("Recipe 2", recipes.get(1).getTitle());
		assertEquals("Recipe 3", recipes.get(2).getTitle());
		assertEquals(21, recipes.get(0).getPreparationTime());
		assertEquals(21, recipes.get(1).getPreparationTime());
		assertEquals(22, recipes.get(2).getPreparationTime());
	}

	@Test
	public void testSeveralNotes() {
		String str = "[[[\n" +
				"N::Note 1\n" +
				"T::Recipe 1\n" +
				"N::Note 2\n" +
				"DIR::Directions\n" +
				"N::Note 3\n" +
				"]]]\n";
		List<Recipe> recipes = readRecipesFromString(str);
		assertNotNull(recipes);
		assertEquals(1, recipes.size());
		assertEquals("Recipe 1", recipes.get(0).getTitle());
		assertEquals("Note 1\n\nNote 2\n\nNote 3", recipes.get(0).getNote());
	}

	@Test
	public void testCategories() {
		String str = "[[[\n" +
				"T::Recipe 1\n" +
				"C::cat1; cat2 cat3;fish, shellfish\n" +
				"]]]\n";
		List<Recipe> recipes = readRecipesFromString(str);
		assertNotNull(recipes);
		assertEquals(1, recipes.size());
		assertEquals("Recipe 1", recipes.get(0).getTitle());
		assertEquals(3, recipes.get(0).getCategories().size());
		assertEquals("cat1", recipes.get(0).getCategories().get(0).getName());
		assertEquals("cat2 cat3", recipes.get(0).getCategories().get(1).getName());
		assertEquals("fish, shellfish", recipes.get(0).getCategories().get(2).getName());
	}

	@Test
	public void testNormalizeYield() {
		String str = "[[[\n" +
				"T::testing\n" +
				"Y::2 cups\n" +
				"I::4 1/2 cups sugar\n" +
				"  5.5 cups salt\n" +
				"DIR::Directions\n" +
				"N::Note 3\n" +
				"]]]\n";
		List<Recipe> recipes = readRecipesFromString(str);
		assertNotNull(recipes);
		assertEquals(1, recipes.size());
		recipes.get(0).normalizeYield();
		assertEquals(4.5f, recipes.get(0).getIngredients().get(0).getAmount(), 0.001);
		assertEquals(5.5f, recipes.get(0).getIngredients().get(1).getAmount(), 0.001);
	}

	@Test
	public void testIgnoreEmptyTags() {
		String str = "[[[\n" +
				"GPT::0:21\n" +
				"T::Recipe 1\n" +
				"]]]\n" +
				"[[[\n" +
				"T::Recipe 2\n" +
				"PT::\n" +
				"]]]\n" +
				"[[[\n" +
				"T::Recipe 3\n" +
				"PT::0:22\n" +
				"]]]\n";
		List<Recipe> recipes = readRecipesFromString(str);
		assertNotNull(recipes);
		assertEquals(3, recipes.size());
		assertEquals(21, recipes.get(0).getPreparationTime());
		assertEquals(21, recipes.get(1).getPreparationTime());
		assertEquals(22, recipes.get(2).getPreparationTime());
	}
	
	@Test
	public void testIngredientTypes() {
		String str = "[[[\n" +
				"T::Recipe 1\n" +
				"I:: 1 cup first ingredient\n" +
				"2 cups second ingredient\n" +
				"T> some text\n" +
				"S> some subtitle\n" +
				"R> some title of a recipe\n" +
				"3 cups of water\n" +
				"]]]\n";
		List<Recipe> recipes = readRecipesFromString(str);
		assertNotNull(recipes);
		assertEquals(1, recipes.size());
		List<RecipeIngredient> ingr = recipes.get(0).getIngredients();
		assertEquals(6, ingr.size());
		assertEquals(RecipeIngredient.TYPE_INGREDIENT, ingr.get(0).getType());
		assertEquals(RecipeIngredient.TYPE_INGREDIENT, ingr.get(1).getType());
		assertEquals(RecipeIngredient.TYPE_TEXT, ingr.get(2).getType());
		assertEquals("some text", ingr.get(2).getIngredient().getName());
		assertEquals(RecipeIngredient.TYPE_SUBTITLE, ingr.get(3).getType());
		assertEquals("some subtitle", ingr.get(3).getIngredient().getName());
		assertEquals(RecipeIngredient.TYPE_RECIPE, ingr.get(4).getType());
		assertEquals("some title of a recipe", ingr.get(4).getIngredient().getName());
		assertEquals(RecipeIngredient.TYPE_INGREDIENT, ingr.get(5).getType());
	}
	
	@Test
	public void testNutritionalTypes() {
		String str = "[[[\n" +
				"T::Recipe 1\n" +
				"NUT:: some nutr\n" +
				"DIR:: Do this\n" +
				"Do that\n" +
				"]]]\n";
		List<Recipe> recipes = readRecipesFromString(str);
		assertNotNull(recipes);
		assertEquals(1, recipes.size());
		assertEquals("Do this\nDo that\n\nsome nutr", recipes.get(0).getDirectionsAsString());
	}
	
	@Test
	public void testYieldAmount() {
		String str = "[[[\n" +
				"T::Recipe 1\n" +
				"YA:: 3 1/2\n" +
				"YU:: cups\n" +
				"]]]\n";
		List<Recipe> recipes = readRecipesFromString(str);
		assertNotNull(recipes);
		assertEquals(1, recipes.size());
		assertEquals("3 1/2 cups", recipes.get(0).getYield());
	}

	@Test
	public void testNutAndFootnotes() {
		String str = "[[[\n" +
				"T::Recipe 1\n" +
				"NUT:: nut 1\n" +
				"DIR:: dir 1\n" +
				"FN:: footnote 1\n" +
				"NUT:: nut 2\n" +
				"DIR:: dir 2\n" +
				"FN:: footnote 2\n" +
				"Do that\n" +
				"]]]\n";
		List<Recipe> recipes = readRecipesFromString(str);
		assertNotNull(recipes);
		assertEquals(1, recipes.size());
		assertEquals("dir 1\n\ndir 2\n\nnut 1\n\nnut 2\n\nfootnote 1\n\nfootnote 2\nDo that", recipes.get(0).getDirectionsAsString());
	}
	
	@Test
	public void testWriting() {
		LineNumberReader bufReader = null;
		try {
			bufReader = new LineNumberReader(new FileReader("src/test/data/mctagit/simple.txt"));
		} catch (FileNotFoundException e) {
            throw new RecipeFoxException(e);
		}
		List<Recipe> recipes = null;
		recipes = mm.readRecipes(bufReader);
		assertNotNull(recipes);
		
		// write them
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PrintWriter ps = new PrintWriter(bos);
		mm.writeRecipe(ps, recipes);
		ps.close();
		
		System.out.println(bos.toString());
		
		// read the recipes again
		List<Recipe> recipes1 = readRecipesFromString(bos.toString());
		assertEquals(recipes.size(), recipes1.size());
		Recipe recipe1 = recipes1.get(0);
		assertEquals(recipes.get(0).getAltSourceLabel(), recipe1.getAltSourceLabel());
		assertEquals(recipes.get(0).getAltSourceText(), recipe1.getAltSourceText());
		assertEquals(recipes.get(0).getAuthor(), recipe1.getAuthor());
		assertEquals(recipes.get(0).getCategoriesAsString(), recipe1.getCategoriesAsString());
		assertEquals(recipes.get(0).getCopyright(), recipe1.getCopyright());
		assertEquals(recipes.get(0).getCuisine(), recipe1.getCuisine());
		assertEquals(recipes.get(0).getDescription(), recipe1.getDescription());
		assertEquals(recipes.get(0).getDirectionsAsString(), recipe1.getDirectionsAsString());
		assertEquals(recipes.get(0).getNote(), recipe1.getNote());
		assertEquals(recipes.get(0).getPreparationTime(), recipe1.getPreparationTime());
		assertEquals(recipes.get(0).getServingIdeas(), recipe1.getServingIdeas());
		assertEquals(recipes.get(0).getServings(), recipe1.getServings());
		assertEquals(recipes.get(0).getSource(), recipe1.getSource());
		assertEquals(recipes.get(0).getTitle(), recipe1.getTitle());
		assertEquals(recipes.get(0).getTotalTime(), recipe1.getTotalTime());
		assertEquals(recipes.get(0).getWine(), recipe1.getWine());
		assertEquals(recipes.get(0).getYield(), recipe1.getYield());
		
		// TODO: Ingredients
	}

	@Test
	public void testWritingEmptyYield() {
		Recipe r = new Recipe();
		r.setTitle("title");
		String str = mm.recipeAsString(r);
		assertFalse(str.contains("Y::"));
		
		r.setYield("one cake");
		str = mm.recipeAsString(r);
		assertTrue(str.contains("Yield:: one cake"));
		
		r.setYield("12 cakes");
		str = mm.recipeAsString(r);
		assertTrue(str.contains("Yield:: 12 cakes"));
	}
	
	@Test
	public void testYieldParts() {
		// yamt first
		String str = "[[[\n" +
				"T::Recipe 1\n" +
				"YAMT::4\n" +
				"YU::cakes\n" +
				"]]]\n";
		List<Recipe> recipes = readRecipesFromString(str);
		assertNotNull(recipes);
		assertEquals(1, recipes.size());
		assertEquals("4 cakes", recipes.get(0).getYield());
		// units first
		str = "[[[\n" +
			"T::Recipe 1\n" +
			"YU::cakes\n" +
			"YAMT::4\n" +
			"]]]\n";
		recipes = readRecipesFromString(str);
		assertNotNull(recipes);
		assertEquals(1, recipes.size());
		assertEquals("4 cakes", recipes.get(0).getYield());
	}

	@Test
	public void testRatings() {
		String str = "[[[\n" +
				"T::Recipe 1\n" +
				"R::rating 1: 4\n" +
				"RATE::rating 2: 12\n" +
				"DIR:: dir 2\n" +
				"Do that\n" +
				"]]]\n";
		List<Recipe> recipes = readRecipesFromString(str);
		assertNotNull(recipes);
		assertEquals(1, recipes.size());
		assertEquals(2, recipes.get(0).getRatings().size());
		assertEquals(0.4f, recipes.get(0).getRating("rating 1"), 0.001);
		assertEquals(1.2f, recipes.get(0).getRating("rating 2"), 0.001);
	}
	
	@Test
	public void testCRLF() {
		String str = "[[[\n" +
				"T::Recipe 1\n" +
				"DIR:: Do this[CRLF]line2\n\n" +
				"Do that[CRLF][crlf]line3\n" +
				"]]]\n";
		List<Recipe> recipes = readRecipesFromString(str);
		assertNotNull(recipes);
		assertEquals(1, recipes.size());
		assertEquals(2, recipes.get(0).getDirections().size());
		assertEquals("Do this\nline2", recipes.get(0).getDirections().get(0));
		assertEquals("Do that\n\nline3", recipes.get(0).getDirections().get(1));
		
		//test the same but just with [CR]
        str = "[[[\n" +
                "T::Recipe 1\n" +
                "DIR:: Do this[CR]line2\n\n" +
                "Do that[CR][cr]line3\n" +
                "]]]\n";
        recipes = readRecipesFromString(str);
        assertNotNull(recipes);
        assertEquals(1, recipes.size());
        assertEquals(2, recipes.get(0).getDirections().size());
        assertEquals("Do this\nline2", recipes.get(0).getDirections().get(0));
        assertEquals("Do that\n\nline3", recipes.get(0).getDirections().get(1));
	}
	
    @Test
    public void testCRLFAtStartAndEnd() {
        // at start of line
        String str = "[[[\n" +
                "T::Recipe 1\n" +
                "DIR:: [CRLF]line2\n\n" +
                "Do that[CRLF][crlf]line3\n" +
                "]]]\n";
        List<Recipe> recipes = readRecipesFromString(str);
        assertNotNull(recipes);
        assertEquals(1, recipes.size());
        assertEquals(2, recipes.get(0).getDirections().size());
        assertEquals("line2", recipes.get(0).getDirections().get(0));
        assertEquals("Do that\n\nline3", recipes.get(0).getDirections().get(1));

        // at end of line
        str = "[[[\n" +
                "T::Recipe 1\n" +
                "DIR:: line2[CRLF]\nline3\n\n" +
                "Do that[CRLF][crlf]line4\n" +
                "]]]\n";
        recipes = readRecipesFromString(str);
        assertNotNull(recipes);
        assertEquals(1, recipes.size());
        assertEquals(2, recipes.get(0).getDirections().size());
        assertEquals("line2\nline3", recipes.get(0).getDirections().get(0));
        assertEquals("Do that\n\nline4", recipes.get(0).getDirections().get(1));
    }

    @Test
	public void testRecipeSeparators() {
		String str = "[[[\n" +
				"T::Recipe 1\n" +
				"DIR:: Do this\n" +
				"[[[\n" +
				"T::Recipe 2\n" +
				"DIR:: Do this 2";
		List<Recipe> recipes = readRecipesFromString(str);
		assertNotNull(recipes);
		assertEquals(2, recipes.size());
		assertEquals("Do this", recipes.get(0).getDirections().get(0));
		assertEquals("Do this 2", recipes.get(1).getDirections().get(0));
	}

	@Test
	public void testMultipleNut() {
		String str = "[[[\n" +
				"T::Recipe 1\n" +
				"DIR::dir1\n" +
				"NUT::nut1\n" +
				"DIR::dir2\n" +
				"NUT::nut2\n";
		List<Recipe> recipes = readRecipesFromString(str);
		assertNotNull(recipes);
		assertEquals(1, recipes.size());
		assertEquals(4, recipes.get(0).getDirections().size());
		assertEquals("dir1", recipes.get(0).getDirections().get(0));
		assertEquals("dir2", recipes.get(0).getDirections().get(1));
		assertEquals("nut1", recipes.get(0).getDirections().get(2));
		assertEquals("nut2", recipes.get(0).getDirections().get(3));
	}
	
	
	@Test
	public void testOneLinerServes() {
		String str = "[[[\n" +
				"T::Recipe 1\n" +
				"DIR::dir1\n" +
				"S::6\n" +
				"nut2\n" +
				"nut3\n";
		List<Recipe> recipes = readRecipesFromString(str);
		assertNotNull(recipes);
		assertEquals(1, recipes.size());
		assertEquals(2, recipes.get(0).getDirections().size());
		assertEquals("dir1", recipes.get(0).getDirections().get(0));
		assertEquals("nut2\nnut3", recipes.get(0).getDirections().get(1));
	}
	
	@Test
	public void testOneLinerWithEmptyLineFirst() {
		String str = "[[[\n" +
				"T::Recipe 1\n" +
				"DIR::dir1\n" +
				"Y::\n" +
				"6 cups\n" +
				"nut2\n" +
				"nut3\n";
		List<Recipe> recipes = readRecipesFromString(str);
		assertNotNull(recipes);
		assertEquals(1, recipes.size());
		assertEquals(2, recipes.get(0).getDirections().size());
		assertEquals("6 cups", recipes.get(0).getYield());
		assertEquals("dir1", recipes.get(0).getDirections().get(0));
		assertEquals("nut2\nnut3", recipes.get(0).getDirections().get(1));
	}
	

	@Test
	public void testExtraDirTags() {
		String str = "[[[\n" +
				"T::Recipe 1\n" +
				"DIR::dir1\n" +
				"DIR::YIELD::6 cups\n" +
				"DIR::NUT::\n" +
				"nut2\n" +
				"nut3\n" +
				"NOTES::TIMES::test";
		List<Recipe> recipes = readRecipesFromString(str);
		assertNotNull(recipes);
		assertEquals(1, recipes.size());
		assertEquals(3, recipes.get(0).getDirections().size());
		assertEquals("dir1", recipes.get(0).getDirections().get(0));
		assertEquals("6 cups", recipes.get(0).getDirections().get(1));
		assertEquals("nut2\nnut3", recipes.get(0).getDirections().get(2));
		assertEquals("test", recipes.get(0).getNote());
	}
	
	@Test
	public void testSpecialIngredient() {
		// test ingredient ending with a comma
		RecipeIngredient ingr = new RecipeIngredient(1f, "cups", "water,", "(1-2 cups)");
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PrintWriter ps = new PrintWriter(bos);
		mm.printIngredient(ps, ingr);
		ps.close();
		assertEquals("1 cups water, (1-2 cups)\r\n", bos.toString());
	}

	
}
