/*
 * Created on 26-10-2004
 */
package net.sf.recipetools.javarecipes.format;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.List;

import junit.framework.TestCase;
import net.sf.recipetools.javarecipes.format.MasterCookBinary.Mc2Reader;
import net.sf.recipetools.javarecipes.model.Configuration;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;
import net.sf.recipetools.javarecipes.model.RecipeIngredient;

import org.junit.Test;

/**
 * @author ft
 *
 */
public class TemplateWriterTest extends TestCase {

	/*
	 * @see TestCase#setUp()
	 */
	private TemplateWriter writer = null; 
	protected void setUp() throws Exception {
		super.setUp();
		writer = new TemplateWriter("src/main/templates/fdx/simple.template");
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		writer = null;
	}

	public void testWriteEmptyRecipe() {
		Recipe recipe = new Recipe();
		String str = writer.recipeAsString(recipe);
		assertEquals("", str);
		str = writer.fileTailAsString();
		assertTrue(str.startsWith("<?xml"));
		assertTrue(str.contains("<Recipes>"));
		assertTrue(str.contains("</Recipes>"));
	}

	@Test
	public void testEscapeTitle() {
		Recipe recipe = new Recipe();
		recipe.setTitle("a&b");
		String str = writer.recipeAsString(recipe);
		str = writer.fileTailAsString();
		assertTrue(str.contains("Name=\"a&amp;b\""));
	}
	
	@Test
	public void testIngredients() {
		Recipe recipe = new Recipe();
		recipe.setTitle("a&b");
		RecipeIngredient ingr = new RecipeIngredient("1 tsp sugar -- white");
		recipe.addIngredient(ingr);
		String str = writer.recipeAsString(recipe);
		str = writer.fileTailAsString();
		assertTrue(str.contains("<RecipeIngredients>"));
		assertTrue(str.contains("<RecipeIngredient Quantity=\"1\""));
		assertTrue(str.contains("Unit=\"tsp\""));
		assertTrue(str.contains("Ingredient=\"sugar -- white\""));
		assertTrue(str.contains("Heading=\"N\""));
	}
	
	public void testMultipleRecipes() {
		MasterCookXml mxml = new MasterCookXml();
		LineNumberReader bufReader = null;
		try {
			bufReader = new LineNumberReader(new FileReader("src/test/data/italy-03.mx2"));
		} catch (FileNotFoundException e) {
            throw new RecipeFoxException(e);
		}
		List<Recipe> all = mxml.readRecipes(bufReader);
		assertEquals(100, all.size());
		String str = writer.recipeAsString(all.get(0));
		str = writer.fileTailAsString();
	}

	public void testHtml() {
		writer.loadTemplate("src/main/templates/html/verySimple.template");
		MasterCookXml mxml = new MasterCookXml();
		LineNumberReader bufReader = null;
		try {
			bufReader = new LineNumberReader(new FileReader("src/test/data/Max-Min MC Field Test.mx2"));
		} catch (FileNotFoundException e) {
            throw new RecipeFoxException(e);
		}
		List<Recipe> all = mxml.readRecipes(bufReader);
		String str = writer.recipeAsString(all.get(0));
		str = writer.fileTailAsString();
		//System.out.println(str);
	}
	
	public void testFdxTemplate() throws IOException {
		List<Recipe> all = readRecipes(new MasterCookXml(), "src/test/data/Max-Min MC Field Test.mx2");
		writer.loadTemplate("src/main/templates/fdx/simple.template");

		PrintWriter out = new PrintWriter(new FileWriter("C:/temp/t/t.fdx"));
		Configuration.setBooleanProperty("USE_FRACTIONS_IN_AMOUNTS", true);
		writer.writeFileHeader(out);
		writer.writeRecipe(out, all);
		writer.writeFileTail(out);
		out.close();
	}
	
	public void testBitterSweetHtml() throws IOException {
		List<Recipe> all = readRecipes(new MasterCookXml(), "src/test/data/Max-Min MC Field Test.mx2");
		createHtmlFiles("src/main/templates/html/bitter-sweet.template", all);
	}
	
	public void testMinimalBlue() throws IOException {
		//Recipe[] all = readRecipes(new MasterCookXml(), "src/test/data/Max-Min MC Field Test.mx2");
		List<Recipe> all = new Mc2Reader().read(new File("src/test/data/MC/Betty Crocker's Best of Baking.MC2"));
		createHtmlFiles("src/main/templates/html/minimal-blue.template", all);
	}
	
	List<Recipe> readRecipes(RecipeTextFormatter reader, String file) {
		LineNumberReader bufReader = null;
		try {
			bufReader = new LineNumberReader(new FileReader(file));
		} catch (FileNotFoundException e) {
            throw new RecipeFoxException(e);
		}
		List<Recipe> all = reader.readRecipes(bufReader);
		return all;
	}
	
	public void createHtmlFiles(String template, List<Recipe> recipes) throws IOException {
		writer.loadTemplate(template);

		writer.outputDirectory = "c:/temp/t";
		PrintWriter out = new PrintWriter(new FileWriter("C:/temp/t/index.html"));
		Configuration.setBooleanProperty("USE_FRACTIONS_IN_AMOUNTS", true);
		writer.writeFileHeader(out);
		writer.writeRecipe(out, recipes);
		writer.writeFileTail(out);
		out.close();
	}
}
