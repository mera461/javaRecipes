/*
 * Created on 26-10-2004
 *
 */
package net.sf.recipetools.javarecipes.format;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.List;

import org.junit.Test;

import junit.framework.TestCase;
import net.sf.recipetools.javarecipes.model.Image;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;
import net.sf.recipetools.javarecipes.model.RecipeIngredient;

/**
 * @author ft
 *
 */
public class MasterCookXmlTest extends TestCase {

	/*
	 * @see TestCase#setUp()
	 */
	private MasterCookXml mxml = null; 
	protected void setUp() throws Exception {
		super.setUp();
		mxml = new MasterCookXml();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		mxml = null;
	}

	public void testReadRecipe1() {
		LineNumberReader bufReader = null;
		try {
			bufReader = new LineNumberReader(new FileReader("src/test/data/italy-03.mx2"));
		} catch (FileNotFoundException e) {
            throw new RecipeFoxException(e);
		}

		
		List<Recipe> all = mxml.readRecipes(bufReader);
		assertEquals(100, all.size());
		Recipe recipe = all.get(0);
		assertTrue(recipe != null);
		assertEquals("A Drum Of Eggplant And Bucatini  - ...", recipe.getTitle());
		assertEquals("Mary Ann Esposito", recipe.getAuthor());
		assertEquals(12, recipe.getServings());
		assertEquals("Italian", recipe.getCategories().get(0).getName());
		assertEquals("Main Dish", recipe.getCategories().get(1).getName());
		assertEquals("Pasta", recipe.getCategories().get(2).getName());
		assertTrue(recipe.getNote().contains("{Timballo Di Melanzane E Bucatini}"));
		assertTrue(recipe.getNote().contains("by Mary A. Esposito, (Morrow Cookbooks, 1998)"));
		assertEquals("Ciao Italia at http://www.ciaoitalia.com", recipe.getSource());
		assertEquals(29, recipe.getIngredients().size());
		assertTrue(recipe.getDirectionsAsString().contains("Cut off the stems"));
		assertTrue(recipe.getDirectionsAsString().contains("\"sweat\""));
		assertTrue(recipe.getDirectionsAsString().contains("best drumroll"));
	}

 	public void testCheckWithBadXMLHeader() {
		LineNumberReader bufReader = null;
		try {
			
			bufReader = new LineNumberReader(new FileReader("src/test/data/cannin02.mx2"));
//			bufReader = new LineNumberReader(new FileReader("src/test/data/asian-03.mx2"));
//			bufReader = new LineNumberReader(new FileReader("D:/pr/data/opskrifter/MasterCook - MX2/akseafood_mx2.zip"));
		} catch (FileNotFoundException e) {
			fail("No exception expected");
		}
		List<Recipe> recipes = mxml.readRecipes(bufReader);
		assertEquals(100, recipes.size());
	}

	public void testWriteEmptyRecipe() {
		Recipe recipe = new Recipe();
		String str = mxml.recipeAsString(recipe);
		assertNotNull(str);
		assertTrue(str.startsWith("<RcpE"));
		
		// almost empty
		recipe.setTitle("<new recipe>");
		recipe.setIngredients((String) null);
		str = mxml.recipeAsString(recipe);
		assertNotNull(str);

		recipe.setIngredients((List<RecipeIngredient>)null);
		str = mxml.recipeAsString(recipe);
		assertNotNull(str);
	}
 	
	public void testWithParagraphsDirections() {
		Recipe recipe = new Recipe();
		recipe.setDirections("test\n\ntest2\n\ntest3");
		String str = mxml.recipeAsString(recipe);
		assertNotNull(str);
		assertTrue(str.contains("<DirT>\r\ntest\r\n</DirT>"));
		assertTrue(str.contains("<DirT>\r\ntest2\r\n</DirT>"));
		assertTrue(str.contains("<DirT>\r\ntest3\r\n</DirT>"));
	}

