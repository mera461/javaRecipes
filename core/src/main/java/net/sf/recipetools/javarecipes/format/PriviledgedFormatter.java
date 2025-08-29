/**
 * 
 */
package net.sf.recipetools.javarecipes.format;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;

import net.sf.recipetools.javarecipes.model.Recipe;

/**
 * @author Frank
 *
 */
public class PriviledgedFormatter implements RecipeFormatter {

	private RecipeFormatter formatter;
	public PriviledgedFormatter(RecipeFormatter formatter) {
		this.formatter = formatter;
	}
	
	/* (non-Javadoc)
	 * @see net.sf.recipetools.javarecipes.format.RecipeFormatter#read(java.io.File)
	 */
	@Override
	public List<Recipe> read(final File f) {
		List<Recipe> result = AccessController.doPrivileged(new PrivilegedAction<List<Recipe>>() {
		    @Override
			public List<Recipe> run() {
				return formatter.read(f);
			}
		});
		return result;
	}

	/* (non-Javadoc)
	 * @see net.sf.recipetools.javarecipes.format.RecipeFormatter#write(net.sf.recipetools.javarecipes.model.Recipe[])
	 */
	@Override
	public void write(final List<Recipe> recipe) {
		AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
		    @Override
			public Boolean run() {
				formatter.write(recipe);
				return true;
			}
		});
	}

	@Override
	public void startFile(String name) {
		startFile(new File(name));
	}
		/* (non-Javadoc)
	 * @see net.sf.recipetools.javarecipes.format.RecipeFormatter#startFile(java.io.File)
	 */
	@Override
	public void startFile(final File f) {
		AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
		    @Override
			public Boolean run() {
				formatter.startFile(f);
				return true;
			}
		});
	}

	/* (non-Javadoc)
	 * @see net.sf.recipetools.javarecipes.format.RecipeFormatter#endFile()
	 */
	@Override
	public void endFile() {
		AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
		    @Override
			public Boolean run() {
				formatter.endFile();
				return true;
			}
		});
	}

	/* (non-Javadoc)
	 * @see net.sf.recipetools.javarecipes.format.RecipeFormatter#setConfig(java.lang.String, java.lang.String)
	 */
	@Override
	public void setConfig(final String property, final String value) {
		AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
		    @Override
			public Boolean run() {
				formatter.setConfig(property, value);
				return true;
			}
		});
	}

	/* (non-Javadoc)
	 * @see net.sf.recipetools.javarecipes.format.RecipeFormatter#getConfig(java.lang.String)
	 */
	@Override
	public String getConfig(final String property) {
		String result = AccessController.doPrivileged(new PrivilegedAction<String>() {
		    @Override
			public String run() {
				return formatter.getConfig(property);
			}
		});
		return result;
	}

}
