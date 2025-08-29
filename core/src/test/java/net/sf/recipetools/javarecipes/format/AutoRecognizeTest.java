/**
 * 
 */
package net.sf.recipetools.javarecipes.format;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.List;

import net.sf.recipetools.javarecipes.model.Recipe;

import org.junit.Before;
import org.junit.Test;


/**
 * @author Frank
 *
 */
public class AutoRecognizeTest {
	
	private AutoRecognize auto = null; 
	
	@Before
	public void setUp() throws Exception {
		auto = new AutoRecognize();
	}
	
	
	@Test
	public void testAutoformatting() {
		// TODO: FDX
		assertNotNull(readFile("src/test/data/0,2625,appetiz1,00.mxp"));
		assertNotNull(readFile("src/test/data/ice_creammx2.mx2"));
		assertNotNull(readFile("src/test/data/mctagit/simple.txt")); // McTagIt
		assertNotNull(readFile("src/test/data/1000.mmf")); // Meal Master
		assertNotNull(readFile("src/test/data/nycexport/cake.txt")); // NYC export
		assertNotNull(readFile("src/test/data/rpw/fav.txt")); // RecipeFormatter2000
	}

	List<Recipe> readFile(String filename) {
		LineNumberReader bufReader = null;
		try {
			bufReader = new LineNumberReader(new FileReader(filename));
		} catch (FileNotFoundException e) {
			fail("Expected not exception");
		}

		return auto.readRecipes(bufReader);
	}
	
}
