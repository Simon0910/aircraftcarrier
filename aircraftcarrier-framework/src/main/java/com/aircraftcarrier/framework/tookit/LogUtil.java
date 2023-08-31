package com.aircraftcarrier.framework.tookit;

import com.aircraftcarrier.framework.support.trace.TraceIdUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
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
    private static final Logger logger = LoggerFactory.getLogger(LogUtil.class);

    private static final ThreadLocal<Map<String, String>> THREAD_LOCAL = new ThreadLocal<>();
    private static final String TID = "tid";
    private static final String FIXED = "fixed";
    private static final String MODULE = "module";
    private static final String FULL_TID = "fullTid";
    private static final String NEWLINE = System.lineSeparator();
    private static final String LOG_CONNECTOR = " - ";
    private static final String NULL = "null";
    private static final String EMPTY = "";
    private static final String LEFT = "【";
    private static final String RIGHT = "】";
    private static final String LOG_PLACEHOLDER = "{}";
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\}");
    private static final String EMPTY_JSON_OBJECT = "{ }";

    private LogUtil() {
    }

    // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<LOG START<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    /**
     * <p>
     * 使用方式
     * <pre> {@code
     *
     *  LogUtil.info("3入参数：{}", LogUtil.getToJsonSupplier(orderInfo));
     *  或
     *  LogUtil.info("33入参数：{}", () -> JSON.toJSONString(orderInfo));
     *
     * }</pre>
     */
    public static Supplier<?> getToJsonSupplier(Object obj) {
        return () -> JSON.toJSONString(obj);
    }

    /**
     * <p>
     * 使用方式
     * <pre> {@code
     *
     *  LogUtil.info("3入参数：{}", LogUtil.getToJsonSupplier(orderInfo), LogUtil.getExceptionSupplier(e));
     *  或
     *  LogUtil.info("33入参数：{}", () -> JSON.toJSONString(orderInfo), () -> e);
     *
     * }</pre>
     */
    public static Supplier<?> getExceptionSupplier(Throwable throwable) {
        return () -> throwable;
    }

    public static void error(String message, Supplier<?>... args) {
        if (logger.isErrorEnabled()) {
            logger.error(formatLogMessage(getCallerStackTrace(), message), getLogArgs(args));
        }
    }

    public static void error(Logger logger, String message, Supplier<?>... args) {
        if (logger.isErrorEnabled()) {
            logger.error(formatLogMessage(getCallerStackTrace(), message), getLogArgs(args));
        }
    }

    public static void errorToJson(String message, Object... args) {
        if (logger.isErrorEnabled()) {
            logger.error(formatLogMessage(getCallerStackTrace(), message), getLogArgsToJson(args));
        }
    }

    public static void errorToJson(Logger logger, String message, Object... args) {
        if (logger.isErrorEnabled()) {
            logger.error(formatLogMessage(getCallerStackTrace(), message), getLogArgsToJson(args));
        }
    }

    public static void warn(String message, Supplier<?>... args) {
        if (logger.isWarnEnabled()) {
            logger.warn(formatLogMessage(getCallerStackTrace(), message), getLogArgs(args));
        }
    }

    public static void warn(Logger logger, String message, Supplier<?>... args) {
        if (logger.isWarnEnabled()) {
            logger.warn(formatLogMessage(getCallerStackTrace(), message), getLogArgs(args));
        }
    }

    public static void warnToJson(String message, Object... args) {
        if (logger.isWarnEnabled()) {
            logger.warn(formatLogMessage(getCallerStackTrace(), message), getLogArgsToJson(args));
        }
    }

    public static void warnToJson(Logger logger, String message, Object... args) {
        if (logger.isWarnEnabled()) {
            logger.warn(formatLogMessage(getCallerStackTrace(), message), getLogArgsToJson(args));
        }
    }

    public static void info(String message, Supplier<?>... args) {
        if (logger.isInfoEnabled()) {
            logger.info(formatLogMessage(getCallerStackTrace(), message), getLogArgs(args));
        }
    }

    public static void info(Logger logger, String message, Supplier<?>... args) {
        if (logger.isInfoEnabled()) {
            logger.info(formatLogMessage(getCallerStackTrace(), message), getLogArgs(args));
        }
    }

    public static void infoToJson(String message, Object... args) {
        if (logger.isInfoEnabled()) {
            logger.info(formatLogMessage(getCallerStackTrace(), message), getLogArgsToJson(args));
        }
    }

    public static void infoToJson(Logger logger, String message, Object... args) {
        if (logger.isInfoEnabled()) {
            logger.info(formatLogMessage(getCallerStackTrace(), message), getLogArgsToJson(args));
        }
    }

    public static void debug(String message, Supplier<?>... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(formatLogMessage(getCallerStackTrace(), message), getLogArgs(args));
        }
    }

    public static void debug(Logger logger, String message, Supplier<?>... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(formatLogMessage(getCallerStackTrace(), message), getLogArgs(args));
        }
    }

    public static void debugToJson(String message, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(formatLogMessage(getCallerStackTrace(), message), getLogArgsToJson(args));
        }
    }

    public static void debugToJson(Logger logger, String message, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(formatLogMessage(getCallerStackTrace(), message), getLogArgsToJson(args));
        }
    }

    public static void trace(String message, Supplier<?>... args) {
        if (logger.isTraceEnabled()) {
            logger.trace(formatLogMessage(getCallerStackTrace(), message), getLogArgs(args));
        }
    }

    public static void trace(Logger logger, String message, Supplier<?>... args) {
        if (logger.isTraceEnabled()) {
            logger.trace(formatLogMessage(getCallerStackTrace(), message), getLogArgs(args));
        }
    }

    public static void traceToJson(String message, Object... args) {
        if (logger.isTraceEnabled()) {
            logger.trace(formatLogMessage(getCallerStackTrace(), message), getLogArgsToJson(args));
        }
    }

    public static void traceToJson(Logger logger, String message, Object... args) {
        if (logger.isTraceEnabled()) {
            logger.trace(formatLogMessage(getCallerStackTrace(), message), getLogArgsToJson(args));
        }
    }

    private static Object[] getLogArgs(Supplier<?>... args) {
        if (args == null || args.length == 0) {
            return new Object[]{};
        } else {
            return Arrays.stream(args).map(Supplier::get).toArray();
        }
    }

    private static Object[] getLogArgsToJson(Object... args) {
        if (args == null || args.length == 0) {
            return new Object[]{};
        } else {
            Object[] jsonArgs = new Object[args.length];
            for (int i = 0, len = args.length; i < len; i++) {
                if (args[i] instanceof Throwable) {
                    jsonArgs[i] = args[i];
                    continue;
                }
                jsonArgs[i] = JSON.toJSONString(args[i]);
            }
            return jsonArgs;
        }
    }

    private static StackTraceElement getCallerStackTrace() {
        return Thread.currentThread().getStackTrace()[3];
    }

    private static String formatLogMessage(StackTraceElement caller, String message) {
        return "(" + caller.getFileName() + ":" + caller.getLineNumber() + ")." + caller.getMethodName() + "() " +
                LogUtil.getFullTid() +
                message;
    }

    // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<LOG END<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<


    public static String uuid() {
        return TraceIdUtil.uuid();
    }

    private static String fixString(String str) {
        return str == null ? EMPTY : str;
    }

    private static String getReplaceFirst(String inString, String oldPattern, String newPattern) {
        return PLACEHOLDER_PATTERN.matcher(inString).replaceFirst(newPattern);
    }

    private static String getReplaceAll(String inString, String oldPattern, String newPattern) {
        return PLACEHOLDER_PATTERN.matcher(inString).replaceAll(newPattern);
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
            context.put(TID, EMPTY);
            // orderNo etc.
            context.put(FIXED, EMPTY);
            // 模块名称
            context.put(MODULE, EMPTY);
            // tid orderNo 模块名称
            context.put(FULL_TID, EMPTY);
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
        builder.append(LOG_CONNECTOR);
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

    public static String getErrorLog(String log, String... args) {
        if (logger.isErrorEnabled()) {
            return getLog(log, args);
        }
        return EMPTY;
    }

    public static String getErrorLogJson(String log, Object... args) {
        if (logger.isErrorEnabled()) {
            return getLogAutoJsonString(log, args);
        }
        return EMPTY;
    }

    public static String getWarnLog(String log, String... args) {
        if (logger.isWarnEnabled()) {
            return getLog(log, args);
        }
        return EMPTY;
    }

    public static String getWarnLogJson(String log, Object... args) {
        if (logger.isWarnEnabled()) {
            return getLogAutoJsonString(log, args);
        }
        return EMPTY;
    }

    public static String getInfoLog(String log, String... args) {
        if (logger.isInfoEnabled()) {
            return getLog(log, args);
        }
        return EMPTY;
    }

    public static String getInfoLogJson(String log, Object... args) {
        if (logger.isInfoEnabled()) {
            return getLogAutoJsonString(log, args);
        }
        return EMPTY;
    }

    public static String getDebugLog(String log, String... args) {
        if (logger.isDebugEnabled()) {
            return getLog(log, args);
        }
        return EMPTY;
    }


    /**
     * 获取   tid 固定前缀 模块标识 - 用户日志
     *
     * @param log  例如:  接单入参orderInfo：{} {}
     * @param args 例如: LogUtil.toJsonString(orderInfo)
     * @return String 例如: 接单入参orderInfo：{"id":123,"name":"xx"}
     */
    private static String getLog(String log, String... args) {
        Map<String, String> context = getContextIfPresent();

        if (!StringUtils.hasText(log)) {
            return context.get(FULL_TID) + log;
        }
        if (!log.contains(LOG_PLACEHOLDER)) {
            FormattingTuple formattingTuple = MessageFormatter.arrayFormat(context.get(FULL_TID) + log, args);
            if (formattingTuple.getThrowable() != null) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                formattingTuple.getThrowable().printStackTrace(new PrintStream(bos));
                return formattingTuple.getMessage() + NEWLINE + bos;
            }
            return formattingTuple.getMessage();
        }
        if (args == null) {
            return context.get(FULL_TID) + getReplaceFirst(log, LOG_PLACEHOLDER, NULL);
        }
        if (args.length < 1) {
            return context.get(FULL_TID) + log;
        }

        // 空对象也是一个{}, 防止被外层log.info解析 {} 转换成 { }
        for (int i = 0; i < args.length; i++) {
            String argString = args[i];
            if (argString == null) {
                args[i] = null;
                continue;
            }
            if (LOG_PLACEHOLDER.equals(argString)) {
                args[i] = EMPTY_JSON_OBJECT;
            } else {
                if (argString.contains(LOG_PLACEHOLDER)) {
                    args[i] = getReplaceAll(argString, LOG_PLACEHOLDER, EMPTY_JSON_OBJECT);
                }
            }
        }

        FormattingTuple formattingTuple = MessageFormatter.arrayFormat(context.get(FULL_TID) + log, args);

        if (formattingTuple.getThrowable() != null) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            formattingTuple.getThrowable().printStackTrace(new PrintStream(bos));
            return formattingTuple.getMessage() + NEWLINE + bos;
        }

        return formattingTuple.getMessage();
    }

    public static String getDebugLogJson(String log, Object... args) {
        if (logger.isDebugEnabled()) {
            return getLogAutoJsonString(log, args);
        }
        return EMPTY;
    }

    /**
     * 获取   tid 固定前缀 模块标识 - 用户日志
     *
     * @param log  例如:  接单入参orderInfo：{} {}
     * @param args 例如: orderInfo
     * @return String 例如: 接单入参orderInfo：{"id":123,"name":"xx"}
     */
    private static String getLogAutoJsonString(String log, Object... args) {
        Map<String, String> context = getContextIfPresent();

        if (!StringUtils.hasText(log)) {
            return context.get(FULL_TID) + log;
        }
        if (!log.contains(LOG_PLACEHOLDER)) {
            FormattingTuple formattingTuple = MessageFormatter.arrayFormat(context.get(FULL_TID) + log, args);
            if (formattingTuple.getThrowable() != null) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                formattingTuple.getThrowable().printStackTrace(new PrintStream(bos));
                return formattingTuple.getMessage() + NEWLINE + bos;
            }
            return formattingTuple.getMessage();
        }
        if (args == null) {
            return context.get(FULL_TID) + getReplaceFirst(log, LOG_PLACEHOLDER, NULL);
        }
        if (args.length < 1) {
            return context.get(FULL_TID) + log;
        }

        // 空对象也是一个{}, 防止被外层log.info解析 {} 转换成 { }
        for (int i = 0; i < args.length; i++) {
            Object argObj = args[i];
            if (argObj instanceof Throwable) {
                continue;
            }
            if (argObj instanceof String) {
                if (LOG_PLACEHOLDER.equals(argObj)) {
                    args[i] = EMPTY_JSON_OBJECT;
                } else {
                    String argString = (String) argObj;
                    if (argString.contains(LOG_PLACEHOLDER)) {
                        args[i] = getReplaceAll(argString, LOG_PLACEHOLDER, EMPTY_JSON_OBJECT);
                    }
                }
            } else {
                if (argObj == null) {
                    args[i] = null;
                    continue;
                }
                String argJson = toJsonString(argObj);
                if (LOG_PLACEHOLDER.equals(argJson)) {
                    args[i] = EMPTY_JSON_OBJECT;
                } else if (argJson.contains(LOG_PLACEHOLDER)) {
                    args[i] = getReplaceAll(argJson, LOG_PLACEHOLDER, EMPTY_JSON_OBJECT);
                }
            }
        }

        FormattingTuple formattingTuple = MessageFormatter.arrayFormat(context.get(FULL_TID) + log, args);

        if (formattingTuple.getThrowable() != null) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            formattingTuple.getThrowable().printStackTrace(new PrintStream(bos));
            return formattingTuple.getMessage() + NEWLINE + bos;
        }

        return formattingTuple.getMessage();
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
     * 获取 tid （作用于接口入参）
     *
     * @return tid
     */
    public static long getTidLong() {
        try {
            return Long.parseLong(getContextIfPresent().get(TID));
        } catch (Exception e) {
            long l = System.nanoTime();
            log.info(getInfoLog("{} tidString==>tidLong {}"), getContextIfPresent().get(FULL_TID), l);
            return l;
        }
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
    public static String toJsonString(Object obj) {
        return JSON.toJSONString(obj);
    }

    public static String toJsonStringError(Object obj) {
        if (logger.isErrorEnabled()) {
            return JSON.toJSONString(obj);
        }
        return EMPTY;
    }

    public static String toJsonStringWarn(Object obj) {
        if (logger.isErrorEnabled()) {
            return JSON.toJSONString(obj);
        }
        return EMPTY;
    }

    public static String toJsonStringInfo(Object obj) {
        if (logger.isInfoEnabled()) {
            return JSON.toJSONString(obj);
        }
        return EMPTY;
    }

    public static String toJsonStringDebug(Object obj) {
        if (logger.isDebugEnabled()) {
            return JSON.toJSONString(obj);
        }
        return EMPTY;
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