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
    public static final int SYS = 500;
    public static final int BIZ = 500;
    public static final int INTERNAL_SERVER_ERROR = 500;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_NULL = 10001;
    public static final int DB_RECORD_EXISTS = 10002;
    public static final int PARAMS_GET_ERROR = 10003;
    public static final int ACCOUNT_PASSWORD_ERROR = 10004;
    public static final int ACCOUNT_DISABLE = 10005;
    public static final int LONG_FAIL = 1000501;
    public static final int IDENTIFIER_NOT_NULL = 10006;
    public static final int CAPTCHA_ERROR = 10007;
    public static final int SUB_MENU_EXIST = 10008;
    public static final int PASSWORD_ERROR = 10009;
    public static final int ACCOUNT_NOT_EXIST = 10010;
    public static final int SUPERIOR_DEPT_ERROR = 10011;
    public static final int SUPERIOR_MENU_ERROR = 10012;
    public static final int DATA_SCOPE_PARAMS_ERROR = 10013;
    public static final int DEPT_SUB_DELETE_ERROR = 10014;
    public static final int DEPT_USER_DELETE_ERROR = 10015;
    public static final int ACT_DEPLOY_ERROR = 10016;
    public static final int ACT_MODEL_IMG_ERROR = 10017;
    public static final int ACT_MODEL_EXPORT_ERROR = 10018;
    public static final int UPLOAD_FILE_EMPTY = 10019;
    public static final int TOKEN_NOT_EMPTY = 10020;
    public static final int TOKEN_INVALID = 10021;
    public static final int ACCOUNT_LOCK = 10022;
    public static final int ACT_DEPLOY_FORMAT_ERROR = 10023;
    public static final int OSS_UPLOAD_FILE_ERROR = 10024;
    public static final int SEND_SMS_ERROR = 10025;
    public static final int MAIL_TEMPLATE_NOT_EXISTS = 10026;
    public static final int REDIS_ERROR = 10027;
    public static final int JOB_ERROR = 10028;
    public static final int INVALID_SYMBOL = 10029;
    public static final int JSON_FORMAT_ERROR = 10030;
    public static final int SMS_CONFIG = 10031;
    public static final int SUPERIOR_REGION_ERROR = 10032;
    public static final int REGION_SUB_DELETE_ERROR = 10033;

    private ErrorCode() {
    }
}
