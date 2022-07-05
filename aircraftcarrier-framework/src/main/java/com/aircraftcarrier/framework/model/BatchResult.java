package com.aircraftcarrier.framework.model;


import com.aircraftcarrier.framework.tookit.MapUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * BatchResult
 * 统一结果明细提示
 *
 * @author lzp
 * @version 1.0
 * @date 2020/7/24
 */
public class BatchResult implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * 批量结果明细类型
     */
    public static final String BATCH_RESULT_ERROR = "BATCH_RESULT_ERROR";

    /**
     * rowNo: 行号
     */
    private static final String ROW_NO = "rowNo";

    /**
     * msg: 错误信息
     */
    private static final String MSG = "msg";

    /**
     * errorType: 错误类型
     */
    private static final String ERROR_TYPE = "errorType";

    /**
     * errorCode: 错误码
     */
    private static final String ERROR_CODE = "errorCode";

    /**
     * ID: 数据主键
     */
    private static final String ID = "id";

    /**
     * 批量操作
     * 错误明细
     */
    private final transient Map<String, Map<String, Object>> errorTypeMap;

    /**
     * 批量上传
     * 错误明细
     */
    private final transient TreeMap<Integer, Map<String, Object>> errorRowTreeMap = new TreeMap<>();

    /**
     * 错误明细上限
     */
    private final Integer errorUpLimit;

    /**
     * 成功条数
     */
    private Integer successCount = 0;

    /**
     * 错误条数
     */
    private Integer errorCount = 0;

    /**
     * 最后错误行号
     */
    private Integer lastErrorRowNo = -1;

    /**
     * BatchResult 构造
     */
    public BatchResult() {
        this(100);
    }

    /**
     * BatchResult 构造
     *
     * @param errorUpLimit 最大错误限制
     */
    public BatchResult(int errorUpLimit) {
        this.errorUpLimit = errorUpLimit;
        this.errorTypeMap = MapUtil.newHashMap(errorUpLimit);
    }

    /**
     * 成功标识
     */
    public boolean success() {
        return !hasError();
    }

    /**
     * 是否有错误明细
     */
    public boolean hasError() {
        return errorCount > 0;
    }

    /**
     * 重置成功数
     */
    public void resetSuccessCount() {
        this.successCount = 0;
    }

    /**
     * 自增
     */
    public void increaseSuccess() {
        successCount++;
    }

    /**
     * 追加成功统计数
     */
    public void increaseSuccess(int successSum) {
        this.successCount += successSum;
    }

    /**
     * 追加错误统计数
     *
     * @param errorSum errorSum
     */
    public void increaseError(int errorSum) {
        this.errorCount += errorSum;
    }

    /**
     * 添加错误明细
     *
     * @param rowNo    rowNo
     * @param errorMsg errorMsg
     */
    public void addErrorMsg(Integer rowNo, String errorMsg) {
        lastErrorRowNo = rowNo;
        if (errorCount > errorUpLimit) {
            return;
        }

        Map<String, Object> errorRow = errorRowTreeMap.get(rowNo);
        if (errorRow != null) {
            errorRow.put(MSG, errorRow.get(MSG) + ", " + errorMsg);
        } else {
            errorRow = new HashMap<>(3);
            errorRow.put(ROW_NO, rowNo);
            errorRow.put(MSG, errorMsg);
            errorRowTreeMap.put(rowNo, errorRow);
            errorCount++;
        }
    }

    /**
     * 添加错误明细
     *
     * @param errorMsg errorMsg
     */
    public void addErrorMsg(String errorMsg) {
        if (errorCount > errorUpLimit) {
            return;
        }
        Map<String, Object> uploadFileErrorMsgType = new HashMap<>(2);
        uploadFileErrorMsgType.put(MSG, errorMsg);
        putErrorMsgMap(String.valueOf(System.nanoTime()), uploadFileErrorMsgType);
    }

    /**
     * 添加错误明细
     *
     * @param errorType errorType
     * @param errorMsg  errorMsg
     */
    public void addErrorMsg(String errorType, String errorMsg) {
        if (errorCount > errorUpLimit) {
            return;
        }
        Map<String, Object> errorObj = new HashMap<>(3);
        errorObj.put(ERROR_TYPE, errorType);
        errorObj.put(MSG, errorMsg);
        putErrorMsgMap(errorType, errorObj);
    }

    /**
     * 添加错误明细
     *
     * @param errorType errorType
     * @param errorCode errorCode
     * @param errorMsg  errorMsg
     * @param id        id
     */
    public void addErrorMsg(String errorType, String errorCode, String errorMsg, Long id) {
        if (errorCount > errorUpLimit) {
            return;
        }
        Map<String, Object> errorObj = new HashMap<>(6);
        errorObj.put(ERROR_TYPE, errorType);
        errorObj.put(ID, id);
        errorObj.put(ERROR_CODE, errorCode);
        errorObj.put(MSG, errorMsg);
        putErrorMsgMap(errorType, errorObj);
    }

    /**
     * 根据错误类型 添加错误信息
     *
     * @param errorType errorType
     * @param errorObj  errorObj
     */
    private void putErrorMsgMap(String errorType, Map<String, Object> errorObj) {
        Map<String, Object> originObj = errorTypeMap.get(errorType);
        if (originObj != null) {
            originObj.put(MSG, originObj.get(MSG) + ", " + errorObj.get(MSG));
        } else {
            errorTypeMap.put(errorType, errorObj);
            errorCount++;
        }
    }

    /**
     * 检查某行是否有错误记录
     *
     * @param rowNo rowNo
     * @return boolean
     */
    public boolean hasErrorByRowNo(Integer rowNo) {
        return errorRowTreeMap.get(rowNo) != null;
    }

    /**
     * 最后一个统计错误的行号
     *
     * @return Integer
     */
    public Integer lastErrorRowNo() {
        return lastErrorRowNo;
    }

    /**
     * 总条数展示
     *
     * @return java.lang.Integer
     */
    public Integer getTotal() {
        return successCount + errorCount;
    }

    /**
     * 成功数展示
     *
     * @return Integer
     */
    public Integer getSuccessCount() {
        return successCount;
    }

    /**
     * 错误数展示
     *
     * @return Integer
     */
    public Integer getErrorCount() {
        return errorCount;
    }

    /**
     * 错误明细
     */
    public List<Map<String, Object>> getDetails() {
        if (!errorTypeMap.isEmpty()) {
            return new ArrayList<>(errorTypeMap.values());
        }
        return new ArrayList<>(errorRowTreeMap.values());
    }


}
