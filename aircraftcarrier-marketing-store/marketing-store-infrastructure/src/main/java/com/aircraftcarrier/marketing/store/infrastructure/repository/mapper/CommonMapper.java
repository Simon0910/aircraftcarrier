package com.aircraftcarrier.marketing.store.infrastructure.repository.mapper;

import org.apache.ibatis.annotations.MapKey;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author lzp
 */
@Repository
public interface CommonMapper {


    /**
     * keywordQry
     *
     * @param keywordQry keywordQry
     * @return List
     */
    @MapKey("id")
    List<Map<String, Object>> keywordsQuery(Map<String, Object> keywordQry);

}
