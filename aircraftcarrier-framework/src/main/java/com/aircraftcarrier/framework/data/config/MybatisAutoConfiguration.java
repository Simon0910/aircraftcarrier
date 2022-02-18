package com.aircraftcarrier.framework.data.config;

import com.aircraftcarrier.framework.data.handlers.AutoEnumTypeHandler;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author lzp
 */
@ConditionalOnClass(org.apache.ibatis.session.Configuration.class)
@Configuration
public class MybatisAutoConfiguration {

    @Resource
    private SqlSessionFactory sqlSessionFactory;

    @PostConstruct
    public void init() {
        // 取得类型转换注册器
        TypeHandlerRegistry typeHandlerRegistry = sqlSessionFactory.getConfiguration().getTypeHandlerRegistry();
        // 注册默认枚举转换器
        typeHandlerRegistry.setDefaultEnumTypeHandler(AutoEnumTypeHandler.class);
    }

}