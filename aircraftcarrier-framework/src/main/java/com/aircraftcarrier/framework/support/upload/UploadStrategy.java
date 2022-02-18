package com.aircraftcarrier.framework.support.upload;

import java.io.IOException;

/**
 * UploadStrategy
 * 上传excel策略
 *
 * @author lzp
 * @version 1.0
 * @date 2020/5/26
 */
public interface UploadStrategy {
    /**
     * 文件校验
     *
     * @return
     * @throws IOException
     */
    Boolean doCheck() throws IOException;

    /**
     * 文件上传
     *
     * @return
     * @throws IOException
     */
    String doUpload() throws IOException;

}
