/**
 * 
 */
package net.sf.recipetools.javarecipes.format;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.List;

import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;
import net.sf.recipetools.javarecipes.model.RecipeIngredient;

import org.junit.Test;

/**
 * @author Frank
 *
 */
public class AdvantageCookingTest {
	
	AdvantageCooking reader = new AdvantageCooking(); 

	/**
	 * Test method for {@link net.sf.recipetools.javarecipes.format.AdvantageCooking#splitCsv(java.lang.String)}.
	 */
	@Test
	public void testSplitCsv() {
		List<String> str = reader.splitCsv("6,\"Ounce\",1,\"Ounce\",1,\"Ounce\"");
		assertEquals("6",str.get(0));
		assertEquals("Ounce",str.get(1));
		assertEquals("1",str.get(2));
		assertEquals("Ounce",str.get(3));
		assertEquals("1",str.get(4));
		assertEquals("Ounce",str.get(5));
		str = reader.splitCsv("6,\"l1~l2\",,3");
		assertEquals("6",str.get(0));
		assertEquals("l1\nl2",str.get(1));
		assertEquals("",str.get(2));
		assertEquals("3",str.get(3));
	}
	
	@Test
	public void testSimpleHeader() {
		String str = 
		"RecipeTitle=\"Build-Your-Own Nachos\"\n"+
		"SetItems=52,\"APPLIANCE\",\"Fry Pan\",\"Fry Pan\",\"\",,\"Y\"\n" +
		"SetItems=53,\"RECIPE CAT\",\"Hamburger, fried\",\"Hamburger, fried\",\"\",,\"Y\"\n"+
		"Recipes=58,\"Build-Your-Own Nachos\",\"Cooking  Day:~Fry hamburger with onion; drain. Add taco seasoning and tomato sauce; simmer for 5 minutes. Cool and place in a quart-size freezer bag. Freeze.~~~To Serve:~Thaw and reheat meat. Combine soup and milk and heat.~~Everyone builds their own nachos by starting with the tortillas chips and covering them with the desired condiments. Top with hot fiesta nacho cheese soup.~~seasoned meat~refried beans~sour cream~salsa~olives~tomatoes~onions~mexican rice~shredded cheese\",\"Hamburger, fried\",\"1 meal\",\"Fry Pan\",\"Source\",\"12 minutter\"\n";

		Recipe recipe = readRecipesFromString(str);
		assertEquals("Build-Your-Own Nachos", recipe.getTitle());
		assertEquals("Fry Pan", recipe.getCategories().get(0).getName());
		assertEquals("Hamburger, fried", recipe.getCategories().get(1).getName());
		assertEquals("1 meal", recipe.getYield());
		assertTrue(recipe.getDirectionsAsString().contains("Cooking  Day:"));
		assertEquals(4,recipe.getDirections().size());
	}
	
	@Test
	public void testSimpleIngredients() {
		String str = 
			"Recipes=58,\"Build-Your-Own Nachos\",\"Cooking  Day:~Fry hamburger with onion; drain. Add taco seasoning and tomato sauce; simmer for 5 minutes. Cool and place in a quart-size freezer bag. Freeze.~~~To Serve:~Thaw and reheat meat. Combine soup and milk and heat.~~Everyone builds their own nachos by starting with the tortillas chips and covering them with the desired condiments. Top with hot fiesta nacho cheese soup.~~seasoned meat~refried beans~sour cream~salsa~olives~tomatoes~onions~mexican rice~shredded cheese\",\"Hamburger, fried\",\"1 meal\",\"Fry Pan\",\"Source\",\"12 minutter\"\n"+
			"SetItems=39,\"WHEN\",\"Cooking Day\",\"Cooking Day\",\"CD\",,\"Y\"\n"+
			"SetItems=20,\"ACTION\",\"Fry\",\"Fry\",\"\",,\"Y\"\n"+
			"SetItems=9,\"INGRED TYP\",\"Meat\",\"Meat\",\"\",,\"\"\n"+
			"Ingredients=16,\"Hamburger (to fry)\",\"Fry\",\"Meat\"\n"+
			"Measures=6,\"Ounce\",1,\"Ounce\",1,\"Ounce\"\n"+
			"Measures=15,\"Pound\",16,\"Ounce\",16,\"Ounce\"\n"+
			"RecipeIngredients=326,\"Build-Your-Own Nachos\",\"1\",1,\"Pound\",\"Hamburger (to fry)\",\"Cooking Day\",\"(or 2+ cups fried hamburger)\"\n"+
			"SetItems=39,\"WHEN\",\"Cooking Day\",\"Cooking Day\",\"CD\",,\"Y\"\n"+
			"SetItems=16,\"ACTION\",\"Chop\",\"Chop\",\"\",,\"Y\"\n"+
			"SetItems=12,\"INGRED TYP\",\"Produce\",\"Produce\",\"\",,\"\"\n"+
			"Ingredients=17,\"Onion, chopped\",\"Chop\",\"Produce\"\n"+
			"Measures=5,\"Teaspoon\",1,\"Teaspoon\",1,\"Teaspoon\"\n"+
			"Measures=9,\"Cup\",48,\"Teaspoon\",48,\"Teaspoon\"\n"+
			"RecipeIngredients=327,\"Build-Your-Own Nachos\",\"1/2\",0.5,\"Cup\",\"Onion, chopped\",\"Cooking Day\",\"\"\n"+
			"SetItems=31,\"CONTAINER\",\"Freezer Bag - Quart\",\"Freezer Bags - Quart\",\"\",,\"Y\"\n"+
			"RecipeContainers=7,\"Build-Your-Own Nachos\",1,\"Freezer Bag - Quart\",\"Freezer Bags - Quart\"\n";
			
		Recipe recipe = readRecipesFromString(str);
		List<RecipeIngredient> ingr = recipe.getIngredients();
		assertEquals(5, ingr.size());
		assertEquals("On cooking day:", ingr.get(0).getIngredient().getName());
		assertEquals(1f, ingr.get(1).getAmount(), 0.01);
		assertEquals("Pound", ingr.get(1).getUnit().getName());
		assertEquals("Hamburger (to fry)", ingr.get(1).getIngredient().getName());
		assertEquals(0.5f, ingr.get(2).getAmount(), 0.01);
		assertEquals("Cup", ingr.get(2).getUnit().getName());
		assertEquals("Onion, chopped", ingr.get(2).getIngredient().getName());
		assertEquals("Containers:", ingr.get(3).getIngredient().getName());
		assertEquals(1f, ingr.get(4).getAmount(), 0.01);
		assertEquals("Freezer Bag - Quart", ingr.get(4).getIngredient().getName());
		
	}

	public void testAllFiles() {
		
	}
	
	
	
	Recipe readFile(String filename) {
		LineNumberReader bufReader = null;
		try {
			bufReader = new LineNumberReader(new FileReader(filename));
		} catch (FileNotFoundException e) {
            throw new RecipeFoxException(e);
		}
		return reader.readRecipes(bufReader).get(0);
	}

	public Recipe readRecipesFromString(String str) {
		LineNumberReader linereader = new LineNumberReader(new StringReader(str));
		return  reader.readRecipes(linereader).get(0);
	}

	
}
