package com.aircraftcarrier.marketing.store.client.es;

import lombok.Data;

/**
 * @author liuzhipeng
 */
@Data
public class EsDocumentVo {
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
