package com.lzp.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
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

    public void setNodeValue(String newPath, Object obj) {
        this.newPath = newPath;
    }

}
