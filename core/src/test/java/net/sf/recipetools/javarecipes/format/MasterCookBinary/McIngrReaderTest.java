/**
 * 
 */
package net.sf.recipetools.javarecipes.format.MasterCookBinary;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import net.sf.recipetools.javarecipes.model.Recipe;

import org.junit.Test;

/**
 * @author Frank
 *
 */
public class McIngrReaderTest {
	@Test
	public void testReadingRecipes() {
		McIngrReader reader = new McIngrReader();
		List<Recipe> recipes = reader.read(new File("src/test/data/MC-ingredient/MC Ingredients.ing"));
		//Recipe[] recipes = reader.read(new File("C:/temp/MC Ingredients-jbs.ing"));
		assertEquals(0, recipes.size());
	}

}
