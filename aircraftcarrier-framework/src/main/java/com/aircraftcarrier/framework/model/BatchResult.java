package com.aircraftcarrier.framework.model;


import com.aircraftcarrier.framework.tookit.MapUtil;
import com.aircraftcarrier.framework.tookit.StringUtils;

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
     * 详细信息错误码
     */
    public static final String DETAIL_ERROR = "DETAIL_ERROR";
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;
    /**
     * key: 行号
     */
    private static final String ROW_NO = "rowNo";

    /**
     * msg: 错误信息
     */
    private static final String MSG = "msg";
    /**
     * 批量操作
     * 错误明细
     */
    private Map<String, Map<String, Object>> errorTipMap;
    /**
     * 批量上传
     * 错误明细
     */
    private final TreeMap<Integer, Map<String, Object>> errorRowTreeMap = new TreeMap<>();
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
        this.errorTipMap = MapUtil.newHashMap(errorUpLimit);
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
     * 追加成功数
     */
    public void increaseSuccess(int successSum) {
        this.successCount += successSum;
    }

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
     * @param tip      tip
     * @param errorMsg errorMsg
     */
    public void addErrorMsg(String tip, Long id, String errorCode, String errorMsg) {
        if (errorCount > errorUpLimit) {
            return;
        }

        Map<String, Object> errorTip = new HashMap<>(6);
        errorTip.put("tip", tip);
        errorTip.put("id", id);
        errorTip.put("errorCode", errorCode);
        errorTip.put(MSG, errorMsg);
        putErrorMsgMap(tip, errorTip);
    }

    /**
     * 添加错误明细
     *
     * @param tip      tip
     * @param errorMsg errorMsg
     */
    public void addErrorMsg(String tip, String errorMsg) {
        if (errorCount > errorUpLimit) {
            return;
        }
        Map<String, Object> errorTip = new HashMap<>(3);
        errorTip.put("tip", tip);
        errorTip.put(MSG, errorMsg);
        putErrorMsgMap(tip, errorTip);
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
        Map<String, Object> uploadFileErrorMsgTip = new HashMap<>(2);
        uploadFileErrorMsgTip.put(MSG, errorMsg);
        putErrorMsgMap(null, uploadFileErrorMsgTip);
    }

    /**
     * 根据tip 添加错误信息
     *
     * @param tip      tip
     * @param errorTip errorTip
     */
    private void putErrorMsgMap(String tip, Map<String, Object> errorTip) {
        if (StringUtils.isBlank(tip)) {
            errorTipMap.put(String.valueOf(System.nanoTime()), errorTip);
        } else {
            Map<String, Object> originErrorTip = errorTipMap.get(tip);
            if (originErrorTip != null) {
                originErrorTip.put(MSG, originErrorTip.get(MSG) + ", " + errorTip.get(MSG));
            } else {
                errorTipMap.put(tip, errorTip);
                errorCount++;
            }
        }
    }

    public boolean hasErrorByRowNo(Integer rowNo) {
        return errorRowTreeMap.get(rowNo) != null;
    }

    public Integer lastErrorRowNo() {
        return lastErrorRowNo;
    }

    public boolean hasErrorByLastErrorRowNo(Integer lastErrorRowNo) {
        return this.lastErrorRowNo.equals(lastErrorRowNo);
    }


    /**
     * 总条数展示
     *
     * @return java.lang.Integer
     */
    public Integer getTotal() {
        return successCount + errorCount;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public Integer getErrorCount() {
        return errorCount;
    }

    /**
     * 错误明细
     */
    public List<Map<String, Object>> getDetails() {
        if (!errorTipMap.isEmpty()) {
            return new ArrayList<>(errorTipMap.values());
        }
        return new ArrayList<>(errorRowTreeMap.values());
    }


}
