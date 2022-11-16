package com.aircraftcarrier.marketing.store.infrastructure.es.data;

import cn.easyes.annotation.IndexName;
import lombok.Data;

/**
 * ES数据模型
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 *
 * @author liuzhipeng
 */
@Data
@IndexName("document")
public class EsDocument {
    /**
     * es中的唯一id
     */
    private String id;

    /**
     * 文档标题
     */
    private String title;
    /**
     * 文档内容
     */
    private String content;
}