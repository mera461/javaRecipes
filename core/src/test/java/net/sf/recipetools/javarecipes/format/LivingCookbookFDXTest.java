/*
 * Created on 26-10-2004
 *
 */
package net.sf.recipetools.javarecipes.format;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import junit.framework.TestCase;
import net.sf.recipetools.javarecipes.fileprocessor.BinaryInputProcessor;
import net.sf.recipetools.javarecipes.fileprocessor.FileProcessor;
import net.sf.recipetools.javarecipes.fileprocessor.OutputProcessor;
import net.sf.recipetools.javarecipes.fileprocessor.RecipeFileWriter;
import net.sf.recipetools.javarecipes.format.MasterCookBinary.Mc2Reader;
import net.sf.recipetools.javarecipes.model.Configuration;
import net.sf.recipetools.javarecipes.model.Cookbook;
import net.sf.recipetools.javarecipes.model.Folder;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;
import net.sf.recipetools.javarecipes.model.RecipeIngredient;

/**
 * @author ft
 *
 */
public class LivingCookbookFDXTest extends TestCase {

	PrintWriter out = null;	
	
	/*
	 * @see TestCase#setUp()
	 */
	private LivingCookbookFDX formatter = null; 
	protected void setUp() throws Exception {
		super.setUp();
		formatter = new LivingCookbookFDX();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		formatter = null;
	}

	public void testWriteEmptyRecipe() {
		Recipe recipe = new Recipe();
		String str = formatter.recipeAsString(recipe);
		assertNotNull(str);
	
		assertEquals("<Recipe ID=\"1\" Name=\"\" Servings=\"0\">\r\n</Recipe>\r\n", str);
	}
	
	public void testRatings() {
		Recipe recipe = new Recipe();
		// with one rating
		recipe.setRating("Kids", 1f);
		String str = formatter.recipeAsString(recipe);
		assertTrue(str.contains("<RecipeReviews>\r\n<RecipeReview Rating=\"5\" Reviewer=\"Mastercook\" ReviewDate=\""));
		assertTrue(str.contains("Kids: 5\r\n</RecipeReview>\r\n</RecipeReviews>"));
		// with two rating
		recipe.setRating("Me", 0.1f);
		str = formatter.recipeAsString(recipe);
		assertTrue(str.contains("<RecipeReviews>\r\n<RecipeReview Rating=\"3\" Reviewer=\"Mastercook\" ReviewDate=\""));
		assertTrue(str.contains("Kids: 5\r\n"));
		assertTrue(str.contains("Me: 1\r\n"));
	}
	
