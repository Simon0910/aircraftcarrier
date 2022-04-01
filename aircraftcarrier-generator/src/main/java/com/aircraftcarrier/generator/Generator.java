package com.aircraftcarrier.generator;

/**
 * 对于没有公共字段的表
 * 需要几处改动
 * 1. DO类不要继承BaseDO
 * 2. Mapper类 继承com.baomidou.mybatisplus.core.mapper.BaseMapper
 * 3. mapper.xml中 Base_Where_Clause 条件需要存在的字段
 *
 * @author lzp
 */
public class Generator {
    private static final String URL = "jdbc:mysql://localhost:3306/aircraftcarrier?allowMultiQueries=true&useSSL=false&rewriteBatchedStatements=true&zeroDateTimeBehavior=convertToNull&serverTimezone=UTC";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "123456";
    private static final String AUTHOR = "lzp";
    private static final String[] TABLE_PREFIX = new String[]{"portal_", "farm_"};
    private static final String OUTPUT_DIR = "D:\\aircraftcarrier\\aircraftcarrier-marketing-store";
    private static final String PARENT = "com.aircraftcarrier.marketing.store";


    public static void main(String[] args) {
        GeneratorStart generatorStart = new GeneratorStart(URL, USERNAME, PASSWORD, AUTHOR, OUTPUT_DIR, TABLE_PREFIX);
        generatorStart.parent(PARENT);
        generatorStart.moduleName("product")
                .tables("product_details")
                .doStart();
    }
}
