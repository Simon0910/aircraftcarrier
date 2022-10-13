package com.aircraftcarrier.framework.concurrent.advance.threadpool;

import com.google.common.collect.Sets;

import java.util.Set;

public class TreeNode {

    private final int value;

    private final Set<TreeNode> children;

    TreeNode(int value, TreeNode... children) {
        this.value = value;
        this.children = Sets.newHashSet(children);
    }

    public int getValue() {
        return value;
    }

    public Set<TreeNode> getChildren() {
        return children;
    }
}