	public void testDirectory() throws FileNotFoundException, UnsupportedEncodingException {
		if (false) return;
		Configuration.setBooleanProperty("USE_FRACTIONS_IN_AMOUNTS", true);
		Configuration.setBooleanProperty("MERGE_INGREDIENT_LINE_CONTINUATIONS", false);
		Configuration.setBooleanProperty("SPLIT_INGREDIENT_LINES_WITH_PLUS", false);
		Configuration.setBooleanProperty("DETECT_AND_MARK_SERVINGS_FROM_YIELD", true);
		Configuration.setBooleanProperty("MOVE_NOTES_IN_DIRECTIONS_TO_NOTES", false);
		Configuration.setBooleanProperty("REMOVE_NON_RECIPE_COMMENTS_FROM_DIRECTIONS", false);
		Configuration.setBooleanProperty("REMOVE_NUTRITION_INFORMATION_FROM_DIRECTIONS", false);
		Configuration.setBooleanProperty("DETECT_AND_MARK_SERVINGS_FROM_DIRECTIONS", true);
		Configuration.setBooleanProperty("EXPAND_UNIT_ABBREVIATIONS", true);
		Configuration.setBooleanProperty("CALCULATE_WEIGHT_FROM_AMOUNT_TIMES_PACKAGE_SIZE", false);
		Configuration.setBooleanProperty("USE_WEIGHT_NOT_PACKAGE_SIZE", false);
		Configuration.setBooleanProperty("INGREDIENT_PROCESSING_WORDS_TO_PREPARATION", false);
		Configuration.setBooleanProperty("PLURALISE_UNITS", true);
		Configuration.setBooleanProperty("MOVE_TEXT_AFTER_COMMAS_TO_PREPARATION", false);
		Configuration.loadSafeCommaStructuresFromFile("commas.txt");
		
		// settings
		final String outputDir = "C:/temp/convert";
		final String fileExtension = "fdx";
		
		final RecipeTextFormatter outputFormatter = new LivingCookbookFDX();
		outputFormatter.setWriteImages(true);
		outputFormatter.setImageDir(outputDir);
		@SuppressWarnings("resource")
		final Mc2Reader reader = new Mc2Reader();

		
		// add cookbook name as a category ?
		reader.setAddCookbookTitleAsCategory(false);

		BinaryInputProcessor ip = new BinaryInputProcessor() {
			public List<Recipe> read(File f) {
				return reader.read(f);
			}
		};
		
		out = new PrintWriter(outputDir+"\\t.t", "UTF-8");
		OutputProcessor op = new OutputProcessor() {
			public void write(List<Recipe> recipes) {
				System.out.println("FOUND: "+recipes.size());
				outputFormatter.writeFileHeader(out);
				outputFormatter.writeRecipe(out, recipes);
			}
			public void endFile() {
				outputFormatter.writeFileTail(out);
				out.close();
			}
			public void startFile(String name) {
				startFile(new File(name));
			}
			public void startFile(File file) {
				if (! file.getName().toLowerCase().endsWith("mc2")) return;
				// empty the folder list
				Folder.getAll().clear();
				// new file
				System.out.println("Opening: "+file.getAbsolutePath());
				//String fdx = name.replaceAll("^.*[\\/]", "");
				String fdx = file.getAbsolutePath().replaceAll("(?i)mc2$", fileExtension);
				try {
					out = new PrintWriter(fdx, "UTF-8");
				} catch (FileNotFoundException e) {
		            throw new RecipeFoxException(e);
				} catch (UnsupportedEncodingException e) {
		            throw new RecipeFoxException(e);
				}
				String dir = file.getAbsolutePath().replaceAll("[\\\\/][^\\\\/]*$", "");
				//System.out.println("Setting dir="+dir);
				outputFormatter.setImageDir(dir);
			}
		}; 
		
		FileProcessor fileProcessor = new FileProcessor(ip, op);
		fileProcessor.process(outputDir);
		outputFormatter.writeFileTail(out);
		out.close();
	}

	public void testConvertASingleFile() throws FileNotFoundException, UnsupportedEncodingException {
		if (true) return;
		Configuration.setBooleanProperty("USE_FRACTIONS_IN_AMOUNTS", true);
		Configuration.setBooleanProperty("MERGE_INGREDIENT_LINE_CONTINUATIONS", true);
		Configuration.setBooleanProperty("SPLIT_INGREDIENT_LINES_WITH_PLUS", true);
		Configuration.setBooleanProperty("DETECT_AND_MARK_SERVINGS_FROM_YIELD", true);
		Configuration.setBooleanProperty("MOVE_NOTES_IN_DIRECTIONS_TO_NOTES", true);
		Configuration.setBooleanProperty("REMOVE_NON_RECIPE_COMMENTS_FROM_DIRECTIONS", false);
		Configuration.setBooleanProperty("REMOVE_NUTRITION_INFORMATION_FROM_DIRECTIONS", false);
		Configuration.setBooleanProperty("DETECT_AND_MARK_SERVINGS_FROM_DIRECTIONS", true);
		Configuration.setBooleanProperty("EXPAND_UNIT_ABBREVIATIONS", true);
		Configuration.setBooleanProperty("CALCULATE_WEIGHT_FROM_AMOUNT_TIMES_PACKAGE_SIZE", false);
		Configuration.setBooleanProperty("USE_WEIGHT_NOT_PACKAGE_SIZE", false);
		Configuration.setBooleanProperty("INGREDIENT_PROCESSING_WORDS_TO_PREPARATION", false);
		Configuration.setBooleanProperty("PLURALISE_UNITS", true);
		Configuration.setBooleanProperty("MOVE_TEXT_AFTER_COMMAS_TO_PREPARATION", true);
		Configuration.loadSafeCommaStructuresFromFile("commas.txt");
		
		Mc2Reader reader = new Mc2Reader();
		// add cookbook name as a category ?
		reader.setAddCookbookTitleAsCategory(false);
		
		String cookbook = "Judy - My Cookbook";
		List<Recipe> recipes = reader.read(new File("C:\\temp\\convert\\"+cookbook+".mc2"));
		System.out.println("Read "+recipes.size()+" recipes");
		
		// normalize
		for (Recipe r : recipes) {
			r.normalize();
		}
		
		
		RecipeFileWriter writer = new RecipeFileWriter(formatter, 200, "C:\\temp\\convert\\"+cookbook+"-%d.fdx");
		writer.setKeepFilenames(false);
		writer.write(recipes);
		writer.close();
	}
	
