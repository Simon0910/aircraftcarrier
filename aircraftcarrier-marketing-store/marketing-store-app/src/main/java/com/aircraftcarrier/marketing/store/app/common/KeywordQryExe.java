package com.aircraftcarrier.marketing.store.app.common;

import cn.hutool.core.util.ArrayUtil;
import com.aircraftcarrier.framework.exception.SysException;
import com.aircraftcarrier.framework.tookit.MapUtil;
import com.aircraftcarrier.framework.tookit.ObjUtil;
import com.aircraftcarrier.framework.tookit.StringUtil;
import com.aircraftcarrier.marketing.store.client.common.KeywordQry;
import com.aircraftcarrier.marketing.store.infrastructure.repository.CommonMapper;
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

    @Resource
    private CommonMapper commonMapper;

    /**
     * 字段映射
     */
    private static final Map<String, String> LIKE_FIELD_MAPPING = MapUtil.newHashMap(16);
    private static final Map<String, String> FIELD_MAPPING = MapUtil.newHashMap(16);
    private static final Map<String, String> TABLE_MAPPING = MapUtil.newHashMap(16);

    /**
     * 初始化
     */
    static {
        LIKE_FIELD_MAPPING.put("goodsNo", "goods_no");

        FIELD_MAPPING.put("goodsNo", "goods_no");

        TABLE_MAPPING.put("t_product", "product");
    }

    public List<Map<String, Object>> execute(KeywordQry keywordQry) {
        if (StringUtil.isBlank(keywordQry.getTableName())) {
            return Collections.emptyList();
        }

        String[] fields = keywordQry.getFields();
        if (ArrayUtil.isEmpty(fields)) {
            return Collections.emptyList();
        }

        // likeField like keyword%
        String keyword = keywordQry.getKeyword();
        if (StringUtil.isNotBlank(keyword)) {
            keywordQry.setKeyword(StringUtil.trim(keyword));
            keywordQry.setLikeField(LIKE_FIELD_MAPPING.get(keywordQry.getLikeField()));
            if (keywordQry.getLikeField() == null) {
                throw new SysException("like field error");
            }
        }

        // fields
        for (int i = 0, len = fields.length; i < len; i++) {
            fields[i] = FIELD_MAPPING.get(fields[i]);
        }
        keywordQry.setFields(ArrayUtil.removeBlank(ArrayUtil.distinct(fields)));
        if (ArrayUtil.isEmpty(keywordQry.getFields())) {
            throw new SysException("field error");
        }

        // table
        keywordQry.setTableName(TABLE_MAPPING.get(keywordQry.getTableName()));
        if (keywordQry.getTableName() == null) {
            throw new SysException("table name error");
        }

        return commonMapper.keywordsQuery(ObjUtil.obj2Map(keywordQry));
    }

}
