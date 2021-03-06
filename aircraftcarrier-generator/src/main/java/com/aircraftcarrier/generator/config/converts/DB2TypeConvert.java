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
import com.aircraftcarrier.generator.config.rules.IColumnType;

import static com.aircraftcarrier.generator.config.converts.TypeConverts.contains;
import static com.aircraftcarrier.generator.config.converts.TypeConverts.containsAny;
import static com.aircraftcarrier.generator.config.rules.DbColumnType.BASE_SHORT;
import static com.aircraftcarrier.generator.config.rules.DbColumnType.BIG_DECIMAL;
import static com.aircraftcarrier.generator.config.rules.DbColumnType.BLOB;
import static com.aircraftcarrier.generator.config.rules.DbColumnType.BOOLEAN;
import static com.aircraftcarrier.generator.config.rules.DbColumnType.BYTE_ARRAY;
import static com.aircraftcarrier.generator.config.rules.DbColumnType.CLOB;
import static com.aircraftcarrier.generator.config.rules.DbColumnType.DATE;
import static com.aircraftcarrier.generator.config.rules.DbColumnType.DOUBLE;
import static com.aircraftcarrier.generator.config.rules.DbColumnType.FLOAT;
import static com.aircraftcarrier.generator.config.rules.DbColumnType.INTEGER;
import static com.aircraftcarrier.generator.config.rules.DbColumnType.LONG;
import static com.aircraftcarrier.generator.config.rules.DbColumnType.STRING;

/**
 * DB2 字段类型转换
 *
 * @author zhanyao, hanchunlin
 * @since 2018-05-16
 */
public class DB2TypeConvert implements ITypeConvert {
    public static final DB2TypeConvert INSTANCE = new DB2TypeConvert();

    /**
     * @inheritDoc
     */
    @Override
    public IColumnType processTypeConvert(GlobalConfig config, String fieldType) {
        return TypeConverts.use(fieldType)
                .test(containsAny("char", "text", "json", "enum").then(STRING))
                .test(contains("bigint").then(LONG))
                .test(contains("smallint").then(BASE_SHORT))
                .test(contains("int").then(INTEGER))
                .test(containsAny("date", "time", "year").then(DATE))
                .test(contains("bit").then(BOOLEAN))
                .test(contains("decimal").then(BIG_DECIMAL))
                .test(contains("clob").then(CLOB))
                .test(contains("blob").then(BLOB))
                .test(contains("binary").then(BYTE_ARRAY))
                .test(contains("float").then(FLOAT))
                .test(contains("double").then(DOUBLE))
                .or(STRING);
    }

}
