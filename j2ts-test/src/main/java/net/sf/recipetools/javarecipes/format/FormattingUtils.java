/**
 * 
 */
package net.sf.recipetools.javarecipes.format;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.util.Locale;

import net.sf.recipetools.javarecipes.model.Configuration;

/**
 * @author frank
 *
 */
public class FormattingUtils {

	static DecimalFormat format = new DecimalFormat("#######.####", new DecimalFormatSymbols(Locale.US));

	public static String formatNumber(double x) {
		String result = "";
		if (Configuration.getBooleanProperty("USE_FRACTIONS_IN_AMOUNTS")) {
			int[] denominators = new int[] { 2, 3, 4, 5, 8, 10, 16 };
			int[] numerators = new int[denominators.length];
			double[] fractionError = new double[denominators.length];
			for (int i = 0; i < fractionError.length; i++)
				fractionError[i] = 1000d;
			boolean found = false;
			int integerPart = (int) Math.floor(x);
			double fracPart = x - integerPart;
			if (integerPart > 0) {
				result = "" + integerPart;
			}
	
			if (fracPart > 0.01) {
				for (int i = 0; i < denominators.length; i++) {
					for (int j = 1; j < denominators[i]; j++) {
						double err = Math.abs(fracPart - 1.0d * j / denominators[i]);
						numerators[i] = j;
						fractionError[i] = err;
						if (err < 0.01) {
							found = true;
							break;
						}
					}
					if (found)
						break;
				}
	
				// find the best match
				double minError = 1000d;
				int minIndex = 0;
				for (int i = 0; i < denominators.length; i++) {
					if (fractionError[i] < minError) {
						minError = fractionError[i];
						minIndex = i;
					}
				}
				result = FormattingUtils.join(result, " ", numerators[minIndex] + "/" + denominators[minIndex]);
			}
		} else {
			StringBuffer str = new StringBuffer();
			FormattingUtils.format.format(x, str, new FieldPosition(0));
			result = str.toString();
		}
		return result;
	}

	public static String join(String s1, String joinString, String s2) {
		if (s1 == null || s1.length() == 0) {
			return s2;
		} else if (s2 == null || s2.length() == 0) {
			return s1;
		} else {
			return s1 + joinString + s2;
		}
	}

	/**
	 * Remove whitespace at the end of the string.
	 * 
	 * @param s
	 *            The string to be trimmed
	 * @return
	 */
	public static String rtrim(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		int i = s.length() - 1;
		while (i >= 0 && Character.isWhitespace(s.charAt(i))) {
			i--;
		}
		return i < 0 ? "" : s.substring(0, i + 1);
	}

}
