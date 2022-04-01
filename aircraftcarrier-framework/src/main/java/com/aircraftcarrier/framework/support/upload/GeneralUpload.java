package com.aircraftcarrier.framework.support.upload;

import cn.hutool.core.util.ObjectUtil;
import com.aircraftcarrier.framework.exception.SysException;
import com.aircraftcarrier.framework.tookit.RequestLimitUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * GeneralUpload
 *
 * @author lzp
 * @version 1.0
 * @date 2020/6/17
 */
@Slf4j
public class GeneralUpload extends AbstractDataUpload {

    /**
     * upload_key
     */
    private static final String UPLOAD_KEY = "sysUpload";

    /**
     * AbstractJfsFileDataUpload构造
     *
     * @param uploadFile uploadFile
     */
    public GeneralUpload(MultipartFile uploadFile) {
        super(uploadFile);
    }


    @Override
    protected Object getUploadObject() {
        return null;
    }

    @Override
    public Boolean doCheck() {
        return true;
    }


    public String upload() throws IOException {
        if (Boolean.FALSE.equals(doCheck())) {
            throw new SysException("上传文件错误!");
        }

        RequestLimitUtil requestLimit = RequestLimitUtil.getInstance();
        boolean success = requestLimit.require(UPLOAD_KEY, 10);
        if (!success) {
            throw new SysException("上传任务过多!");
        }

        try {
            log.info("upload start...");
            long start = System.currentTimeMillis();
            if (doCheck()) {
                doUpload();
                if (ObjectUtil.isNotEmpty(uploadObject) || ObjectUtil.isNotEmpty(getUploadObject())) {
                    doUploadObject();
                }
            }
            log.info("upload end 耗时[{}], resultId: [{}]", System.currentTimeMillis() - start, resultId);
        } catch (Exception e) {
            log.error("上传异常: ", e);
            throw new SysException(e.getMessage());
        } finally {
            requestLimit.release(UPLOAD_KEY);
        }

        return resultId;
    }


}
