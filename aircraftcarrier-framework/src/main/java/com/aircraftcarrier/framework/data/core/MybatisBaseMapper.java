package com.aircraftcarrier.framework.data.core;

import com.aircraftcarrier.framework.data.BaseDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.mapping.ResultSetType;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author lzp
 */
public interface MybatisBaseMapper<T extends BaseDO> extends BaseMapper<T> {

    /**
     * 根据QueryWrapper条件进行流式查询
     *
     * @param wrapper 条件
     * @return Cursor<T>
     */
    @Transactional(rollbackFor = Exception.class)
    @Options(resultSetType = ResultSetType.FORWARD_ONLY, fetchSize = 5000)
    Cursor<T> selectCursor(@Param(Constants.WRAPPER) QueryWrapper<T> wrapper);


}
