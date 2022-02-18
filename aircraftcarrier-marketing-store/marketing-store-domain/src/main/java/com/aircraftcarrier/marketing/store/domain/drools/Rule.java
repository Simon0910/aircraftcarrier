package com.aircraftcarrier.marketing.store.domain.drools;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * neo
 */
@Getter
@Setter
public class Rule implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String ruleKey;
    private String path = "src/main/resources/rules/temp.drl";
    private String content;
    private String version;
    private String lastModifyTime;
    private String createTime;
}