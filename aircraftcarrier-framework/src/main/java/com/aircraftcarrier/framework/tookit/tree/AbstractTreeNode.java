package com.aircraftcarrier.framework.tookit.tree;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * TreeNode
 *
 * @author lzp
 */
public abstract class AbstractTreeNode<T extends Serializable> implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * 节点ID
     */
    @JsonIgnore
    private Serializable abstractId;

    /**
     * 父点ID
     */
    @JsonIgnore
    private Serializable abstractParentId;

    /**
     * 菜单名称
     */
    private String label;

    /**
     * 层级
     */
    private Integer level;

    /**
     * 是否叶子节点
     */
    private boolean leaf;

    /**
     * 父节点
     */
    @JsonBackReference
    private T parent;

    /**
     * 该点的子树集合
     */
    private List<T> children = new ArrayList<>();


    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public boolean getLeaf() {
        return leaf;
    }

    public void setLeaf(Boolean leaf) {
        this.leaf = leaf;
    }

    public T getParent() {
        return parent;
    }

    public void setParent(T parent) {
        this.parent = parent;
    }

    public List<T> getChildren() {
        return children;
    }

    public void setChildren(List<T> children) {
        this.children = children;
    }

    /**
     * abstractId
     *
     * @return abstractId
     */
    public abstract Serializable getAbstractId();

    /**
     * abstractParentId
     *
     * @return abstractParentId
     */
    public abstract Serializable getAbstractParentId();

}