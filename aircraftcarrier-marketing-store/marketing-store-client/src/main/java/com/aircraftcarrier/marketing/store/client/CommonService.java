package com.aircraftcarrier.marketing.store.client;

import com.aircraftcarrier.marketing.store.client.common.KeywordQry;

import java.util.List;
import java.util.Map;

/**
 * @author lzp
 */
public interface CommonService {

    /**
     * keywordsQuery
     *
     * @param keywordQry keywordQry
     * @return List
     */
    List<Map<String, Object>> keywordsQuery(KeywordQry keywordQry);
}
