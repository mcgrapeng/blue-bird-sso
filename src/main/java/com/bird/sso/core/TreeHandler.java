package com.bird.sso.core;

import com.google.common.collect.Lists;
import com.bird.sso.domain.SelfReference;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 张鹏
 * 展示树处理器
 *
 * @param <T>
 */
public class TreeHandler<T extends SelfReference<T>> {

    private List<T> data;

    private List<T> childNodes = Lists.newArrayList();

    private List<T> parentNodes = Lists.newArrayList();

    private TreeHandler(List<T> data) {
        this.data = data;
    }

    private TreeHandler() {
    }

    public static <T> TreeHandler of(List<T> data) {
        return new TreeHandler(data);
    }

    public static TreeHandler of() {
        return new TreeHandler();
    }

    /**
     * 建立树形结构
     *
     * @return
     */
    public List<T> builTree() {
        List<T> tree = Lists.newArrayList();
        for (T node : getRootNode()) {
            node = buildChilTree(node);
            tree.add(node);
        }
        return tree;
    }

    /**
     * 建立子树形结构
     *
     * @param pNode
     * @return
     */
    private T buildChilTree(T pNode) {
        List<T> chils = Lists.newArrayList();
        for (T node : data) {
            if (node.getPid().equals(pNode.getSid())) {
                chils.add(buildChilTree(node));
            }
        }
        pNode.setChildrens(chils);
        return pNode;
    }

    /**
     * 获取根节点
     *
     * @return
     */
    private List<T> getRootNode() {
        List<T> rootMenuLists = Lists.newArrayList();
        for (T node : data) {
            if (node.getPid() == 0) {
                rootMenuLists.add(node);
            }
        }
        return rootMenuLists;
    }


    /**
     * 获取某个节点上边的所有父节点(含自己)
     *
     * @param data
     * @param pid
     * @return
     */
    public List<T> getParentNode(List<T> data, Long pid) {
        for (T t : data) {
            // 判断是否存在父节点
            if (t.getSid() == pid) {
                // 递归遍历上一级
                getParentNode(data, t.getPid());
                parentNodes.add(t);
            }
        }
        return parentNodes;
    }





    /**
     * 获取某个节点下面的所有子节点(不含自己)
     *
     * @param data
     * @param sid
     * @return
     */
    public List<T> getChildNode(List<T> data, long sid) {
        for (T r : data) {
            //遍历出父id等于参数的id，add进子节点集合
            if (r.getPid() == sid) {
                //递归遍历下一级
                getChildNode(data, r.getSid());
                childNodes.add(r);
            }
        }
        return childNodes;
    }


    /**
     * 获取某个节点下面的所有子节点(含自己)
     *
     * @param data
     * @param sid
     * @return
     */
    public List<T> getChildNodeContainSelf(List<T> data, long sid) {
        List<T> l = data.stream().filter(t -> t.getSid() == sid)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(l)) {
            childNodes.addAll(l);
        }
        return getChildNode(data, sid);
    }


    public void destroy() {
        if (CollectionUtils.isNotEmpty(this.data)) {
            //this.all.clear();
            this.data = null;
        }
        if (CollectionUtils.isNotEmpty(this.childNodes)) {
            //this.all.clear();
            this.childNodes = null;
        }

        if (CollectionUtils.isNotEmpty(this.parentNodes)) {
            //this.all.clear();
            this.parentNodes = null;
        }
    }
}
