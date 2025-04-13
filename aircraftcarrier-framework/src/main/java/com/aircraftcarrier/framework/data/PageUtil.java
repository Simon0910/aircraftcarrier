package com.aircraftcarrier.framework.data;

import com.aircraftcarrier.framework.model.request.PageQuery;
import com.aircraftcarrier.framework.model.Page;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;

import java.io.Serializable;

/**
 * @author zh
 */
public class PageUtil {

    private PageUtil() {
    }

    public static <R extends Serializable, E> Page<R> getPage(PageQuery pageQuery, ISelect select, Class<R> modelClass) {
        com.github.pagehelper.Page<E> page = PageHelper.startPage(pageQuery.getPageNum(), pageQuery.getPageSize(), true);
        safeExecute(select);
        return Page.build(page.getResult(), page.getTotal(), modelClass);
    }

    public static <E extends Serializable> Page<E> getPage(PageQuery pageQuery, ISelect select) {
        com.github.pagehelper.Page<E> page = PageHelper.startPage(pageQuery.getPageNum(), pageQuery.getPageSize(), true);
        safeExecute(select);
        return Page.build(page.getResult(), page.getTotal());
    }

    private static void safeExecute(ISelect select) {
        try {
            select.doSelect();
        } finally {
            PageHelper.clearPage();
        }
    }
}