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
//@Entity
public class Folder extends NamedEntity {

	private static List<Folder> all = new ArrayList<Folder>();
	private static long idCounter = 1;
	
	/**
	 * @param id
	 * @return Get the chapter with the specified id, or null if not found
	 */
	public static Folder get(long id) {
		for (Folder i : all) {
			if (i.id == id) return i;
		}
		return null;
	}
	
	public static void clear() {
		all.clear();
	}
	
	/** Hibernate ID: primary key */
	//@Id
	//@GeneratedValue
	Long id;

	/** the hibernate HBM version */
	//@Version
	int hbmVersion;
	
	//@Basic
	//@org.hibernate.annotations.Index(name = "IDX_CATEGORY_NAME")
	private String name;
	
	private String description;
	
	Folder parent;
	
	List<Folder> children = new ArrayList<Folder>();
	
	
	/**
	 * 
	 */
	public Folder() {
		super();
		id = idCounter++;
		all.add(this);
	}

	/**
	 * @param name
	 */
	public Folder(String name) {
		super(name);
		id = idCounter++;
		all.add(this);
	}

	/**
	 * @param id
	 * @param name
	 * @param parent
	 */
	public Folder(int id, String name) {
		super(id, name);
		all.add(this);
	}

	/**
	 * @return Returns the id.
	 */
    @Override
	public Long getId() {
		return id;
	}
	/**
	 * @param id The id to set.
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
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the chapters
	 */
	public List<Folder> getChildren() {
		return children;
	}

	/**
	 * @param chapters the chapters to set
	 */
	public void setChildren(List<Folder> children) {
		this.children = children;
	}
	
	public void addChild(Folder child) {
		children.add(child);
		child.parent = this;
	}

	/**
	 * @return the all
	 */
	public static List<Folder> getAll() {
		return all;
	}

	/**
	 * @param all the all to set
	 */
	public static void setAll(List<Folder> all) {
		Folder.all = all;
	}

	/**
	 * @return the parent
	 */
	public Folder getParent() {
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(Folder parent) {
		this.parent = parent;
		if (parent != null
			&& ! parent.children.contains(this)) {
			parent.addChild(this);
		}
	}
	
	/**
	 * @return Returns the parent.
	 */
	public Folder getRoot() {
		if (parent != null) {
			return parent.getRoot();
		} 
		return this;
	}
	
	public boolean isRoot() {
		return parent == null;
	}
	
}
