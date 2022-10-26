package com.aircraftcarrier.framework.tookit;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Node implements Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;
    private Long id;
    private String name;
    private List<Node> subList;
}
