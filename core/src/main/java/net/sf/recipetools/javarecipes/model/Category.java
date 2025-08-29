/*
 * Created on 20-10-2004
 */
package net.sf.recipetools.javarecipes.model;

import java.util.Set;

/**
 * @author ft
 *
 */
// @Entity
public class Category extends NamedEntity {

    /** Hibernate ID: primary key */
    // @Id
    // @GeneratedValue
    Long id;

    /** the hibernate HBM version */
    // @Version
    int hbmVersion;

    // @Basic
    // @org.hibernate.annotations.Index(name = "IDX_CATEGORY_NAME")
    private String name;

    // @ManyToOne( targetEntity =
    // net.sf.recipetools.javarecipes.model.Category.class )
    // @JoinColumn(name = "PARENT_ID", nullable = true)
    private Category parent;

    // @OneToMany(mappedBy = "parent")
    private Set<Category> children;

    /**
	 * 
	 */
    public Category() {
        super();
    }

    /**
     * @param name
     */
    public Category(String name) {
        super(name);
    }

    /**
     * @param name
     * @param parentId
     */
    public Category(String name, Category parent) {
        super(name);
        this.parent = parent;
    }

    /**
     * @param id
     * @param name
     * @param parent
     */
    public Category(int id, String name, Category parent) {
        super(id, name);
        this.parent = parent;
    }

    /**
     * @return Returns the parent.
     */
    public Category getParent() {
        return parent;
    }

    /**
     * @param parent
     *            The parentId to set.
     */
    public void setParent(Category parent) {
        this.parent = parent;
    }

    /**
     * @return the children
     */
    public Set<Category> getChildren() {
        return children;
    }

    /**
     * @param children
     *            the children to set
     */
    public void setChildren(Set<Category> children) {
        this.children = children;
    }

    /**
     * @return Returns the id.
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * @param id
     *            The id to set.
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
     * @param hbmVersion
     *            the hbmVersion to set
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
     * @param name
     *            the name to set
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }
}
