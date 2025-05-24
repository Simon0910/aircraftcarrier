package com.aircraftcarrier.framework.data;

import com.aircraftcarrier.framework.model.Page;
import com.aircraftcarrier.framework.model.request.PageQuery;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.page.PageMethod;

import java.io.Serializable;
import java.util.List;

/**
 * @author zh
 */
public class PageUtil {

    private PageUtil() {
    }

    public static <R extends Serializable, E> Page<R> getPage(PageQuery pageQuery, ISelect select, Class<R> modelClass) {
        com.github.pagehelper.Page<E> page = PageMethod.startPage(pageQuery.getPageNum(), pageQuery.getPageSize(), true);
        safeExecute(select);
        return Page.build(page.getResult(), page.getTotal(), modelClass);
    }

    public static <E extends Serializable> Page<E> getPage(PageQuery pageQuery, ISelect select) {
        com.github.pagehelper.Page<E> page = PageMethod.startPage(pageQuery.getPageNum(), pageQuery.getPageSize(), true);
        safeExecute(select);
        return Page.build(page.getResult(), page.getTotal());
    }

    public static long count(ISelect select) {
        com.github.pagehelper.Page<?> page = PageMethod.startPage(1, -1, true);
        safeExecute(select);
        return page.getTotal();
    }

    public static long calcPages(int pageSize, long total) {
        if (total < 1) {
            return 0;
        }
        if (pageSize <= 0) {
            return 1;
        }
        return (int) ((total + pageSize - 1) / pageSize);
    }

    public static long calcPages(int pageSize, ISelect select) {
        long total = count(select);
        return calcPages(pageSize, total);
    }

    public static <E> List<E> getPageList(PageQuery pageQuery, ISelect select) {
        com.github.pagehelper.Page<E> page = PageMethod.startPage(pageQuery.getPageNum(), pageQuery.getPageSize(), false);
        safeExecute(select);
        return page.getResult();
    }

    public static <E> List<E> getPageList(int pageNum, int pageSize, ISelect select) {
        com.github.pagehelper.Page<E> page = PageMethod.startPage(pageNum, pageSize, false);
        safeExecute(select);
        return page.getResult();
    }

    private static void safeExecute(ISelect select) {
        try {
            select.doSelect();
        } finally {
            PageMethod.clearPage();
        }
    }


}