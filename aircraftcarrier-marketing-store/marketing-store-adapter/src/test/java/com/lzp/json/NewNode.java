package com.lzp.json;

import com.fasterxml.jackson.databind.node.ValueNode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewNode {

    private String newPath;

    private ValueNode valueNode;

    public NewNode(String newPath, ValueNode valueNode) {
        this.newPath = newPath;
        this.valueNode = valueNode;
    }
}
