/**
 * 
 */
package net.sf.recipetools.javarecipes.format;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.sf.recipetools.javarecipes.model.Configuration;

/**
 * @author frank
 *
 */
public class FormattingUtilsTest {
	@Test
	public void testFormatter() {
		Configuration.setBooleanProperty("USE_FRACTIONS_IN_AMOUNTS", false);
		assertEquals("4.1", FormattingUtils.formatNumber(4.1));
		assertEquals("4.72", FormattingUtils.formatNumber(4.72));
		assertEquals("4", FormattingUtils.formatNumber(4));
		Configuration.setBooleanProperty("USE_FRACTIONS_IN_AMOUNTS", true);
		assertEquals("4 1/10", FormattingUtils.formatNumber(4.1));
		assertEquals("2 3/4", FormattingUtils.formatNumber(2.74585));
		assertEquals("27 1/2", FormattingUtils.formatNumber(27.4585));
		assertEquals("274 2/3", FormattingUtils.formatNumber(274.585));
		assertEquals("2745 7/8", FormattingUtils.formatNumber(2745.85));
		assertEquals("27458 1/2", FormattingUtils.formatNumber(27458.5));
	}
	
	@Test
	public void testFormatterAsFraction() {
		Configuration.setBooleanProperty("USE_FRACTIONS_IN_AMOUNTS", true);
		assertEquals("1/3", FormattingUtils.formatNumber(0.33333));
		assertEquals("1/2", FormattingUtils.formatNumber(0.5));
		assertEquals("1 1/2", FormattingUtils.formatNumber(1.5));
		assertEquals("1/4", FormattingUtils.formatNumber(0.25));
		assertEquals("2/5", FormattingUtils.formatNumber(0.4));
		assertEquals("3/8", FormattingUtils.formatNumber(0.375));
		assertEquals("3/10", FormattingUtils.formatNumber(0.3));
		assertEquals("1 3/10", FormattingUtils.formatNumber(1.3));
	}

}