	public void testWithDirectionsWithLinebreaks() {
		Recipe recipe = new Recipe();
		recipe.addDirections("line1\nline3");
		recipe.addDirections("step2");
		String str = mxml.recipeAsString(recipe);
		System.out.println(str);
		assertNotNull(str);
		assertTrue(str.contains("<DirT>\r\nline1&#013;&#010;line3\r\n</DirT>"));
		assertTrue(str.contains("<DirT>\r\nstep2\r\n</DirT>"));
	}
	
	
	public void testWithNonFractionalUnit() {
		Recipe recipe = new Recipe();
		recipe.setIngredients("1/25 1/2 ounce can  black beans, rinsed");
		String str = mxml.recipeAsString(recipe);
		System.out.println(str);
		assertNotNull(str);
		assertTrue(str.contains("<IngR code=\"I\" name=\"black beans, rinsed\" unit=\"1/2 ounce can\" qty=\"0.04\"></IngR>"));
	}

	
	
	public void testWithParagraphsDirectionsNormalized() {
		Recipe recipe = new Recipe();
		recipe.setDirections("test\n\ntest2\n\ntest3");
		recipe.normalize();
		String str = mxml.recipeAsString(recipe);
		assertNotNull(str);
		assertTrue(str.contains("<DirT>\r\ntest\r\n</DirT>"));
		assertTrue(str.contains("<DirT>\r\ntest2\r\n</DirT>"));
		assertTrue(str.contains("<DirT>\r\ntest3\r\n</DirT>"));
	}
	
	
	public void testWithImage() {
		Recipe recipe = createRecipe();
		Image image = new Image("name", "src/test/data/cookies.jpg");
		recipe.addImage(image);
		mxml.setImageDir("c:\\temp");
		mxml.setTheMasterCookProgram(new File("C:\\Program Files\\MasterCook 14\\Program\\Mastercook14.exe"));
		String str = mxml.recipeAsString(recipe);
		assertNotNull(str);
		File file = new File("c:\\temp\\recipetitle.jpg");
		assertTrue(file.exists());
		file.delete();
		
		assertTrue(str.toLowerCase().contains("author=\"\" img=\"recipetitle.jpg\""));
	}

	public Recipe createRecipe() {
		Recipe recipe = new Recipe();
		recipe.setDirections("test\n\ntest2\n\ntest3");
		recipe.setTitle("recipe & title");
		return recipe;
	}

	public void testXmlEscape() {
		assertEquals("aa&#33298;aa", RecipeTextFormatter.escapeXml("aa\u8212aa"));
	}

	public void testWritingFileSource() {
		Recipe recipe = new Recipe();
		recipe.setDirections("test\n\ntest2\n\ntest3");
		recipe.setFileSource("dir1/file2");
		recipe.normalize();
		String str = mxml.recipeAsString(recipe);
		assertNotNull(str);
		assertTrue(str.contains("<!-- Original file: dir1/file2 -->"));
	}

	@Test
	public void testWithSpecialChars() {
		LineNumberReader bufReader = null;
		String filename = "src/test/data/MacGourmet/specialChars.mgourmet";
		try {
			bufReader = new LineNumberReader(new InputStreamReader(new FileInputStream(new File(filename)), "UTF-8"));
		} catch (IOException e) {
            throw new RecipeFoxException(e);
		}
		
		List<Recipe> all = new MacGourmetXml().read(bufReader);
		assertEquals(1, all.size());
		String str = mxml.recipeAsString(all.get(0));
		String[] lines = str.split("[\r\n]+");
		assertEquals("<RcpE name=\"Harry Young&apos;s Burgoo (Stew/Soup)\" author=\"\">", lines[0]);
	}

	@Test
	public void testRecipeWithNutrionalLinksINtI() {
		LineNumberReader bufReader = null;
		try {
			bufReader = new LineNumberReader(new FileReader("src/test/data/ice_creammx2.mx2"));
		} catch (FileNotFoundException e) {
            throw new RecipeFoxException(e);
		}

		
		List<Recipe> all = mxml.readRecipes(bufReader);
		assertEquals(212, all.size());
		
	}
	
}
