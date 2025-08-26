/*
 * Created on 20-10-2004
 *
 */
package net.sf.recipetools.javarecipes.model;


/**
 * @author ft
 *
 */
//@Entity
public class Ingredient extends NamedEntity {

	/** Hibernate ID: primary key */
	//@Id
	//@GeneratedValue
	Long id;

	/** the hibernate HBM version */
	//@Version
	int hbmVersion;

	//@Basic
	//@org.hibernate.annotations.Index(name = "IDX_INGR_NAME")
	private String name;
	
	/**
	 * 
	 */
	public Ingredient() {
		super();
	}
	
	/**
	 * @param id
	 * @param name
	 */
	public Ingredient(int id, String name) {
		super(id, name);
	}

	/**
	 * @param name
	 */
	public Ingredient(String name) {
		super(name);
	}

	/**
	 * @return the id
	 */
    @Override
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
    @Override
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the hbmVersion
	 */
    @Override
	public int getHbmVersion() {
		return hbmVersion;
	}

	/**
	 * @param hbmVersion the hbmVersion to set
	 */
    @Override
	public void setHbmVersion(int hbmVersion) {
		this.hbmVersion = hbmVersion;
	}

	/**
	 * @return the name
	 */
    @Override
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
    @Override
	public void setName(String name) {
		this.name = name;
	}
}
