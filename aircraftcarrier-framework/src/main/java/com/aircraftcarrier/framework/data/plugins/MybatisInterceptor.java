package com.aircraftcarrier.framework.data.plugins;

import com.aircraftcarrier.framework.data.plugins.inner.InnerInterceptor;
import com.github.pagehelper.util.MetaObjectUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 数据过滤
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0
 */
@Slf4j
@Intercepts(
        {
                @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}),
                @Signature(type = StatementHandler.class, method = "getBoundSql", args = {}),
                @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        }
)
public class MybatisInterceptor implements Interceptor {
    private static final String SQL = "sql";
    private static final String LOG = "-- sql:\n";

    @Setter
    private List<InnerInterceptor> interceptors = new ArrayList<>();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object target = invocation.getTarget();
        Object[] args = invocation.getArgs();
        if (target instanceof final Executor executor) {
            Object parameter = args[1];
            boolean isUpdate = args.length == 2;
            MappedStatement ms = (MappedStatement) args[0];
            if (!isUpdate && ms.getSqlCommandType() == SqlCommandType.SELECT) {
                RowBounds rowBounds = (RowBounds) args[2];
                ResultHandler<?> resultHandler = (ResultHandler<?>) args[3];
                BoundSql boundSql;
                int size = 4;
                if (args.length == size) {
                    boundSql = ms.getBoundSql(parameter);
                } else {
                    // 几乎不可能走进这里面,除非使用Executor的代理对象调用query[args[6]]
                    boundSql = (BoundSql) args[5];
                }
                if (log.isDebugEnabled()) {
                    MetaObject metaObject = MetaObjectUtil.forObject(boundSql);
                    // String sql = SqlUtil.getSql(boundSql, parameter, ms.getConfiguration());
                    // log.debug("==> {}", sql);
                    metaObject.setValue(SQL, LOG + boundSql.getSql());
                }
                for (InnerInterceptor query : interceptors) {
                    if (!query.willDoQuery(executor, ms, parameter, rowBounds, resultHandler, boundSql)) {
                        return Collections.emptyList();
                    }
                    query.beforeQuery(executor, ms, parameter, rowBounds, resultHandler, boundSql);
                }
                CacheKey cacheKey = executor.createCacheKey(ms, parameter, rowBounds, boundSql);
                return executor.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);

            } else if (isUpdate) {
                for (InnerInterceptor update : interceptors) {
                    if (!update.willDoUpdate(executor, ms, parameter)) {
                        return -1;
                    }
                    update.beforeUpdate(executor, ms, parameter);
                }
            }
        } else {
            before((StatementHandler) target, args);
        }
        return invocation.proceed();
    }

    private void before(StatementHandler target, Object[] args) {
        // StatementHandler
        // 目前只有StatementHandler.getBoundSql方法args才为null
        if (null == args) {
            for (InnerInterceptor innerInterceptor : interceptors) {
                innerInterceptor.beforeGetBoundSql(target);
            }
        } else {
            Connection connections = (Connection) args[0];
            Integer transactionTimeout = (Integer) args[1];
            for (InnerInterceptor innerInterceptor : interceptors) {
                innerInterceptor.beforePrepare(target, connections, transactionTimeout);
            }
        }
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor || target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    public void addInnerInterceptor(InnerInterceptor innerInterceptor) {
        this.interceptors.add(innerInterceptor);
    }
}