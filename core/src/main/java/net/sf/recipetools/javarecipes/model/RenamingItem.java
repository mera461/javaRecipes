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
public class RenamingItem extends NamedEntity {
	public static final int TYPE_CATEGORY = 0;
	public static final int TYPE_INGREDIENT = 1;
	public static final int TYPE_UNIT = 2;
	
	/** Hibernate ID: primary key */
	//@Id
	//@GeneratedValue
	Long id;

	/** the hibernate HBM version */
	//@Version
	int hbmVersion;

	//@Basic
	private int objectClass;
	
	//@Basic
	//@org.hibernate.annotations.Index(name = "IDX_ORIGINAL_NAME")
	private String name;

	//@Basic
	private String newName;
	
	
	/**
	 * 
	 */
	public RenamingItem() {
		super();
	}
	
	/**
	 * @param id
	 * @param name
	 */
	public RenamingItem(int id, String name) {
		super(id, name);
	}

	/**
	 * @param name
	 */
	public RenamingItem(String name) {
		super(name);
	}

	/**
	 * @param oldname
	 * @param newname
	 */
	public RenamingItem(String oldname, String newname) {
		super(oldname);
		this.newName = newname;
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

	/**
	 * @return the objectClass
	 */
	public int getObjectClass() {
		return objectClass;
	}

	/**
	 * @param objectClass the objectClass to set
	 */
	public void setObjectClass(int objectClass) {
		this.objectClass = objectClass;
	}

	/**
	 * @return the newName
	 */
	public String getNewName() {
		return newName;
	}

	/**
	 * @param newName the newName to set
	 */
	public void setNewName(String newName) {
		this.newName = newName;
	}
}
