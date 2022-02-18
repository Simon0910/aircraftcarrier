package com.aircraftcarrier.framework.support.upload;

import java.util.List;

/**
 * 数据接口Gateway
 *
 * @author lzp
 * @version 1.0
 * @date 2020/6/16
 */
public interface GeneralDataGateway {

    /**
     * 下载数据详情 from jfs
     *
     * @param fileJfs      fileJfs
     * @param responseType responseType
     * @return T
     */
    <T> T downloadObject(String fileJfs, Class<T> responseType);

    /**
     * 下载数据详情 from jfs
     *
     * @param fileJfs      fileJfs
     * @param responseType responseType
     * @return java.util.List<T>
     */
    <T> List<T> downloadList(String fileJfs, Class<T> responseType);
}
