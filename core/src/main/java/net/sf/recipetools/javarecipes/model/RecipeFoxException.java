/**
 * 
 */
package net.sf.recipetools.javarecipes.model;

/**
 * A simple wrapper class to avoid throwing a RuntimeException directly
 * 
 * @author Frank
 *
 */
public class RecipeFoxException extends RuntimeException {

	private static final long serialVersionUID = -3989601797281284109L;

	public RecipeFoxException() {
	}

	/**
	 * @param arg0
	 */
	public RecipeFoxException(String arg0) {
		super(arg0);
	}

	/**
	 * @param cause
	 */
	public RecipeFoxException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public RecipeFoxException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public RecipeFoxException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
