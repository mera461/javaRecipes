/*
 * Created on 26-10-2004
 *
 */
package net.sf.recipetools.javarecipes.format;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.List;
import java.util.regex.Pattern;

import junit.framework.TestCase;
import net.sf.recipetools.javarecipes.model.Configuration;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;
import net.sf.recipetools.javarecipes.model.RecipeIngredient;

/**
 * @author ft
 *
 */
public class MealMasterTest extends TestCase {

	/*
	 * @see TestCase#setUp()
	 */
	private MealMaster mm = null; 
	protected void setUp() throws Exception {
		super.setUp();
		mm = new MealMaster();
		Configuration.setBooleanProperty("INGREDIENT_PROCESSING_WORDS_TO_PREPARATION", true);
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		mm = null;
	}

	public void testReadRecipe1() {
		LineNumberReader bufReader = null;
		try {
			bufReader = new LineNumberReader(new FileReader("src/test/data/1000.mmf"));
		} catch (FileNotFoundException e) {
            throw new RecipeFoxException(e);
		}

		Recipe recipe = null;
		recipe = mm.readRecipes(bufReader).get(0);
		assertTrue(recipe != null);
		assertEquals("\"BE MINE\" LOLLIPOPS", recipe.getTitle());
		assertEquals("Candies", recipe.getCategories().get(0).getName());
		assertEquals("Valentine", recipe.getCategories().get(1).getName());
		assertEquals("8 Servings", recipe.getYield());
		RecipeIngredient ingr = recipe.getIngredients().get(0); 
		assertEquals("Text only", ingr.getIngredient().getName());
		assertTrue(recipe.getDirectionsAsString().startsWith("Source: Better Homes and Garden"));
		
		// read recipe no 2
		recipe = null;
		recipe = mm.readRecipes(bufReader).get(0);
		assertTrue(recipe != null);
		assertEquals("\"BLUE\" FETTUCCINE", recipe.getTitle());
		assertEquals("Pasta", recipe.getCategories().get(0).getName());
		assertEquals("4 Servings", recipe.getYield());

		ingr = recipe.getIngredients().get(0); 
		assertEquals(4f, ingr.getAmount(), 0.01);
		assertEquals("oz", ingr.getUnit().getName());
		assertEquals("Danish blue cheese or 8 oz.", ingr.getIngredient().getName());

		ingr = recipe.getIngredients().get(3);
		assertEquals(0.25f, ingr.getAmount(), 0.01);
		assertEquals("c", ingr.getUnit().getName());
		assertEquals("Marinated, dried tomatoes", ingr.getIngredient().getName());

		ingr = recipe.getIngredients().get(9); 
		assertEquals(1.5f, ingr.getAmount(), 0.01);
		assertEquals("ts", ingr.getUnit().getName());
		assertEquals("Finely chopped fresh basil,", ingr.getIngredient().getName());
		
		assertTrue(Pattern.compile("On waxed paper").matcher(recipe.getDirectionsAsString()).find());
	}
	
	public void testIgnoreText() {
		String txt = "dummy 1\n"
			+ "MMMMM----- Recipe via Meal-Master (tm) v8.04\n\n"
			+ "Title: Anzac Biscuits (Cookies)\n"
			+ "Categories: Cookies\n"
			+ "Yield: 1 servings\n\n"
			+ "3/4 c  Fine sugar\n\n"
			+ "ANZAC day is celebrated in Australia on April 25\n\n"
			+ "MMMMM\n\n"
			+ "dummy text 2";
		
		LineNumberReader reader = new LineNumberReader(new StringReader(txt));
		List<Recipe> recipes = mm.readRecipes(reader);
		assertTrue(recipes != null);
		assertEquals(1, recipes.size());
	}

	public void testMapUnitToMealMaster() {
		assertNull(mm.mapUnitToMealMaster("abc"));
		assertNull(mm.mapUnitToMealMaster("stalk"));
		assertEquals("c", mm.mapUnitToMealMaster("cup"));
		assertEquals("tb", mm.mapUnitToMealMaster("tablespoon"));
	}
	
