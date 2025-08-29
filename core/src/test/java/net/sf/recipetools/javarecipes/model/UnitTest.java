/**
 * 
 */
package net.sf.recipetools.javarecipes.model;

import static org.junit.Assert.*;

import org.junit.Test;


/**
 * @author Frank
 *
 */
public class UnitTest {

	/**
	 * Test method for {@link net.sf.recipetools.javarecipes.model.Unit#pluralize()}.
	 */
	@Test
	public void testPluralize() {
		String[][] tests = {
				{"tablespoon", "tablespoons"},
				{"(2-oz) can", "(2-oz) cans"},
				{"loaf", "loaves"},
				{"lb.", "lb."},
				{"kg", "kg"},
//				{"", ""},
		};
	
		for (String[] io: tests) {
			assertEquals(io[1], new Unit(io[0]).pluralize());
		}
	}

	/**
	 * Test method for {@link net.sf.recipetools.javarecipes.model.Unit#isKnown()}.
	 */
	@Test
	public void testIsKnown() {
		String[][] tests = {
				{"tsp..", "t"},
				{"tablespoon(s)", "t"},
				{"tablespoon", "t"},
				{"tsp.", "t"},
				{"tsp", "t"},
				{"tsp(s)", "t"},
				{"tspx(s)", ""},
//				{"", ""},
		};
	
		for (String[] io: tests) {
			assertEquals(io[0], io[1].length() > 0, new Unit(io[0]).isKnown());
		}
	}
	
	/**
	 * Test extract
	 */
	@Test
	public void testExtract() {
		String[][] tests = {
				{"tablespoon(s) sugar", "tablespoon(s)"},
				{"oz", "oz"},
				{"(or some other ingredient)", ""},
				{"ounce can water", "ounce can"},
				{"rounded tsp sugar", "rounded tsp"},
				{"can(s) (28-ounce) water", "can(s) (28-ounce)"},
				{"14 oz can  pineapple", "14 oz can"},
				{"(center-cut) slice ham", "(center-cut) slice"},
				{"(rounded) tsp sugar", "(rounded) tsp"},
				{"cup small onion", "cup"},
				{"tablespoon sugar", "tablespoon"},
				{"tsp(s) sugar", "tsp(s)"},
				{"small cup sugar", "small cup"},
				{"small onion", "small"},
				{"(10-oz) can sugar", "(10-oz) can"},
				{"(2-oz) flank steak", "(2-oz)"},
				{"handful sugar", "handful"},
				{"a handful sugar", "a handful"},
				{"a few onions", "a few"},
				{"a few drops of oil", "a few drops"},
				{"can (10 oz.) enchilada sauce", "can (10 oz.)"},
		};
	
		for (String[] io: tests) {
			assertEquals("Input: "+io[0], io[1], Unit.extract(io[0]).getName());
		}
	}


	@Test
	public void testNormalize() {
		String[][] tests = {
				{"(8.8-oz) can", "(8.8-ounce) can"},
				{"ounce cn", "ounce can"},
				{"oz cn", "ounce can"},
				{"small tbsp", "small tablespoon"},
				{"tablespoon(s)", "tablespoon"},
				{"(10 oz) jar", "(10 ounce) jar"},
		};
	
		Configuration.setBooleanProperty("EXPAND_UNIT_ABBREVIATIONS", true);
		for (String[] io: tests) {
			Unit u = new Unit(io[0]);
			u.normalize();
			assertEquals(io[0], io[1], u.getName());
		}
	}

}
