/*
 * Created on 25-01-2005
 *
 */
package net.sf.recipetools.javarecipes.model;


/**
 * @author ft
 *
 */
public abstract class NamedEntity extends DbEntity {

	/**
	 * 
	 */
	public NamedEntity() {
		super();
	}
	public NamedEntity(String name) {
		super();
		setName(name);
	}
	public NamedEntity(long id, String name) {
		super();
		setId(id);
		setName(name);
	}

	/**
	 * @return Returns the name.
	 */
	public abstract String getName();
	/**
	 * @param name The name to set.
	 */
	public abstract void setName(String name);
	
}
