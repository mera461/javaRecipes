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
public class ConfigurationTest {

	@Test
	public void testJson() {
		String fromRecipeFox = "{\"TITLE_CASE_PREFERENCE\":2,\"SUBTITLE_CASE_PREFERENCE\":2,\"USE_FRACTIONS_IN_AMOUNTS\":true,\"PLURALISE_UNITS\":true,\"EXPAND_UNIT_ABBREVIATIONS\":true,\"CALCULATE_WEIGHT_FROM_AMOUNT_TIMES_PACKAGE_SIZE\":true,\"USE_WEIGHT_NOT_PACKAGE_SIZE\":false,\"REMOVE_GRAPHICAL_CHAR_PREFIX_FROM_INGREDIENTS\":true,\"MERGE_INGREDIENT_LINE_CONTINUATIONS\":true,\"SPLIT_INGREDIENT_LINES_WITH_PLUS\":true,\"MARK_ALTERNATE_INGREDIENT_LINES_AS_TEXT\":true,\"MARK_INGREDIENTS_WITH_NO_AMOUNT_OR_UNIT_AS_TEXT\":true,\"MOVE_INGREDIENT_PROCESSING_WORDS_TO_PREPARATION\":false,\"MOVE_PARENTHESIS_IN_INGREDIENTS_TO_PREPARATION\":true,\"MOVE_OPTIONAL_TO_PREPARATION\":true,\"MOVE_TO_TASTE_TO_PREPARATION\":true,\"MOVE_TEXT_AFTER_COMMAS_TO_PREPARATION\":true,\"MOVE_TEXT_AFTER_SEMICOLON_TO_PREPARATION\":true,\"DETECT_AND_MARK_SUBTITLES\":true,\"MOVE_NOTES_IN_DIRECTIONS_TO_NOTES\":true,\"DETECT_AND_MARK_SERVINGS_FROM_DIRECTIONS\":true,\"REMOVE_INCORRECT_LINE_BREAKS_FROM_DIRECTIONS\":true,\"REMOVE_DIRECTION_STEP_NUMBERS\":true,\"DETECT_AND_MARK_SERVINGS_FROM_YIELD\":true,\"COPY_YIELD_TO_DIRECTIONS\":false,\"CATEGORY_SPLIT_CHARS\":\";,\",\"MASTERCOOK_PROGRAM\":\"C:\\\\Program Files\\\\MasterCook 14\\\\Program\\\\Mastercook14.exe\",\"LIVING_COOKBOOK_PROGRAM\":\"\"}";
		Configuration.fromJson(fromRecipeFox);
		assertEquals(2, Configuration.getIntProperty("TITLE_CASE_PREFERENCE"));
		assertEquals(2, Configuration.getIntProperty("SUBTITLE_CASE_PREFERENCE"));
		assertEquals(true, Configuration.getBooleanProperty("DETECT_AND_MARK_SERVINGS_FROM_YIELD"));
		assertEquals(false, Configuration.getBooleanProperty("COPY_YIELD_TO_DIRECTIONS"));
		assertEquals("C:\\Program Files\\MasterCook 14\\Program\\Mastercook14.exe", Configuration.getStringProperty("MASTERCOOK_PROGRAM"));
	}
}
