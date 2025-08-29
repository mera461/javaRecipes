/*
 * Created on 25-01-2005
 *
 */
package net.sf.recipetools.javarecipes.model;


/**
 * @author ft
 *
 */
public abstract class DbEntity {

	DbEntity() {
		setId(-1L);
		setHbmVersion(-1);
	}

	DbEntity(long id) {
		setId(id);
		setHbmVersion(-1);
	}
	
	public abstract Long getId();
	public abstract void setId(Long id);
	
	public abstract int getHbmVersion();
	public abstract void setHbmVersion(int hbmVersion);
	
	public boolean isNew() {
		return getHbmVersion() == -1;
	}
	
}
