/*
 * Created on 20-10-2004
 *
 */
package net.sf.recipetools.javarecipes.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ft
 *
 */
public class RecipeIngredientSection {

	private int id;
	private String title;
	private String description;
	private List<RecipeIngredient> recipeIngredients;
	
	/**
	 * @param id
	 * @param title
	 * @param description
	 * @param ingredients
	 */
	public RecipeIngredientSection(int id, String title, String description,
			List<RecipeIngredient> ingredients) {
		super();
		this.id = id;
		this.title = title;
		this.description = description;
		recipeIngredients = ingredients;
	}

	public RecipeIngredientSection(String title, String description) {
		super();
		this.title = title;
		this.description = description;
		recipeIngredients = new ArrayList<RecipeIngredient>();
	}
	
	
	/**
	 * 
	 */
	public RecipeIngredientSection() {
		super();
		recipeIngredients = new ArrayList<RecipeIngredient>();
		this.title = "";
		this.description = "";
		
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return Returns the id.
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id The id to set.
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @return Returns the ingredients.
	 */
	public List<RecipeIngredient> getIngredients() {
		return recipeIngredients;
	}
	/**
	 * @param ingredients The ingredients to set.
	 */
	public void setIngredients(List<RecipeIngredient> ingredients) {
		recipeIngredients = ingredients;
	}
	/**
	 * @return Returns the title.
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param title The title to set.
	 */
	public void setTitle(String title) {
		this.title = title;
	}
}
