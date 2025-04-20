package com.aircraftcarrier.marketing.store.app.demo.executor.excel;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.aircraftcarrier.framework.excel.AbstractImportUploadWorker;
import com.aircraftcarrier.framework.excel.util.EasyExcelReadUtil;
import com.aircraftcarrier.framework.excel.util.ReadResult;
import com.aircraftcarrier.framework.model.BatchResult;
import com.aircraftcarrier.framework.model.response.SingleResponse;
import com.aircraftcarrier.framework.tookit.BeanUtil;
import com.aircraftcarrier.marketing.store.client.demo.excel.DemoImportExcelCmd;
import com.aircraftcarrier.marketing.store.client.demo.excel.template.DemoImportExcel;
import com.aircraftcarrier.marketing.store.infrastructure.repository.DemoRepository;
import com.aircraftcarrier.marketing.store.infrastructure.repository.dataobject.DemoDo;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Objects;


/**
 * 批量导入 执行类
 *
 * @author lzp
 * @date 2022-10-30
 * @since 1.0
 */
@Slf4j
@Component
public class DemoImportCmdExe {

    /**
     * ProductDetailsRepository
     */
    @Resource
    private DemoRepository demoRepository;

    /**
     * execute
     *
     * @param importCmd importCmd
     * @return SingleResponse<BatchResult>
     */
    public SingleResponse<BatchResult> execute(DemoImportExcelCmd importCmd) throws IOException {
        MultipartFile file = importCmd.getFile();
        EasyExcelReadUtil.checkExcelFile(file);
        ReadResult<DemoImportExcel> readResult = EasyExcelReadUtil.readAllList(file.getInputStream(), DemoImportExcel.class, 1);

//        BatchResult batchResult = ImportUploadWorkerBuilder.<DemoImportExcel>builder().worker(new DemoImportImportUpload()).rowList(readResult.getRowList()).batchCheckSize(100).batchInvokeSize(1000).build().doWork();
//        BatchResult batchResult = new DemoImportImportUpload().builder().rowList(readResult.getRowList()).batchCheckSize(100).batchInvokeSize(1000).build().doWork();
//        return SingleResponse.ok(batchResult);
        EasyExcelReadUtil.readBatchRow(file.getInputStream(), DemoImportExcel.class, 0, 0, 1, (list, context) -> {
            System.out.println(JSON.toJSONString(list));
        });
        return null;
    }

    /**
     * 上传类
     */
    class DemoImportImportUpload extends AbstractImportUploadWorker<DemoImportExcel> {

        @Override
        protected DemoImportExcel preCheck(DemoImportExcel row) {
            if (checkNullField(row)) {
                return row;
            }
            return null;
        }

        @Override
        protected List<DemoImportExcel> preBatchCheck(List<DemoImportExcel> rowList) {
            log.info("preBatchCheck -> rowList.size:: {}", rowList.size());
            return rowList;
        }

        @Override
        protected void doBatchInvoke(List<DemoImportExcel> rowList) {
            log.info("doBatchInvoke -> rowList.size:: {}", rowList.size());
            doSaveBatch(rowList);
        }

        /**
         * checkNullField
         * 如果必要，可能要手写枚举校验
         *
         * @param row row
         * @return boolean
         */
        private boolean checkNullField(DemoImportExcel row) {
            if (CharSequenceUtil.isBlank(row.getBizNo())) {
                batchResult.addErrorMsg(row.getRowNo(), "业务主键为空");
                return false;
            }
            if (CharSequenceUtil.isBlank(row.getSellerNo())) {
                batchResult.addErrorMsg(row.getRowNo(), "商家编码为空");
                return false;
            }
            if (CharSequenceUtil.isBlank(row.getSellerName())) {
                batchResult.addErrorMsg(row.getRowNo(), "商家名称为空");
                return false;
            }
            if (CharSequenceUtil.isBlank(row.getDescription())) {
                batchResult.addErrorMsg(row.getRowNo(), "说明为空");
                return false;
            }
            if (Objects.isNull(row.getDataType())) {
                batchResult.addErrorMsg(row.getRowNo(), "类型为null");
                return false;
            }
            if (Objects.isNull(row.getDeleted())) {
                batchResult.addErrorMsg(row.getRowNo(), "删除标识,0:正常,1:删除为null");
                return false;
            }
            return true;
        }

        /**
         * 保存数据库
         *
         * @param rowList rowList
         */
        private void doSaveBatch(List<DemoImportExcel> rowList) {
            if (CollUtil.isEmpty(rowList)) {
                return;
            }
            // do saveBatch ...
            List<DemoDo> list = BeanUtil.convertList(rowList, DemoDo.class);
            demoRepository.saveBatch(list);
            batchResult.increaseSuccess(rowList.size());
        }

    }


}
