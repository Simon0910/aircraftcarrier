package com.aircraftcarrier.framework.web.ser;

import com.aircraftcarrier.framework.tookit.DateTimeUtil;

/**
 * Date2ShortSerializer
 *
 * @author liuzhipeng
 */
public class Date2ShortStringSerializer extends DateSerializer {
    public Date2ShortStringSerializer() {
        super(DateTimeUtil.DATE_FORMAT);
    }
}
