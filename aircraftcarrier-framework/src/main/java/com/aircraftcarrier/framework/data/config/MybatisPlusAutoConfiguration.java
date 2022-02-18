package com.aircraftcarrier.framework.data.config;

import com.aircraftcarrier.framework.data.component.MybatisMapperRefresh;
import com.aircraftcarrier.framework.data.core.MybatisMetaObjectHandler;
import com.aircraftcarrier.framework.data.core.MybatisSqlInjector;
import com.aircraftcarrier.framework.data.handlers.AutoDataPermissionHandler;
import com.aircraftcarrier.framework.data.plugins.MybatisInterceptor;
import com.aircraftcarrier.framework.data.plugins.inner.DataPermissionInterceptor;
import com.aircraftcarrier.framework.tookit.SpringContextUtils;
import com.aircraftcarrier.framework.tookit.config.ToolsAutoConfiguration;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;

/**
 * @author lzp
 */
@ConditionalOnClass(MybatisConfiguration.class)
@AutoConfigureAfter(ToolsAutoConfiguration.class)
@Configuration
public class MybatisPlusAutoConfiguration {

    private static final ResourcePatternResolver RESOURCE_RESOLVER = new PathMatchingResourcePatternResolver();

    @Value("${mybatis-plus.mapper-locations}")
    private String mapperLocations;

    @Value("${mybatis-plus.refresh-mapper.enabled:false}")
    private Boolean refreshMapper;

    @Bean
    @ConditionalOnClass(net.sf.jsqlparser.expression.Expression.class)
    public AutoDataPermissionHandler autoDataPermissionHandler() {
        return new AutoDataPermissionHandler();
    }


    /**
     * 自动填充功能
     *
     * @return MetaObjectHandler
     */
    @Bean
    @ConditionalOnClass(MetaObjectHandler.class)
    public MetaObjectHandler metaHandler() {
        // mybatis-plus自定义Sql注入
        return new MybatisMetaObjectHandler();
    }

    @Bean
    @ConditionalOnClass(DefaultSqlInjector.class)
    public DefaultSqlInjector sqlInjector() {
        // mybatis-plus方法扩展
        return new MybatisSqlInjector();
    }

    @Bean
    @ConditionalOnClass(Interceptor.class)
    public Interceptor mybatisInterceptor() {
        MybatisInterceptor mybatisInterceptor = new MybatisInterceptor();
        // mybatis自定义数据权限拦截器
        mybatisInterceptor.addInnerInterceptor(new DataPermissionInterceptor(autoDataPermissionHandler()));
        return mybatisInterceptor;
    }

    @Bean
    @ConditionalOnClass(MybatisPlusInterceptor.class)
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor plusInterceptor = new MybatisPlusInterceptor();
        // mybatis-plus防止全表更新与删除拦截器
        plusInterceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        return plusInterceptor;
    }

    /**
     * mybatis-plus
     * mapper.xml 热加载
     *
     * @return MybatisMapperRefresh
     */
    @ConditionalOnProperty(prefix = "mybatis-plus.refresh-mapper", value = "enabled", havingValue = "true")
    @ConditionalOnClass(SqlSessionFactory.class)
    @Bean
    public MybatisMapperRefresh mybatisMapperRefresh() {
        SqlSessionFactory sqlSessionFactory = (SqlSessionFactory) SpringContextUtils.getBean("sqlSessionFactory");
        Resource[] resources = new Resource[0];
        try {
            resources = RESOURCE_RESOLVER.getResources(mapperLocations);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new MybatisMapperRefresh(resources, sqlSessionFactory, 10, 5, refreshMapper);

    }
}
