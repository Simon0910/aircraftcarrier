package com.aircraftcarrier.framework.data.core;

import com.aircraftcarrier.framework.data.core.method.SelectCursor;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.metadata.TableInfo;

import java.util.List;

/**
 * 自定义Sql注入
 *
 * @author zh
 */
public class MybatisSqlInjector extends DefaultSqlInjector {

    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass, TableInfo tableInfo) {
        List<AbstractMethod> methodList = super.getMethodList(mapperClass, tableInfo);
        /*
         * 以下 3 个为内置选装件
         * 头 2 个支持字段筛选函数
         * 例: 不要指定了 update 填充的字段
         */
        methodList.add(new SelectCursor());
        return methodList;
    }
}
