/**
 * 
 */
package net.sf.recipetools.javarecipes.format;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeIngredient;

/**
 * @author frank
 *
 */
public class JsonFormatterTest {
	
	JsonFormatter formatter = new JsonFormatter();

	@Test
	public void testFromJson() throws IOException {
		java.nio.file.Path path = Paths.get("src/test/data/json/recipes1.json");
		String json = new String (Files.readAllBytes(path));
		List<Recipe> recipes = formatter.recipeFromJson(json);
		assertEquals(2, recipes.size());
		assertEquals("Meatball Sandwich", recipes.get(0).getTitle());
		assertEquals("Swedish Meatball Casserole", recipes.get(1).getTitle());
		// check first recipe
		Recipe r = recipes.get(0);
		assertEquals("4.53", r.getWine());
		assertEquals(40, r.getTotalTime());
		assertEquals(13, r.getIngredients().size());
		
	}
	
	@Test
	public void testJsonWithoutIngredients() {
		Recipe recipe = new Recipe("Test");
		assertEquals("{title:\"Test\",}", formatter.recipeToJson(recipe));
	}

	@Test
	public void testJsonWithImage() throws IOException {
		java.nio.file.Path path = Paths.get("src/test/data/json/recipe w image from RecipeFox.json");
		String json = new String (Files.readAllBytes(path));
		List<Recipe> recipes = formatter.recipeFromJson(json);
		assertEquals(1, recipes.size());
		Recipe r = recipes.get(0);
		assertEquals("Blueberry Gorgonzola Salad", r.getTitle());
		assertEquals(1, recipes.get(0).getImages().size());
	}

	@Test
	public void testWithIntegerFields() throws IOException {
		Path path = Paths.get("src/test/data/json/recipes2.json");
		String json = new String (Files.readAllBytes(path));
		List<Recipe> recipes = formatter.recipeFromJson(json);
		assertEquals(1, recipes.size());
	}	
	
	
	@Test
	public void testToJson() throws IOException {
		Path path = Paths.get("src/test/data/json/recipes1.json");
		String json = new String (Files.readAllBytes(path));
		List<Recipe> recipes = formatter.recipeFromJson(json);
		assertEquals(2, recipes.size());
		
		String json1 = formatter.recipeToJson(recipes);
		System.out.println("orignal:"+json);
		System.out.println("reprocessed:"+json1);
		
		// reprocessing it
		List<Recipe> r1 = formatter.recipeFromJson(json1);
		assertEquals(2, r1.size());
	}

	@Test
	public void testJson1() {
		RecipeIngredient ingr = new RecipeIngredient(1.0f, "tsp", "water", "hot");
		String str = formatter.recipeIngredientToJson(ingr);
		assertEquals("\"1 tsp water -- hot\"",
					 str);
	}
	
}
