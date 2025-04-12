package com.aircraftcarrier.marketing.store.app.common;

import cn.hutool.core.util.ArrayUtil;
import com.aircraftcarrier.framework.exception.SysException;
import com.aircraftcarrier.framework.tookit.BeanMapUtil;
import com.aircraftcarrier.framework.tookit.MapUtil;
import com.aircraftcarrier.framework.tookit.StringUtil;
import com.aircraftcarrier.marketing.store.client.common.KeywordQry;
import com.aircraftcarrier.marketing.store.infrastructure.repository.mapper.CommonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author lzp
 */
@Slf4j
@Component
public class KeywordQryExe {

    /**
     * 字段映射
     */

    private static final Map<String, String> TABLE_MAPPING = MapUtil.newHashMap(16);
    private static final Map<String, Map<String, String>> FIELD_MAPPING = MapUtil.newHashMap(16);

    static {
        TABLE_MAPPING.put("product", "t_product");

        Map<String, String> tableProductField = MapUtil.newHashMap(16);
        tableProductField.put("goodsNo", "goods_no");
        FIELD_MAPPING.put("product", tableProductField);
    }

    @Resource
    private CommonMapper commonMapper;

    public List<Map<String, Object>> execute(KeywordQry keywordQry) {
        // query table
        if (StringUtil.isBlank(keywordQry.getTableName())) {
            return Collections.emptyList();
        }
        keywordQry.setTableName(TABLE_MAPPING.get(keywordQry.getTableName()));
        if (keywordQry.getTableName() == null) {
            throw new SysException("table name error");
        }

        // query fields
        String[] fields = keywordQry.getFields();
        if (ArrayUtil.isEmpty(fields)) {
            return Collections.emptyList();
        }
        Map<String, String> fieldMap = FIELD_MAPPING.get(keywordQry.getTableName());
        for (int i = 0, len = fields.length; i < len; i++) {
            fields[i] = fieldMap.get(fields[i]);
        }
        keywordQry.setFields(ArrayUtil.removeBlank(ArrayUtil.distinct(fields)));
        if (ArrayUtil.isEmpty(keywordQry.getFields())) {
            throw new SysException("field error");
        }

        // likeField like keyword%
        String keyword = keywordQry.getKeyword();
        if (StringUtil.isNotBlank(keyword)) {

            // keyword
            keywordQry.setKeyword(StringUtil.trim(keyword).concat("%"));

            // likeField
            keywordQry.setLikeField(fieldMap.get(keywordQry.getLikeField()));
            if (keywordQry.getLikeField() == null) {
                throw new SysException("like field error");
            }

        } else {
            // likeField set null
            keywordQry.setLikeField(null);
        }

        return commonMapper.keywordsQuery(BeanMapUtil.obj2Map(keywordQry));
    }

}
