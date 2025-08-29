/**
 * 
 */
package net.sf.recipetools.javarecipes.format.MasterCookBinary;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;


/**
 * @author g42182
 */
public class McxReaderTest {

	@Test
	public void testReadAllImages() {
		McxReader mcxreader = new McxReader(new File("src/test/data/MC/a_images.mcx"));
		assertEquals(1, mcxreader.imagemap.size()); 
		assertEquals(1, mcxreader.directiveImagemap.size());
		Integer[] keys = mcxreader.directiveImagemap.keySet().toArray(new Integer[0]);
		assertEquals(3, mcxreader.directiveImagemap.get(keys[0]).size());
		mcxreader = new McxReader(new File("src/test/data/MC/Breadworld.mcx"));
		assertEquals(2, mcxreader.imagemap.size()); // 2 images and 2 thumbs
		assertEquals(0, mcxreader.directiveImagemap.size()); // 0 directive images
/*		mcxreader = new McxReader(new File("src/test/data/MC/Vegetarian Food Network Canada.mcx"));
		assertEquals(412, mcxreader.imagemap.size()); 
		assertEquals(0, mcxreader.directiveImagemap.size()); 
		mcxreader = new McxReader(new File("src/test/data/MC/Cooking Light October 2006.mcx" ));
		assertEquals(62, mcxreader.imagemap.size()); 
		assertEquals(0, mcxreader.directiveImagemap.size()); 
*/	
		mcxreader.close();
		}
	
}
