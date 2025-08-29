/**
 * 
 */
package net.sf.recipetools.javarecipes.utils;

import static org.junit.Assert.*;
import net.sf.recipetools.javarecipes.format.MasterCookBinary.Blast;

import org.junit.Test;

/**
 * @author Frank
 *
 */
public class BlastTest {

	@Test
	public void testBasic() {
		Blast b = new Blast();
		byte[] t = new byte[] {0, 4, 0x82-256, 0x24, 0x25, 0x8f-256, 0x80-256, 0x7f};
		byte[] result = b.blast(t);
		assertEquals("AIAIAIAIAIAIA", new String(result));
	}
}
