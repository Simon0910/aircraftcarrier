package com.aircraftcarrier.framework.tookit;

import org.burningwave.core.assembler.StaticComponentContainer;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class BeanUtil_Test {

    Node node = new Node();

    @Before
    public void before() {
        StaticComponentContainer.Modules.exportAllToAll();

        node.setId(1L);
        node.setName("parent");

        Node node2 = new Node();
        node2.setId(2L);
        node2.setName("sub");

        List<Node> subList = new ArrayList<>();
        subList.add(node2);

        node.setSubList(subList);
    }

    @Test
    public void copyTest() {
        Node convert = BeanUtil.convert(node, Node.class);
        convert.setName("other");
        System.out.println(node);
        System.out.println(convert);
    }

    @Test
    public void copyTest02() {
        Node convert = BeanUtil.deepCopy(node);
        convert.setName("other");
        System.out.println(node);
        System.out.println(convert);
    }
}
