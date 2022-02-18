package com.aircraftcarrier.generator.custom;

import com.aircraftcarrier.generator.config.GlobalConfig;
import com.aircraftcarrier.generator.config.converts.MySqlTypeConvert;
import com.aircraftcarrier.generator.config.converts.TypeConverts;
import com.aircraftcarrier.generator.config.rules.IColumnType;

import static com.aircraftcarrier.generator.config.converts.TypeConverts.contains;

/**
 * @author lzp
 */
public class MybatisMySqlTypeConvert extends MySqlTypeConvert {

    @Override
    public IColumnType processTypeConvert(GlobalConfig globalConfig, String fieldType) {
        System.out.println("转换类型：" + fieldType);
        //将数据库中datetime转换成date
        return TypeConverts.use(fieldType).test(contains("datetime").then(t -> toDateType(globalConfig, t)))
                .or(super.processTypeConvert(globalConfig, fieldType));
    }
}
