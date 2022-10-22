package com.aircraftcarrier.framework.data;

import cn.hutool.core.util.PageUtil;
import com.aircraftcarrier.framework.data.core.MybatisBaseMapper;
import com.aircraftcarrier.framework.exception.ErrorCode;
import com.aircraftcarrier.framework.exception.ToolException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.BoundSqlInterceptor;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageSerializable;
import com.github.pagehelper.page.PageMethod;
import com.github.pagehelper.util.MetaObjectUtil;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SetOperationList;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.reflection.MetaObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author lzp
 */
@Slf4j
public class MybatisBatchUtil {
    /**
     * 数据库分批查询 批次大小
     */
    private static final Integer PAGE_SIZE = 5000;

    private MybatisBatchUtil() {
    }

    public static <T extends BaseDO<T>> List<T> selectAllListBatchByWrapper(LambdaQueryWrapper<T> queryWrapper, MybatisBaseMapper<T> mapper) {
        return selectAllListBatchByWrapper(queryWrapper, mapper, MybatisBatchUtil::invokeData, Integer.MAX_VALUE);
    }

    public static <T extends BaseDO<T>> List<T> selectAllListBatchByWrapper(LambdaQueryWrapper<T> queryWrapper, MybatisBaseMapper<T> mapper, Consumer<List<T>> callback) {
        return selectAllListBatchByWrapper(queryWrapper, mapper, callback, Integer.MAX_VALUE);
    }

    public static <T extends BaseDO<T>> List<T> selectAllListBatchByWrapper(LambdaQueryWrapper<T> queryWrapper, MybatisBaseMapper<T> mapper, int maxRowCount) {
        return selectAllListBatchByWrapper(queryWrapper, mapper, MybatisBatchUtil::invokeData, maxRowCount);
    }

    /**
     * 查询list，应对超大查询，超过DB服务端大小限制的可以使用此方法
     *
     * @param queryWrapper queryWrapper
     * @param mapper       mapper
     * @param callback     callback
     * @param maxRowCount  maxRowCount
     * @return List<T>
     */
    public static <T extends BaseDO<T>> List<T> selectAllListBatchByWrapper(LambdaQueryWrapper<T> queryWrapper,
                                                                            MybatisBaseMapper<T> mapper,
                                                                            Consumer<List<T>> callback,
                                                                            int maxRowCount) {
        long start = System.currentTimeMillis();
        List<T> result = new ArrayList<>();
        Long count = mapper.selectCount(queryWrapper);
        log.info("selectAllListBatchByWrapper - count: 【{}】 ", count);
        if (count < 1) {
            return result;
        }
        if (count <= PAGE_SIZE) {
            result = mapper.selectList(queryWrapper);
            callback.accept(result);
            return result;
        }
        if (count > maxRowCount) {
            count = (long) maxRowCount;
        }

        int pages = PageUtil.totalPage(count.intValue(), PAGE_SIZE);
        log.info("selectAllListBatchByWrapper - 总页数: 【{}】", pages);
        PageSerializable<T> pageSerializable;
        Serializable id = 0;
        for (int i = 0; i < pages; i++) {
            queryWrapper.gt(T::getId, id);
            pageSerializable = PageMethod.startPage(1, PAGE_SIZE, false)
                    .doSelectPageSerializable(() -> mapper.selectList(queryWrapper));

            List<T> list = pageSerializable.getList();
            if (list.isEmpty()) {
                return result;
            }
            callback.accept(list);
            result.addAll(list);
            T t = list.get(list.size() - 1);
            id = t.getId();
            list.clear(); //help gc
        }
        log.info("selectAllListBatchByWrapper - 批量查询完成 耗时：【{}】", (System.currentTimeMillis() - start));
        return result;
    }

    public static <T extends BaseDO<T>> List<T> selectAllListBatch(ISelect iSelect) {
        return selectAllListBatch(iSelect, MybatisBatchUtil::invokeData, Integer.MAX_VALUE);
    }

