package com.aircraftcarrier.framework.tookit.tree;


import com.aircraftcarrier.framework.tookit.BeanUtil;
import com.aircraftcarrier.framework.tookit.MapUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * TreeNodeUtil
 *
 * @author lzp
 * @version 1.0
 * @date 2020/8/24
 */
public class TreeNodeUtil {

    /**
     * TreeNodeUtil
     */
    private TreeNodeUtil() {
    }

    /**
     * list ==> tree
     * 保留全部数据
     *
     * @param list list
     * @param <T>  树
     * @return List<T> 树
     */
    public static <T extends AbstractTreeNode<T>> List<T> listToTreeFast(List<T> list) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        // 防止改变参数原来结构
        list = BeanUtil.deepCopyList(list);

        Map<Serializable, T> root = MapUtil.newHashMap(128);
        Map<Serializable, T> nodeMap = list.stream().collect(Collectors.toMap(AbstractTreeNode::getAbstractId, Function.identity()));

        list.forEach(node -> {
            AbstractTreeNode<T> parent = nodeMap.get(node.getAbstractParentId());
            if (parent != null) {
                parent.getChildren().add(node);
            } else {
                AbstractTreeNode<T> rootNode = root.get(node.getAbstractId());
                if (rootNode != null) {
                    return;
                }
                root.put(node.getAbstractId(), nodeMap.get(node.getAbstractId()));
            }
        });

        return new ArrayList<>(root.values());
    }


    /**
     * list ==> tree
     * 自动过滤掉 无关联数据
     *
     * @param list list
     * @param <T>  树
     * @return List<T> 树
     */
    public static <T extends AbstractTreeNode<T>> List<T> listToTreeFast(List<T> list, final Serializable rootParentId) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        // 防止改变参数原来结构
        list = BeanUtil.deepCopyList(list);

        Map<Serializable, T> root = MapUtil.newHashMap(128);
        Map<Serializable, T> nodeMap = list.stream().collect(Collectors.toMap(AbstractTreeNode::getAbstractId, Function.identity()));

        list.forEach(node -> {
            AbstractTreeNode<T> parent = nodeMap.get(node.getAbstractParentId());
            if (parent != null) {
                parent.getChildren().add(node);
            } else {
                AbstractTreeNode<T> rootNode = root.get(node.getAbstractId());
                if (rootNode != null) {
                    return;
                }

                if (Objects.equals(node.getAbstractParentId(), rootParentId)) {
                    root.put(node.getAbstractId(), nodeMap.get(node.getAbstractId()));
                }
            }
        });

        return new ArrayList<>(root.values());
    }


    /**
     * list ==> tree
     * 保留全部数据
     *
     * @param list list
     * @param <T>  树
     * @return List<T> 树
     */
    public static <T extends AbstractTreeNode<T>> List<T> listToTree(List<T> list) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Serializable, T> idMap = valid(list);

        Map<Serializable, List<T>> childTreeNodeMap = list.stream().filter(e -> null != e.getAbstractParentId()).collect(Collectors.groupingBy(T::getAbstractParentId));

        return list.stream().filter(e -> isNotTreeNodeExist(idMap, e.getAbstractParentId())).map(e -> getTreeNode(childTreeNodeMap, e, 0)).collect(Collectors.toList());
    }


    /**
     * list ==> tree
     * 自动过滤掉 无关联数据
     *
     * @param list list
     * @param <T>  树
     * @return 树
     */
    public static <T extends AbstractTreeNode<T>> List<T> listToTree(List<T> list, final Serializable rootParentId) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        // 防止改变参数原来结构
        list = BeanUtil.deepCopyList(list);

        Map<Serializable, T> idMap = valid(list);

        List<T> nullRoot = new ArrayList<>(idMap.size());
        Map<Serializable, List<T>> childTreeNodeMap = list.stream().filter(e -> {
            if (null == e.getAbstractParentId()) {
                nullRoot.add(e);
                return false;
            } else {
                return true;
            }
        }).collect(Collectors.groupingBy(T::getAbstractParentId));

        List<T> rootChildList;
        if (rootParentId == null) {
            if (nullRoot.isEmpty()) {
                return new ArrayList<>();
            }
            rootChildList = nullRoot.stream().filter(e -> e.getAbstractId() != null).collect(Collectors.toList());
        } else {
            rootChildList = childTreeNodeMap.get(rootParentId);
            if (rootChildList == null) {
                return new ArrayList<>();
            }
        }

        for (T child : rootChildList) {
            getTreeNode(childTreeNodeMap, child, 0);
        }
        return rootChildList;
    }

    private static <T extends AbstractTreeNode<T>> Map<Serializable, T> valid(List<T> list) {
        return list.stream().collect(Collectors.toMap(AbstractTreeNode::getAbstractId, e -> {
            if (Objects.equals(e.getAbstractId(), e.getAbstractParentId())) {
                throw new IllegalArgumentException(String.format("abstractId:[%s] abstractParentId must not be equal", e.getAbstractId()));
            }
            return e;
        }, (k1, k2) -> {
            throw new IllegalArgumentException("abstractId must not be duplicate");
        }));
    }

    /**
     * tree ==> list
     *
     * @param treeNodeList treeNodeList
     * @param <T>          树
     * @return List<T> 树
     */
    public static <T extends AbstractTreeNode<T>> List<T> treeToList(List<T> treeNodeList) {
        List<T> list = new ArrayList<>();
        if (treeNodeList == null || treeNodeList.isEmpty()) {
            return list;
        }

        childrenToList(treeNodeList, list);
        return list;
    }


    /**
     * childrenList ==> list
     *
     * @param childrenList childrenList
     * @param list         list
     * @param <T>          树
     */
    private static <T extends AbstractTreeNode<T>> void childrenToList(List<T> childrenList, List<T> list) {
        if (childrenList == null || childrenList.isEmpty()) {
            return;
        }
        for (T childrenTreeNode : childrenList) {
            list.add(childrenTreeNode);
            childrenToList(childrenTreeNode.getChildren(), list);
        }
    }


    /**
     * 根据ID判断该点是否存在
     *
     * @param idMap idMap
     * @param id    id
     * @return Boolean
     */
    private static <T extends AbstractTreeNode<T>> Boolean isNotTreeNodeExist(Map<Serializable, T> idMap, Serializable id) {
        if (id == null) {
            return false;
        }
        return null == idMap.get(id);
    }


    /**
     * 根据实体获取TreeNode
     *
     * @param childTreeNodeMap childTreeNodeMap
     * @param t                t
     * @param level            level
     * @return T
     */
    private static <T extends AbstractTreeNode<T>> T getTreeNode(Map<Serializable, List<T>> childTreeNodeMap, T t, Integer level) {
        t.setLevel(level++);

        List<T> childTreeNodeList = childTreeNodeMap.get(t.getAbstractId());
        if (childTreeNodeList == null) {
            t.setLeaf(true);
        } else {
            t.setChildren(childTreeNodeList);
            for (T child : childTreeNodeList) {
                child.setParent(t);
                getTreeNode(childTreeNodeMap, child, level);
            }
        }
        return t;
    }

}