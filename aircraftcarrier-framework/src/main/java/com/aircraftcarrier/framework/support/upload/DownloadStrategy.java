package com.aircraftcarrier.framework.support.upload;


import com.aircraftcarrier.framework.exception.SysException;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * DownloadStrategy
 *
 * @author lzp
 * @version 1.0
 * @date 2020-05-25
 */
public interface DownloadStrategy {

    /**
     * 文件下载
     *
     * @param response response
     */
    void doDownload(HttpServletResponse response);

    /**
     * 写入浏览器
     *
     * @param response  response
     * @param filename  filename
     * @param sheetName sheetName
     * @param list      list
     * @param clazz     clazz
     * @throws SysException 写异常
     */
    default <T> void write(HttpServletResponse response, String filename, String sheetName, List<T> list, Class<T> clazz) throws SysException {

    }
}