	public void testEscapeLinebreaks() {
		assertEquals("test1&#xD;&#xA;test2&#xD;&#xA;", formatter.escapeLinebreaks("test1\ntest2\n"));
		assertEquals("test1&#xD;&#xA;test2&#xD;&#xA;", formatter.escapeLinebreaks("test1\n\rtest2\n\r"));
	}
	
	public void testNotes() {
		Recipe r = new Recipe();
		r.setTitle("title");
		r.setNote("note");
		String str = formatter.recipeAsString(r);
		assertNotNull(str);
		assertTrue(str.contains("Comments=\"note\""));
		// without notes and descr
		r.setNote(null);
		str = formatter.recipeAsString(r);
		assertFalse(str.contains("Comments="));
		// with only descr
		r.setDescription("descr");
		str = formatter.recipeAsString(r);
		assertTrue(str.contains("Comments=\"descr\""));
	}
	
	public void testReaderSimple() {
		LineNumberReader reader = getReader("src/test/data/FDX/Steakhouse Grinder.fdx");

		List<Recipe> all = formatter.readRecipes(reader);
		assertEquals(1, all.size());
		Recipe recipe = all.get(0);
		assertTrue(recipe != null);
		assertEquals("\"Steakhouse\" Grinder", recipe.getTitle());
		assertTrue(recipe.getNote().startsWith("Special Extra For"));
		assertEquals(1, recipe.getServings());
		assertEquals(10, recipe.getPreparationTime());
		assertEquals(10, recipe.getTotalTime());
		assertEquals("kraftfoods.com", recipe.getSource());
		assertEquals("http://www.kraftrecipes.com/recipes/steakhouse-grinder-114182.aspx", recipe.getUrl());
		assertEquals(7, recipe.getIngredients().size());
		RecipeIngredient ingr = recipe.getIngredients().get(2);
		assertEquals(1/3.0f, ingr.getAmount(), 0.01);
		assertEquals("cup", ingr.getUnit().getName());
		assertEquals("thin fresh mushroom slices", ingr.getIngredient().getName());
		assertEquals(3, recipe.getDirections().size());
		assertNotNull(recipe.getDirectionImage(0));
		assertNotNull(recipe.getDirectionImage(1));
		assertNotNull(recipe.getDirectionImage(2));
		assertEquals(1, recipe.getImages().size());
		
	}

	public void testAllFields() {
		LineNumberReader reader = getReader("src/test/data/FDX/allfields.fdx");

		List<Recipe> all = formatter.readRecipes(reader);
		assertEquals(1, all.size());
		Recipe recipe = all.get(0);
		assertTrue(recipe != null);
		assertEquals("RecipeName", recipe.getTitle());
		assertEquals(3, recipe.getCategories().size());
		assertEquals("12 very long units", recipe.getYield());
		assertEquals("Copyright", recipe.getCopyright());
		assertEquals(6, recipe.getDirections().size());
		assertEquals("Tips 1", recipe.getDirections().get(2));
		assertEquals("Author note1", recipe.getDirections().get(4));
	}

