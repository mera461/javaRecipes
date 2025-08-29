/**
 * 
 */
package net.sf.recipetools.javarecipes.format.cookenpro;


import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import net.sf.recipetools.javarecipes.fileprocessor.BinaryInputProcessor;
import net.sf.recipetools.javarecipes.fileprocessor.FileProcessor;
import net.sf.recipetools.javarecipes.fileprocessor.OutputProcessor;
import net.sf.recipetools.javarecipes.model.Configuration;
import net.sf.recipetools.javarecipes.model.Folder;
import net.sf.recipetools.javarecipes.model.Recipe;

import org.junit.Before;
import org.junit.Test;


/**
 * @author Frank
 *
 */
public class CookenProBinaryReaderTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		Folder.clear();
		Configuration.setBooleanProperty("USE_FRACTIONS_IN_AMOUNTS", false);
		Configuration.setBooleanProperty("PLURALISE_UNITS", true);
	}
	
	@Test
	public void testReadSimpleRecipe() {
		// test version 9
		CookenProBinaryReader reader = new CookenProBinaryReader(new File("src/test/data/CookenProV9Binary/t.dvo"));
		List<Recipe> recipes = reader.readAllRecipes();
		assertEquals(367, recipes.size());
		// test version 8
		/*** DOES not work on 64-bit windows
		reader = new CookenProBinaryReader(new File("src/test/data/CookenProBinary/t.dvo"));
		recipes = reader.readAllRecipes();
		assertEquals(1, recipes.size());
		*/
	}

	@Test
	public void testAllMyFiles() {
		if (true)
			return;
		final CookenProBinaryReader reader = new CookenProBinaryReader();
		BinaryInputProcessor ip = new BinaryInputProcessor() {
			public List<Recipe> read(File f) {
				System.out.println("Reading from: " + f.getName());
				if (f.getName().endsWith("dvo")) {
					return reader.read(f);
				} else {
					return null;
				}
			}
		};
		OutputProcessor op = new OutputProcessor() {
			public void write(List<Recipe> recipes) {
				// test normalize them
				for (Recipe r: recipes) {
					r.normalize();
				}
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
		fileProcessor.process("Y:/data/opskrifter/CookenPro - Binary");
	}

	
}
