/*
 * Created on 20-10-2004
 */
package net.sf.recipetools.javarecipes.model;



/**
 * @author ft
 *
 */
//@Entity
public class Cookbook extends Folder {

	private Image image;
	
	/**
	 * 
	 */
	public Cookbook() {
		super();
	}

	/**
	 * @param name
	 */
	public Cookbook(String name) {
		super(name);
	}

	/**
	 * @param id
	 * @param name
	 * @param parent
	 */
	public Cookbook(int id, String name) {
		super(id, name);
	}

	/**
	 * @return the image
	 */
	public Image getImage() {
		return image;
	}

	/**
	 * @param image the image to set
	 */
	public void setImage(Image image) {
		this.image = image;
	}
}
