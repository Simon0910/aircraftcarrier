package com.aircraftcarrier.framework.support.upload;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author lzp
 */
@Slf4j
public abstract class AbstractDataUpload extends AbstractUpload {

    /**
     * 文件上传唯一标识
     */
    protected String objectResultId;

    /**
     * 文件上传数据结构
     */
    protected Object uploadObject;

    /**
     * 构造
     *
     * @param uploadFile uploadFile
     */
    protected AbstractDataUpload(MultipartFile uploadFile) {
        super(uploadFile);
    }

    /**
     * 上传Object
     *
     * @return java.lang.Object
     */
    protected abstract Object getUploadObject();

    /**
     * 上传数据流 => jfs
     *
     * @return java.lang.String jfs唯一标识
     */
    public String doUploadObject() {
        if (ObjectUtil.isEmpty(uploadObject)) {
            uploadObject = getUploadObject();
        }
        Assert.notNull(uploadObject, "uploadObject must not be null!");
        log.info("doUploadObject start...");
        long start = System.currentTimeMillis();
        // .. objectResultId = ...
        log.info("doUploadObject end: 耗时[{}],  [{}] ", System.currentTimeMillis() - start, this.objectResultId);
        return this.objectResultId;
    }
}
