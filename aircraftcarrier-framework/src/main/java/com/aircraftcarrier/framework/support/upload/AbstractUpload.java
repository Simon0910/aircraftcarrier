package com.aircraftcarrier.framework.support.upload;

import cn.hutool.core.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

/**
 * AbstractExcelUpload
 * 文件上传接口
 *
 * @author lzp
 * @version 1.0
 * @date 2020/5/26
 */
@Slf4j
public abstract class AbstractUpload implements UploadStrategy {
    /**
     * 上传文件
     */
    protected MultipartFile uploadFile;

    /**
     * 校验结果
     */
    protected boolean checkSuccess;

    /**
     * 上传结果
     */
    protected String resultId;

    /**
     * AbstractJfsFileUpload构造
     */
    protected AbstractUpload() {
    }

    /**
     * 构造
     */
    protected AbstractUpload(MultipartFile uploadFile) {
        this.uploadFile = uploadFile;
    }

    /**
     * 上传
     */
    @Override
    public String doUpload() {
        Assert.notNull(uploadFile, "uploadFile must not be null!");
        log.info("doUpload start...");
        long start = System.currentTimeMillis();
        //
        log.info("doUpload end 耗时[{}], resultId: [{}]", System.currentTimeMillis() - start, resultId);
        return resultId;
    }

}
