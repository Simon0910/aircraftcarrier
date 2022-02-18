package com.aircraftcarrier.framework.data;

import com.aircraftcarrier.framework.model.request.PageQuery;
import com.aircraftcarrier.framework.model.response.PageResponse;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.Page;
import com.github.pagehelper.page.PageMethod;

import java.io.Serializable;

/**
 * @author zh
 */
public class PageUtil {

    private PageUtil() {
    }

    public static <R extends Serializable, E> PageResponse<R> getPage(PageQuery pageQuery, ISelect select, Class<R> modelClass) {
        Page<E> page = PageMethod.startPage(pageQuery.getPageNum(), pageQuery.getPageSize(), true).doSelectPage(select);
        return PageResponse.build(page.getResult(), page.getTotal(), modelClass);
    }

    public static <E extends Serializable> PageResponse<E> getPage(PageQuery pageQuery, ISelect select) {
        Page<E> page = PageMethod.startPage(pageQuery.getPageNum(), pageQuery.getPageSize(), true).doSelectPage(select);
        return PageResponse.build(page.getResult(), page.getTotal());
    }
}