	public void testReaderTechnique() {
		LineNumberReader reader = getReader("src/test/data/FDX/Technique.fdx");

		List<Recipe> all = formatter.readRecipes(reader);
		assertEquals(1, all.size());
		Recipe recipe = all.get(0);
		assertTrue(recipe != null);
		assertEquals("Test technique", recipe.getTitle());
		assertEquals(2, recipe.getDirections().size());
		assertEquals("Comments", recipe.getNote());
		assertEquals("Source", recipe.getSource());
		assertEquals("webpage", recipe.getUrl());
		assertEquals("Copyright", recipe.getCopyright());
	}

	public void testChapters() {
		Cookbook.clear();
		LineNumberReader reader = getReader("src/test/data/FDX/Chapters.fdx");

		List<Recipe> all = formatter.readRecipes(reader);
		assertEquals(1, all.size());
		// test the cookbook
		assertEquals(3, Cookbook.getAll().size());
		Folder cookbook = Folder.getAll().get(0);
		assertEquals("Test cookbook", cookbook.getName());
		assertEquals(1, cookbook.getChildren().size());
		assertEquals("comments", cookbook.getDescription());
		assertEquals("Chapter 1", cookbook.getChildren().get(0).getName());
		// test the chapters
		assertEquals(3, Folder.getAll().size()); // incl the default top chapter
		Folder chapter = all.get(0).getFolder();
		assertTrue(chapter != null);
		assertEquals("Chapter 1.2", chapter.getName());
		assertEquals("Chapter 1", chapter.getParent().getName());
		assertEquals("Test cookbook", chapter.getParent().getParent().getName());
		assertNull(chapter.getParent().getParent().getParent());
		
	}

	public void testWritingChapters() {
		Cookbook.clear();
		LineNumberReader reader = getReader("src/test/data/FDX/Chapters.fdx");
		@SuppressWarnings("unused")
		List<Recipe> all = formatter.readRecipes(reader);
		
		// test
		String str = formatter.fileHeaderAsString();
		assertTrue(str.contains("<Cookbook Name=\"Test cookbook\" ID=\"22\" Comments=\"comments\""));
		assertTrue(str.contains("<CookbookChapter Name=\"Chapter 1.2\" ID=\"157\" CookbookID=\"22\" ParentChapterID=\"153\""));
	}
	
	public void testIgnoreBOM() {
		LineNumberReader reader = getReader("src/test/data/FDX/File-with-BOM.fdx");
	    //LineNumberReader reader = getReader("/pr/downloads/temp/Test Recipe.fdx");

		List<Recipe> all = formatter.readRecipes(reader);
		assertEquals(1, all.size());
	}
	
    public void testIgnoreBOMWithOtherMethods() {
        // using factory
        RecipeFormatter f1 = FormatterFactory.getFormatter("FDX");
        List<Recipe> all = f1.read(new File("src/test/data/FDX/File-with-BOM.fdx"));
        assertEquals(1, all.size());
        // using readRecipes
        all = formatter.readRecipes(new File("src/test/data/FDX/File-with-BOM.fdx"));
        assertEquals(1, all.size());
        
    }
    
	public void testCookInactiveTime() {
		Recipe recipe = new Recipe();
		recipe.setTitle("test");
		recipe.setCookTime(11);
		recipe.setTime("INACTIVE", 12);
		String str = formatter.recipeAsString(recipe);
		assertTrue(str.contains("CookingTime=\"11\""));
		assertTrue(str.contains("InactiveTime=\"12\""));
	}
	
	public LineNumberReader getReader(String filename) {
		LineNumberReader bufReader = null;
		try {
			bufReader = new LineNumberReader(new InputStreamReader(new FileInputStream(filename), formatter.getDefaultCharacterSet()));
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new RecipeFoxException(e);
		}
		return bufReader;
	}

}