	public void testReadRecipeCount() {
		LineNumberReader bufReader = null;
		try {
			bufReader = new LineNumberReader(new FileReader("src/test/data/1000.mmf"));
		} catch (FileNotFoundException e) {
            throw new RecipeFoxException(e);
		}
		
		
		int count = 0;
		long start = System.currentTimeMillis();
		List<Recipe> recipes = mm.readRecipes(bufReader);
		while (recipes!= null && recipes.size() != 0) {
			count+=recipes.size();
			recipes = mm.readRecipes(bufReader);
		}
		assertEquals(1000, count);
		long time = System.currentTimeMillis() - start;
		System.out.println("Read 1000 recipes in "+(time/1000.0)+ " seconds");
	}

	
	public void testWrite() {
		LineNumberReader bufReader = null;
		try {
			bufReader = new LineNumberReader(new FileReader("src/test/data/1000.mmf"));
		} catch (FileNotFoundException e) {
            throw new RecipeFoxException(e);
		}

		List<Recipe> recipes = null;
		recipes = mm.readRecipes(bufReader); // the first has no ingredients
		recipes = mm.readRecipes(bufReader); // get the second recipe
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PrintWriter out = new PrintWriter(bos);
		mm.writeRecipe(out, recipes);
		out.close();
		
		System.out.print(bos.toString());
		
		bufReader = new LineNumberReader(new CharArrayReader(bos.toString().toCharArray()));
		List<Recipe> recipe1 = mm.readRecipes(bufReader);
		
		Recipe r = recipes.get(0);
		Recipe r1= recipe1.get(0);
		
		assertEquals(r.getAuthor(), r1.getAuthor());
//		assertEquals(recipe.getDirectionsAsString(), recipe1.getDirectionsAsString());
		assertEquals(r.getNote(), r1.getNote());
		assertEquals(r.getPreparationTime(), r1.getPreparationTime());
		assertEquals(r.getServings(), r1.getServings());
		assertEquals(r.getSource(), r1.getSource());
		assertEquals(r.getTitle(), r1.getTitle());
		assertEquals(r.getYield(), r1.getYield());
		assertEquals(r.getIngredients().size(), r1.getIngredients().size());

		for (int i=0; i<r.getIngredients().size(); i++) {
			assertEquals(r.getIngredients().get(i).getAmount(),
						 r1.getIngredients().get(i).getAmount(),0.01);
			if (r.getIngredients().get(i).hasUnit() || r1.getIngredients().get(i).hasUnit()) {
				assertEquals(r.getIngredients().get(i).getUnit().getName(),
							 r1.getIngredients().get(i).getUnit().getName());
			}
			assertEquals(r.getIngredients().get(i).getIngredient().getName(),
					 r1.getIngredients().get(i).getIngredient().getName());
			assertEquals(r.getIngredients().get(i).getProcessing(),
					 r1.getIngredients().get(i).getProcessing());
		}
		
 	}	

	public void testWriteEmptyRecipe() {
		Recipe recipe = new Recipe();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PrintWriter out = new PrintWriter(bos);
		mm.writeRecipe(out, recipe);
		out.close();
		String str = bos.toString();
		assertNotNull(str);
		assertTrue(str.startsWith("MMMMM"));
	}

	
	
	public void testRecipeAsString() {
		LineNumberReader bufReader = null;
		try {
			bufReader = new LineNumberReader(new FileReader("src/test/data/1000.mmf"));
		} catch (FileNotFoundException e) {
            throw new RecipeFoxException(e);
		}

		Recipe recipe = null;
		recipe = mm.readRecipes(bufReader).get(0);
		
		String str = mm.recipeAsString(recipe);
		assertNotNull(str);
		assertTrue(str.startsWith("MMMMM"));
		
	}
	
	public void testRecipeAsStringWithLongIngredient() {
		Recipe recipe = new Recipe();
		recipe.setTitle("test title");
		recipe.addIngredient(new RecipeIngredient(1, "cup", "chopped ginger                                "));
		recipe.normalize();
		String str = mm.recipeAsString(recipe);
		assertTrue(str.startsWith("MMMMM"));
		assertTrue(str.contains("   1.00  c ginger -- chopped"));
	}

	public void testSplitStringWithIndent() {
		assertEquals("test", mm.splitStringWithIndent("test",2,6));
		assertEquals("testte\n  - st", mm.splitStringWithIndent("testtest",2,6));
		assertEquals("testte\n  - stte\n  - st", mm.splitStringWithIndent("testtesttest",2,6));
	}
	
}
