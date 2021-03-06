package com.aircraftcarrier.marketing.store.infrastructure.repository.mapper;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author lzp
 */
@Mapper
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
