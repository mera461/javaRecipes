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
public class LivingCookbookArchiveTest {

	LivingCookbookArchive formatter = new LivingCookbookArchive(); 
	
	public LivingCookbookArchiveTest() {
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
	public void testSingleRead() {
		List<Recipe> recipes = formatter.read(new File("src/test/data/FDXZ/hr-Whipped_Potatoes_with_Garlic_and_Cheese.fdxz"));
		assertNotNull(recipes);
		assertEquals(1, recipes.size());
		assertEquals("Whipped Potatoes with Garlic and Cheese", recipes.get(0).getTitle());
		assertEquals(1, recipes.get(0).getImages().size());
	}

    @Test
    public void testMultipleRecipes() {
    	List<Recipe> recipes = formatter.read(new File("src/test/data/FDXZ/Chili.fdxz"));
        assertNotNull(recipes);
        assertEquals(93, recipes.size());
        // count images
        int imgCount = 0;
        for (Recipe r: recipes) {
            imgCount+=r.getImages().size();
            imgCount+=r.getDirectionImages().size();
        }
        assertEquals(104, imgCount);
    }

    /**
	 * Test writing a mz2
	 */
//	@Test
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
