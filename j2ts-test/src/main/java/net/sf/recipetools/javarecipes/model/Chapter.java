/*
 * Created on 20-10-2004
 *
 */
package net.sf.recipetools.javarecipes.model;

/**
 * @author ft
 *
 */
// @Entity
public class Chapter extends Folder {

    /**
	 * 
	 */
    public Chapter() {
        super();
    }

    /**
     * @param name
     */
    public Chapter(String name) {
        super(name);
    }

    /**
     * @param name
     * @param parentId
     */
    public Chapter(String name, Folder parent) {
        super(name);
        setParent(parent);
    }

    /**
     * @param id
     * @param name
     * @param parent
     */
    public Chapter(int id, String name, Chapter parent) {
        super(id, name);
        setParent(parent);
    }
}
