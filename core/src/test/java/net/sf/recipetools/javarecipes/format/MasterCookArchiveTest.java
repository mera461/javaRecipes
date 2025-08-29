/**
 * 
 */
package net.sf.recipetools.javarecipes.format;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import net.sf.recipetools.javarecipes.model.Recipe;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Frank
 *
 */
public class MasterCookArchiveTest {

	MasterCookArchive formatter = new MasterCookArchive(); 
	
	public MasterCookArchiveTest() {
		formatter.setTheMasterCookProgram(new File("C:\\Program Files\\MasterCook 14\\Program\\Mastercook14.exe"));
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
	}

	/**
	 * Test method for {@link net.sf.recipetools.javarecipes.format.MasterCookArchive#read(java.io.File)}.
	 */
	@Test
	public void testRead() {
		List<Recipe> recipes = formatter.read(new File("src/test/data/MZ2/MC14 Beta 5 Export.mz2"));
		assertNotNull(recipes);
		assertEquals(177, recipes.size());
		// count images
		int imgCount = 0;
		for (Recipe r: recipes) {
			if (r.getImages().size()>0) imgCount++;
		}
		assertEquals(31, imgCount);
	}

	/**
	 * Test writing a mz2
	 */
	@Test
	public void testWrite() {
		LivingCookbookFDX lc = new LivingCookbookFDX();
		List<Recipe> recipes = lc.read(new File("src/test/data/FDX/allfields.fdx"));
		assertNotNull(recipes);
		assertEquals(1, recipes.size());
		String name = "c:\\temp\\junit.mz2";
		File f = new File(name);
		f.delete();
		formatter.startFile(f);
		formatter.write(recipes);
		formatter.endFile();
		assertTrue(f.exists());
	}

}
