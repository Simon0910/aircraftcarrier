package com.aircraftcarrier.framework.data.handlers;

import com.aircraftcarrier.framework.data.plugins.handler.DataPermissionHandler;
import com.aircraftcarrier.framework.tookit.MapUtil;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;

import java.util.Map;

/**
 * @author lzp
 */
@Slf4j
public class AutoDataPermissionHandler implements DataPermissionHandler {
    private static final String TEST_4 = "com.farm.infrastructure.repository.DemoMapper.selectById";
    /**
     * 这里可以理解为数据库配置的数据权限规则 SQL
     */
    private static final Map<String, Boolean> SQL_SEGMENT_CONTROL_MAP = MapUtil.newHashMap(128);
    private static final Map<String, String> SQL_SEGMENT_MAP = MapUtil.newHashMap(128);

    static {
        SQL_SEGMENT_CONTROL_MAP.put(TEST_4, false);
        SQL_SEGMENT_MAP.put(TEST_4, "seller_name like 'abc%'");
    }

    @Override
    public boolean willIgnoreDataPermissionInterceptor(String mappedStatementId) {
        Boolean ignore = SQL_SEGMENT_CONTROL_MAP.get(mappedStatementId);
        return ignore == null || ignore;
    }

    @Override
    public Expression getSqlSegment(Expression where, String mappedStatementId) {
        try {
            String sqlSegment = SQL_SEGMENT_MAP.get(mappedStatementId);
            Expression sqlSegmentExpression = CCJSqlParserUtil.parseCondExpression(sqlSegment);
            if (null != where) {
                log.info("原 where : {}", where);
                if (mappedStatementId.equals(TEST_4)) {
                    // 这里测试返回 OR 条件
                    return new OrExpression(where, sqlSegmentExpression);
                }
                return new AndExpression(where, sqlSegmentExpression);
            }
            return sqlSegmentExpression;
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
        return null;
    }
}
