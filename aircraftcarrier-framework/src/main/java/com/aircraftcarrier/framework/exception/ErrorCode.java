package com.aircraftcarrier.framework.exception;

/**
 * 错误编码，由5位数字组成，前2位为模块编码，后3位为业务编码
 * <p>
 * 如：10001（10代表系统模块，001代表业务代码）
 * </p>
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0
 */
public final class ErrorCode {
    public static final String UNAUTHORIZED = "401";
    public static final String FORBIDDEN = "403";
    public static final String INTERNAL_SERVER_ERROR = "500";
    public static final String SYS = "500";
    public static final String BIZ = "10000";
    public static final String NOT_NULL = "10001";
    public static final String DB_RECORD_EXISTS = "10002";
    public static final String PARAMS_GET_ERROR = "10003";
    public static final String ACCOUNT_PASSWORD_ERROR = "10004";
    public static final String ACCOUNT_DISABLE = "10005";
    public static final String LONG_FAIL = "1000501";
    public static final String IDENTIFIER_NOT_NULL = "10006";
    public static final String CAPTCHA_ERROR = "10007";
    public static final String SUB_MENU_EXIST = "10008";
    public static final String PASSWORD_ERROR = "10009";
    public static final String ACCOUNT_NOT_EXIST = "10010";
    public static final String SUPERIOR_DEPT_ERROR = "10011";
    public static final String SUPERIOR_MENU_ERROR = "10012";
    public static final String DATA_SCOPE_PARAMS_ERROR = "10013";
    public static final String DEPT_SUB_DELETE_ERROR = "10014";
    public static final String DEPT_USER_DELETE_ERROR = "10015";
    public static final String ACT_DEPLOY_ERROR = "10016";
    public static final String ACT_MODEL_IMG_ERROR = "10017";
    public static final String ACT_MODEL_EXPORT_ERROR = "10018";
    public static final String UPLOAD_FILE_EMPTY = "10019";
    public static final String TOKEN_NOT_EMPTY = "10020";
    public static final String TOKEN_INVALID = "10021";
    public static final String ACCOUNT_LOCK = "10022";
    public static final String ACT_DEPLOY_FORMAT_ERROR = "10023";
    public static final String OSS_UPLOAD_FILE_ERROR = "10024";
    public static final String SEND_SMS_ERROR = "10025";
    public static final String MAIL_TEMPLATE_NOT_EXISTS = "10026";
    public static final String REDIS_ERROR = "10027";
    public static final String JOB_ERROR = "10028";
    public static final String INVALID_SYMBOL = "10029";
    public static final String JSON_FORMAT_ERROR = "10030";
    public static final String SMS_CONFIG = "10031";
    public static final String SUPERIOR_REGION_ERROR = "10032";
    public static final String REGION_SUB_DELETE_ERROR = "10033";

    private ErrorCode() {
    }
}
