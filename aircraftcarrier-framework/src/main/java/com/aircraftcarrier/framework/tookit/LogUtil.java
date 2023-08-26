package com.aircraftcarrier.framework.tookit;

import com.aircraftcarrier.framework.support.trace.TraceIdUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * LogUtil
 *
 * @author zhipengliu
 * @date 2023/8/20
 * @since 1.0
 */
@Slf4j
public class LogUtil {
    private static final ThreadLocal<Map<String, String>> THREAD_LOCAL = new ThreadLocal<>();
    private static final String TID = "tid";
    private static final String FIXED = "fixed";
    private static final String MODULE = "module";
    private static final String FULL_TID = "fullTid";
    private static final String LOG_CONNECTOR = "%s - %s";

    private static final String LOG_EX_CONNECTOR = "%s - %s\n%s";
    private static final String LOG_EX_CONNECTOR2 = "%s\n%s";

    private static final String EMPTY = "";
    private static final String LEFT = "【";
    private static final String RIGHT = "】";
    private static final String LOG_PLACEHOLDER = "{}";
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\}");
    private static final String EMPTY_JSON_OBJECT = "{ }";

    private LogUtil() {
    }

    public static String uuid() {
        return TraceIdUtil.uuid();
    }

    private static String fixString(String str) {
        return str == null ? EMPTY : str;
    }

    private static String getReplacedFirst(String log) {
        return PLACEHOLDER_PATTERN.matcher(log).replaceFirst("null");
    }

    /**
     * get
     */
    private static Map<String, String> getContextIfPresent() {
        Map<String, String> context = THREAD_LOCAL.get();
        if (context == null) {
            context = new HashMap<>(6);
            // 模块名称
            // tid
            context.put(TID, "0");
            // orderNo etc.
            context.put(FIXED, EMPTY);
            // 模块名称
            context.put(MODULE, EMPTY);
            // tid orderNo 模块名称
            context.put(FULL_TID, "0");
        }
        return context;
    }

    /**
     * set
     */
    private static void setContext(Map<String, String> context) {
        THREAD_LOCAL.set(context);
    }

    /**
     * removeContext
     */
    private static void removeContext() {
        THREAD_LOCAL.remove();
    }

    /**
     * setFixedName
     */
    public static void setTraceFixedName(String fixedName) {
        TraceIdUtil.setFixedName(fixedName);
    }

    /**
     * setModuleName
     */
    public static void setTraceModuleName(String moduleName) {
        TraceIdUtil.setModuleName(moduleName);
    }

    /**
     * getRootTraceId
     */
    public static String getTraceId() {
        return TraceIdUtil.getTraceIdOrUuid();
    }

    /**
     * getTraceIdLong
     */
    public static String getTraceIdLong() {
        return getTraceId();
    }

    /**
     * 请求一个标识
     *
     * <p>
     * 使用方式
     * <pre> {@code
     *  LogUtil.requestStart("订单号", "模块1");
     *  try {
     *      log.info(LogUtil.getLog("入参: 【{}】", LogUtil.toJsonString(orderInfo)));
     *      log.info(LogUtil.getLog("出参: 【{}】", "orderNo"));
     *      LogUtil.resetModule("模块2");
     *      log.info(LogUtil.getLog("入参: 【{}】", LogUtil.toJsonString(orderInfo)));
     *      log.info(LogUtil.getLog("出参: 【{}】", "orderNo"));
     *  } finally {
     *      LogUtil.requestEnd();
     *  }
     *
     * }</pre>
     *
     * @param fixed 请求标识
     */
    public static void requestStart(String fixed) {
        requestStartByTid(uuid(), fixed, EMPTY);
    }

    public static void requestStart(String fixed, String module) {
        requestStartByTid(uuid(), fixed, module);
    }

    public static void requestStartByTid(String tid) {
        requestStartByTid(tid, EMPTY, EMPTY);
    }

    public static void requestStartByTid(String tid, String fixed) {
        requestStartByTid(tid, fixed, EMPTY);
    }

    public static void requestStartByTid(String tid, String fixed, String module) {
        // get
        Map<String, String> context = getContextIfPresent();

        // tid
        context.put(TID, tid);
        // orderNo etc.
        context.put(FIXED, fixString(fixed));
        // 模块名称.
        context.put(MODULE, fixString(module));
        // tid orderNo 模块名称
        concatContext(context);

        // set
        setContext(context);
    }

    /**
     * {tid} {fixed} {module}
     */
    private static void concatContext(Map<String, String> context) {
        StringBuilder builder = new StringBuilder();
        builder.append(context.get(TID));
        if (!context.get(FIXED).isEmpty()) {
            builder.append(LEFT).append(context.get(FIXED)).append(RIGHT);
        }
        if (!context.get(MODULE).isEmpty()) {
            builder.append(LEFT).append(context.get(MODULE)).append(RIGHT);
        }
        context.put(FULL_TID, builder.toString());
    }

    /**
     * 重置 fixed
     *
     * @param fixed fixed
     */
    public static void resetFixed(String fixed) {
        Map<String, String> context = getContextIfPresent();
        context.put(FIXED, fixString(fixed));
        concatContext(context);
    }

    /**
     * 重置 module
     *
     * @param module module
     */
    public static void resetModule(String module) {
        Map<String, String> context = getContextIfPresent();
        context.put(MODULE, fixString(module));
        concatContext(context);
    }


    /**
     * 获取   tid 固定前缀 模块标识 - 用户日志
     *
     * @param log  例如:  接单入参orderInfo：{} {}
     * @param args 例如: LogUtil.toJsonString(orderInfo)
     * @return String 例如: 接单入参orderInfo：{"id":123,"name":"xx"}
     */
    public static String getLog(String log, String... args) {
        return getLogAutoJson(log, (Object[]) args);
    }


    /**
     * 获取   tid 固定前缀 模块标识 - 用户日志
     *
     * @param log  例如:  接单入参orderInfo：{} {}
     * @param args 例如: LogUtil.toJsonString(orderInfo)
     * @return String 例如: 接单入参orderInfo：{"id":123,"name":"xx"}
     */
    public static String getLogAutoJson(String log, Object... args) {
        Map<String, String> context = getContextIfPresent();

        if (StringUtils.isEmpty(log)) {
            return String.format(LOG_CONNECTOR, context.get(FULL_TID), EMPTY);
        }
        if (!log.contains(LOG_PLACEHOLDER)) {
            if (args != null && args.length > 0 && args[args.length - 1] instanceof Throwable) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ((Throwable) args[args.length - 1]).printStackTrace(new PrintStream(bos));
                args[args.length - 1] = bos;
                return String.format(LOG_EX_CONNECTOR, context.get(FULL_TID), log, args[args.length - 1]);
            }
            return String.format(LOG_CONNECTOR, context.get(FULL_TID), log);
        }
        if (args == null) {
            return String.format(LOG_CONNECTOR, context.get(FULL_TID), getReplacedFirst(log));
        }
        if (args.length < 1) {
            return String.format(LOG_CONNECTOR, context.get(FULL_TID), log);
        }

        // 空对象也是一个{}, 防止被外层log.info解析 {} 转换成 { }
        Throwable throwable = null;
        for (int i = 0; i < args.length; i++) {
            Object argObj = args[i];
            if (argObj instanceof Throwable) {
                if (i != args.length - 1) {
                    args[i] = args[i].toString();
                } else {
                    throwable = ((Throwable) args[i]);
                    args[i] = EMPTY_JSON_OBJECT;
                }
                continue;
            }
            if (argObj instanceof String) {
                if (LOG_PLACEHOLDER.equals(argObj)) {
                    args[i] = EMPTY_JSON_OBJECT;
                } else {
                    String argString = (String) argObj;
                    if (argString.contains(LOG_PLACEHOLDER)) {
                        args[i] = StringUtils.replace(argString, LOG_PLACEHOLDER, EMPTY_JSON_OBJECT);
                    }
                }
            } else {
                String argJson = toJsonString(argObj);
                if (argJson.contains(LOG_PLACEHOLDER)) {
                    args[i] = StringUtils.replace(argJson, LOG_PLACEHOLDER, EMPTY_JSON_OBJECT);
                }
            }
        }

        if (throwable != null) {
            FormattingTuple formattingTuple = MessageFormatter.arrayFormat(String.format(LOG_CONNECTOR, context.get(FULL_TID), log), args, throwable);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            formattingTuple.getThrowable().printStackTrace(new PrintStream(bos));
            return String.format(LOG_EX_CONNECTOR2, formattingTuple.getMessage(), bos);
        }
        return MessageFormatter.arrayFormat(String.format(LOG_CONNECTOR, context.get(FULL_TID), log), args).getMessage();
    }


    /**
     * 获取 tid （作用于接口入参）
     *
     * @return tid
     */
    public static String getTid() {
        return getContextIfPresent().get(TID);
    }


    /**
     * 获取 丰富的tid
     *
     * @return fullTid : {tid} {fixed} {module}
     */
    public static String getFullTid() {
        return getContextIfPresent().get(FULL_TID);
    }


    /**
     * @see JSON#toJSONString(Object)
     */
    public static String toJsonString(Object o) {
        return JSON.toJSONString(o);
    }

    /**
     * remove
     */
    public static void requestEnd() {
        Map<String, String> context = THREAD_LOCAL.get();
        if (context != null) {
            context.clear();
        }
        // removeContext
        removeContext();
    }

}