    public static <T extends BaseDO<T>> List<T> selectAllListBatch(ISelect iSelect, Consumer<List<T>> callback) {
        return selectAllListBatch(iSelect, callback, Integer.MAX_VALUE);
    }

    public static <T extends BaseDO<T>> List<T> selectAllListBatch(ISelect iSelect, int maxRowCount) {
        return selectAllListBatch(iSelect, MybatisBatchUtil::invokeData, maxRowCount);
    }

    /**
     * 批量逐页查询，适用于所有复杂情况
     * 性能略慢
     *
     * @param iSelect  自定义sql
     * @param callback 每个批次回调一次结果
     * @param <T>      返回类型
     * @return List<T> 返回结果
     */
    public static <T extends BaseDO<T>> List<T> selectAllListBatch(ISelect iSelect,
                                                                   Consumer<List<T>> callback,
                                                                   int maxRowCount) {
        long start = System.currentTimeMillis();
        List<T> result = new ArrayList<>();
        long count = PageMethod.count(iSelect);
        log.info("selectAllListBatch - count: 【{}】 ", count);
        if (count < 1) {
            return result;
        }

        PageSerializable<T> pageSerializable;
        if (count <= PAGE_SIZE) {
            pageSerializable = PageMethod.startPage(1, PAGE_SIZE, false)
                    .doSelectPageSerializable(iSelect);
            List<T> list = pageSerializable.getList();
            callback.accept(list);
            return list;
        }
        if (count > maxRowCount) {
            count = maxRowCount;
        }

        int pages = PageUtil.totalPage((int) count, PAGE_SIZE);
        log.info("selectAllListBatch - 总页数: 【{}】", pages);
        for (int i = 0; i < pages; i++) {
            pageSerializable = PageMethod.startPage(i + 1, PAGE_SIZE, false)
                    .doSelectPageSerializable(iSelect);

            List<T> list = pageSerializable.getList();
            if (list.isEmpty()) {
                return result;
            }
            callback.accept(list);
            result.addAll(list);
            list.clear(); //help gc
        }
        log.info("selectAllListBatch - 批量查询完成 耗时：【{}】", (System.currentTimeMillis() - start));
        return result;
    }

    public static <T extends BaseDO<T>> List<T> selectAllListBatchWithId(ISelect iSelect) {
        return selectAllListBatchWithId(iSelect, MybatisBatchUtil::invokeData, Integer.MAX_VALUE);
    }

    public static <T extends BaseDO<T>> List<T> selectAllListBatchWithId(ISelect iSelect, Consumer<List<T>> callback) {
        return selectAllListBatchWithId(iSelect, callback, Integer.MAX_VALUE);
    }

    public static <T extends BaseDO<T>> List<T> selectAllListBatchWithId(ISelect iSelect, int maxRowCount) {
        return selectAllListBatchWithId(iSelect, MybatisBatchUtil::invokeData, maxRowCount);
    }

    /**
     * 适用于单表大批量数据查询
     * 性能较好
     *
     * @param iSelect  自定义sql是单表，且没有排序
     * @param callback 每个批次回调一次结果
     * @param <T>      返回类型
     * @return List<T> 返回结果
     */
    public static <T extends BaseDO<T>> List<T> selectAllListBatchWithId(ISelect iSelect,
                                                                         Consumer<List<T>> callback,
                                                                         int maxRowCount) {
        long start = System.currentTimeMillis();
        List<T> result = new ArrayList<>();
        long count = PageMethod.count(iSelect);
        log.info("selectAllListBatchWithId - count: 【{}】 ", count);
        if (count < 1) {
            return result;
        }

        PageSerializable<T> pageSerializable;
        if (count <= PAGE_SIZE) {
            pageSerializable = PageMethod.startPage(1, PAGE_SIZE, false).doSelectPageSerializable(iSelect);
            result = pageSerializable.getList();
            callback.accept(result);
            return result;
        }
        if (count > maxRowCount) {
            count = maxRowCount;
        }

        int pages = PageUtil.totalPage((int) count, PAGE_SIZE);
        log.info("selectAllListBatchWithId - 总页数: 【{}】", pages);
        Serializable id = 0;
        for (int i = 0; i < pages; i++) {
            pageSerializable = PageMethod.startPage(1, PAGE_SIZE, false)
                    .boundSqlInterceptor(new MyBoundSqlInterceptor(id))
                    .doSelectPageSerializable(iSelect);

            List<T> list = pageSerializable.getList();
            if (list.isEmpty()) {
                return result;
            }
            callback.accept(list);
            result.addAll(list);
            T t = list.get(list.size() - 1);
            id = t.getId();
            list.clear(); //help gc
        }
        log.info("selectAllListBatchWithId - 批量查询完成 耗时：【{}】", (System.currentTimeMillis() - start));
        return result;
    }

