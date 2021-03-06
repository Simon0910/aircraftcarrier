/*
 * Copyright (c) 2011-2021, baomidou (jobob@qq.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.aircraftcarrier.generator.config.converts;

import com.aircraftcarrier.generator.config.GlobalConfig;
import com.aircraftcarrier.generator.config.ITypeConvert;
import com.aircraftcarrier.generator.config.rules.DbColumnType;
import com.aircraftcarrier.generator.config.rules.IColumnType;

import static com.aircraftcarrier.generator.config.converts.TypeConverts.contains;
import static com.aircraftcarrier.generator.config.converts.TypeConverts.containsAny;
import static com.aircraftcarrier.generator.config.rules.DbColumnType.BLOB;
import static com.aircraftcarrier.generator.config.rules.DbColumnType.DOUBLE;
import static com.aircraftcarrier.generator.config.rules.DbColumnType.FLOAT;
import static com.aircraftcarrier.generator.config.rules.DbColumnType.LONG;
import static com.aircraftcarrier.generator.config.rules.DbColumnType.SHORT;
import static com.aircraftcarrier.generator.config.rules.DbColumnType.STRING;

/**
 * MYSQL 数据库字段类型转换
 *
 * @author hubin, hanchunlin
 * @since 2017-01-20
 */
public class FirebirdTypeConvert implements ITypeConvert {
    public static final FirebirdTypeConvert INSTANCE = new FirebirdTypeConvert();

    /**
     * 转换为日期类型
     *
     * @param config 配置信息
     * @param type   类型
     * @return 返回对应的列类型
     */
    public static IColumnType toDateType(GlobalConfig config, String type) {
        switch (config.getDateType()) {
            case ONLY_DATE:
                return DbColumnType.DATE;
            case SQL_PACK:
                switch (type) {
                    case "date":
                    case "year":
                        return DbColumnType.DATE_SQL;
                    case "time":
                        return DbColumnType.TIME;
                    default:
                        return DbColumnType.TIMESTAMP;
                }
            case TIME_PACK:
                switch (type) {
                    case "date":
                        return DbColumnType.LOCAL_DATE;
                    case "time":
                        return DbColumnType.LOCAL_TIME;
                    case "year":
                        return DbColumnType.YEAR;
                    default:
                        return DbColumnType.LOCAL_DATE_TIME;
                }
        }
        return STRING;
    }

    /**
     * @inheritDoc
     */
    @Override
    public IColumnType processTypeConvert(GlobalConfig config, String fieldType) {
        return TypeConverts.use(fieldType)
                .test(containsAny("cstring", "text").then(STRING))
                .test(contains("short").then(SHORT))
                .test(contains("long").then(LONG))
                .test(contains("float").then(FLOAT))
                .test(contains("double").then(DOUBLE))
                .test(contains("blob").then(BLOB))
                .test(contains("int64").then(LONG))
                .test(containsAny("date", "time", "year").then(t -> toDateType(config, t)))
                .or(STRING);
    }

}
