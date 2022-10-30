package com.aircraftcarrier.marketing.store.client.demo.excel;

import com.aircraftcarrier.framework.model.AbstractCommand;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/**
 * Demo ImportCmd
 *
 * @author lzp
 * @date 2022-10-30
 * @since 1.0
 */
@Getter
@Setter
public class DemoImportExcelCmd extends AbstractCommand {
    /**
     * 上传文件
     */
    private MultipartFile file;

    public DemoImportExcelCmd(MultipartFile file) {
        this.file = file;
    }
}
