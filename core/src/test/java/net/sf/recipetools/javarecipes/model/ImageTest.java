/**
 * 
 */
package net.sf.recipetools.javarecipes.model;


import net.sf.recipetools.javarecipes.model.Image.ImageFormat;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Frank
 *
 */
public class ImageTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testImageExtension() {
		Image image = new Image("name", "src/test/data/images/test.gif");
		assertEquals("GIF", image.getImageType());
		image = new Image("name", "src/test/data/images/test1.jpg");
		assertEquals("JPG", image.getImageType());
		image = new Image("name", "src/test/data/images/test2.png");
		assertEquals("PNG", image.getImageType());
	}
	
	@Test
	public void testConvert() {
		Image image = new Image("name", "src/test/data/images/test.gif");
		assertEquals("GIF", image.getImageType());
		image.convertTo(ImageFormat.PNG);
		assertEquals("PNG", image.getImageType());
		image.convertTo(ImageFormat.JPEG);
		assertEquals("JPG", image.getImageType());
		image = new Image("name", "src/test/data/images/test1.jpg");
		assertEquals("JPG", image.getImageType());
		image.convertTo(ImageFormat.PNG);
		assertEquals("PNG", image.getImageType());
		image.convertTo(ImageFormat.GIF);
		assertEquals("GIF", image.getImageType());
	}
	
}
