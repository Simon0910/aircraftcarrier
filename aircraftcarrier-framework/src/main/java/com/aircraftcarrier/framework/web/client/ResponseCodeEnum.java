package com.aircraftcarrier.framework.web.client;

/**
 * @author liuzhipeng
 * @since 2020-01-15 10:13
 */
public enum ResponseCodeEnum {
    /**
     * SUCCESS
     */
    SUCCESS("SUCCESS", "SUCCESS"),

    /**
     * ERROR
     */
    ERROR("ERROR", "ERROR"),

    /**
     * FAIL
     */
    FAIL("FAIL", "FAIL"),
    ;

    /**
     * code
     */
    String code;
    /**
     * desc
     */
    String desc;

    /**
     * ResponseCodeEnum
     *
     * @param code code
     * @param desc desc
     */
    ResponseCodeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * getCode
     *
     * @return String
     */
    public String getCode() {
        return code;
    }

    /**
     * getDesc
     *
     * @return String
     */
    public String getDesc() {
        return desc;
    }
}