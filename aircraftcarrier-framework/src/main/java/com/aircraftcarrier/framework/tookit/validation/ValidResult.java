package com.aircraftcarrier.framework.tookit.validation;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 校验结果类
 *
 * @author lzp
 */
@Getter
@Setter
public class ValidResult {

    /**
     * 错误信息
     */
    private final List<ErrorMessage> errorList;
    /**
     * 是否有错误
     */
    private boolean hasErrors;

    /**
     * ValidResult()
     */
    public ValidResult() {
        this.errorList = new ArrayList<>();
    }

    /**
     * hasErrors
     *
     * @return 是否存在错误信息
     */
    public boolean hasErrors() {
        return hasErrors;
    }

    /**
     * setHasErrors
     *
     * @param hasErrors 设置错误标识
     */
    public void setHasErrors(boolean hasErrors) {
        this.hasErrors = hasErrors;
    }

    /**
     * addError
     *
     * @param propertyName 属性名称
     * @param message      错误信息
     */
    public void addError(String propertyName, String message) {
        this.errorList.add(new ErrorMessage(propertyName, message));
    }


    /**
     * 获取所有验证信息
     *
     * @return 集合形式
     */
    public List<ErrorMessage> errorList() {
        return errorList;
    }

    /**
     * 获取所有验证信息
     *
     * @return 字符串形式
     */
    public String getErrors() {
        StringBuilder sb = new StringBuilder();
        for (Iterator<ErrorMessage> it = errorList.iterator(); it.hasNext(); ) {
            ErrorMessage errorMessage = it.next();
            sb.append(errorMessage.getPropertyPath()).append(":").append(errorMessage.getMessage());
            if (it.hasNext()) {
                sb.append("; ");
            }
        }
        return sb.toString();
    }
}