    public static <T extends BaseDO<T>> void invokeData(List<T> list) {
        log.debug("invokeData...");
    }

    static class MyBoundSqlInterceptor implements BoundSqlInterceptor {

        private final Serializable id;

        public MyBoundSqlInterceptor(Serializable id) {
            if (id == null) {
                throw new IllegalArgumentException("must query master table `id` column");
            }
            this.id = id;
        }

        @Override
        public BoundSql boundSql(Type type, BoundSql boundSql, CacheKey cacheKey, Chain chain) {
            if (type == Type.PAGE_SQL) {
                String sql;
                try {
                    sql = handleSql(boundSql);
                } catch (JSQLParserException e) {
                    log.error("handleSql(boundSql) error: ", e);
                    throw new ToolException(ErrorCode.INTERNAL_SERVER_ERROR, "handleSql(boundSql) error");
                }
                MetaObject metaObject = MetaObjectUtil.forObject(boundSql);
                metaObject.setValue("sql", sql);
            }
            return chain.doBoundSql(type, boundSql, cacheKey);
        }

        private String handleSql(BoundSql boundSql) throws JSQLParserException {
            String originalSql = boundSql.getSql();
            Statement statement = CCJSqlParserUtil.parse(originalSql);
            if (statement instanceof Select) {
                processSelect((Select) statement);
            }
            return statement.toString();
        }

        protected void processSelect(Select select) {
            SelectBody selectBody = select.getSelectBody();
            if (selectBody instanceof PlainSelect) {
                this.setWhere((PlainSelect) selectBody);
            } else if (selectBody instanceof SetOperationList setOperationList) {
                List<SelectBody> selects = setOperationList.getSelects();
                selects.forEach(s -> this.setWhere((PlainSelect) s));
            }

        }

        private void setWhere(PlainSelect plainSelect) {
            List<Join> joins = plainSelect.getJoins();
            FromItem fromItem = plainSelect.getFromItem();
            Alias alias = fromItem.getAlias();
            String aliasName = alias.getName();
            List<OrderByElement> orderByElements = new ArrayList<>();
            OrderByElement orderByElement = new OrderByElement();

            if (joins == null) {
                orderByElement.setExpression(new Column("id"));
                orderByElements.add(orderByElement);
                plainSelect.setOrderByElements(orderByElements);

                GreaterThan greaterThan = new GreaterThan();
                greaterThan.setLeftExpression(new Column("id"));
                setId(plainSelect, greaterThan);

            } else {
                orderByElement.setExpression(new Column(aliasName + ".id"));
                orderByElements.add(orderByElement);
                plainSelect.setOrderByElements(orderByElements);

                GreaterThan greaterThan = new GreaterThan();
                greaterThan.setLeftExpression(new Column(aliasName + ".id"));
                setId(plainSelect, greaterThan);

            }
        }

        private void setId(PlainSelect plainSelect, GreaterThan greaterThan) {
            greaterThan.setRightExpression(new LongValue(String.valueOf(id)));

            Expression where = plainSelect.getWhere();
            Expression expression;
            if (where != null) {
                expression = new AndExpression(where, greaterThan);
            } else {
                expression = greaterThan;
            }
            plainSelect.setWhere(expression);
        }
    }
}


