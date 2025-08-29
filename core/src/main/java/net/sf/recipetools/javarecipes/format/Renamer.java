/**
 * 
 */
package net.sf.recipetools.javarecipes.format;

import java.util.ArrayList;
import java.util.List;

import net.sf.recipetools.javarecipes.model.Category;
import net.sf.recipetools.javarecipes.model.Ingredient;
import net.sf.recipetools.javarecipes.model.NamedEntity;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeIngredient;
import net.sf.recipetools.javarecipes.model.RenamingItem;
import net.sf.recipetools.javarecipes.model.Unit;


/**
 * @author ft1009
 *
 */
public class Renamer {
	private List<RenamingItem>[] renamingItems = new ArrayList[3];
	
	public void initialize() {
		// TODO: Get all from DB.
	}
	
	public void rename(Recipe recipe) {
		for (Category c : recipe.getCategories()) {
			rename(c);
		}
		for (RecipeIngredient ingr : recipe.getIngredients()) {
			rename(ingr.getUnit());
			rename(ingr.getIngredient());
		}
	}
	public void rename(Category category) {
		rename(category, RenamingItem.TYPE_CATEGORY);
	}
	public void rename(Ingredient ingr) {
		rename(ingr, RenamingItem.TYPE_INGREDIENT);
	}
	public void rename(Unit unit) {
		rename(unit, RenamingItem.TYPE_UNIT);
	}
	
	public void rename(NamedEntity x, int objectClass) {
		if (x==null || x.getName()==null || x.getName().length()==0) {
			return;
		}
		RenamingItem item = findMatch(objectClass, x.getName());
		if (item != null) {
			x.setName(item.getNewName());
		}
	}
	
	/**
	 * Find a renaming item matching the given class and name
	 * @param objectClass search for renamingItems of the given class
	 * @param name and matching the given name
	 * @return the item found or null if none were found.
	 */
	RenamingItem findMatch(int objectClass, String name) {
		for (RenamingItem item : renamingItems[objectClass]) {
			if (item.getName().equals(name)) {
				return item;
			}
		}
		return null;
	}
	
}
