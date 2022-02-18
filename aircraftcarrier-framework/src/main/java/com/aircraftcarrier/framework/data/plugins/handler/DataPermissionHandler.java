package com.aircraftcarrier.framework.data.plugins.handler;

import net.sf.jsqlparser.expression.Expression;

/**
 * @author lzp
 */
public interface DataPermissionHandler {

    /**
     * 忽略拦截
     *
     * @param mappedStatementId Mybatis MappedStatement Id 根据该参数可以判断具体执行方法
     * @return
     */
    default boolean willIgnoreDataPermissionInterceptor(String mappedStatementId) {
        return false;
    }

    /**
     * 获取数据权限 SQL 片段
     *
     * @param where             待执行 SQL Where 条件表达式
     * @param mappedStatementId Mybatis MappedStatement Id 根据该参数可以判断具体执行方法
     * @return JSqlParser 条件表达式
     */
    Expression getSqlSegment(Expression where, String mappedStatementId);
}
