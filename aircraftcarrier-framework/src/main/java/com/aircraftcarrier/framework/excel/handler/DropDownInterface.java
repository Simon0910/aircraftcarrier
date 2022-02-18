package com.aircraftcarrier.framework.excel.handler;

/**
 * @author lzp
 */
@FunctionalInterface
public interface DropDownInterface {
    /**
     * 获取下来资源
     *
     * @return String[]
     */
    String[] getSource();
}