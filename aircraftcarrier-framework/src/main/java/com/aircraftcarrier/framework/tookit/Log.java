package com.aircraftcarrier.framework.tookit;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Log
 * 主要实现功能为
 * <p>
 * <p>1. 根据日志级别水平打印日志
 * <p>{@link #error(String, Throwable)}
 * <p>{@link #error(String, Supplier[])}
 * <p>{@link #info(String, Supplier[])}
 * <p>{@link #getToJsonSupplier(Object)}
 * <p>{@link #getSupplier(Object)}
 * <p>{@link #getExceptionSupplier(Throwable)}
 * <p>{@link #toErrorJsonString(Object)}
 * <p>{@link #toInfoJsonString(Object)}
 * <p>
 * <p>2. 自动携带追踪信息
 * <p>{@link #start(String)}
 * <p>{@link #setModule(String)}
 * <p>{@link #end()}
 * <p>
 * <p>3. 自动toJsonString
 * <p>{@link #infoToJson(String, Object...)}
 * <p>{@link #errorToJson(String, Object...)}
 * <p>
 * <p>示例
 * <pre> {@code
 *  // 调整日志级别 观察是否打印
 *  log.error("error...{} {}", 11, Log.toErrorJsonString(orderInfo));
 *  log.warn("warn...{} {}", 11, Log.toWarnJsonString(orderInfo));
 *  log.info("info...{} {}", 11, Log.toInfoJsonString(orderInfo));
 *  log.debug("debug...{} {}", 11, Log.toDebugJsonString(orderInfo));
 *
 *  Log.error(log, "error2...{} {}", Log.getSupplier(22), Log.getToJsonSupplier(orderInfo));
 *  Log.warn(log, "warn2...{} {}", Log.getSupplier(22), Log.getToJsonSupplier(orderInfo));
 *  Log.info(log, "info2...{} {}", Log.getSupplier(22), Log.getToJsonSupplier(orderInfo));
 *  Log.debug(log, "debug2...{} {}", Log.getSupplier(22), Log.getToJsonSupplier(orderInfo));
 *
 *  Log.errorToJson(log, "error3...{} {}", 33, orderInfo);
 *  Log.warnToJson(log, "warn3...{} {}", 33, orderInfo);
 *  Log.infoToJson(log, "info3...{} {}", 33, orderInfo);
 *  Log.debugToJson(log, "debug3...{} {}",33, orderInfo);
 *
 * }</pre>
 * <p>
 * <p>使用方式
 * <pre> {@code
 *  Log.start("订单号", "模块1");
 *  try {
 *      // .....
 *      Log.info("入参：{}", Log.getToJsonSupplier(orderInfo));
 *      Log.info("入参：{} {}", getSupplier(11), Log.getToJsonSupplier(orderInfo));
 *      // (TestController.java:89).method() 1273227570368791【订单号】【模块1】 - 入参：{"orderNo":"123","id":"1","orderInfoDetail":{}}
 *
 *      // 模块2
 *      Log.setModule("模块2");
 *      Log.infoToJson("入参：{}", orderInfo);
 *      Log.infoToJson("入参：{} {}", 11, orderInfo);
 *      // (TestController.java:93).method() 1273227570368791【订单号】【模块2】 - 入参：{"orderNo":"123","id":"1","orderInfoDetail":{}}
 *
 *      // 模块n
 *      Log.setModule("模块n");
 *      Log.infoToJson("入参：{}", orderInfo);
 *      Log.infoToJson("入参：{} {}", 11, orderInfo);
 *      // (TestController.java:97).method() 1273227570368791【订单号】【模块n】 - 入参：{"orderNo":"123","id":"1","orderInfoDetail":{}}
 *      // .....
 *  }  catch (Exception e) {
 *      Log.error("接口异常1", e);
 *      Log.error("接口异常2 {}, {}", Log.getToJsonSupplier(orderInfo), getSupplier(11), getExceptionSupplier(e));
 *      Log.errorToJson("接口异常4 {}, {}", orderInfo, 11, e);
 *
 *      // (TestController.java:107).method() 1273227570368791【订单号】【模块n】 - 接口异常4 {"orderNo":"123","id":"1","orderInfoDetail":{}}, 11
 *      // java.lang.RuntimeException: 错误了！
 * 	    //        at com.xx.xxx.TestController.method(TestController.java:101)
 * 	    //        ...
 *  } finally {
 *      Log.end();
 *  }
 *
 * }</pre>
 * <p>
 *
 * @author zhipengliu
 * @since 1.0
 */
public class Log {
    private static final Logger logger = LoggerFactory.getLogger(Log.class);

    private static final ThreadLocal<Map<String, String>> THREAD_LOCAL = new ThreadLocal<>();
    private static final String TID_KEY = "tid";
    private static final String FIXED_0_KEY = "fixed0";
    private static final String FIXED_PREV_KEY = "fixedPrev";
    private static final String FIXED_KEY = "fixed";
    private static final String MODULE_0_KEY = "module0";
    private static final String MODULE_PREV_KEY = "modulePrev";
    private static final String MODULE_KEY = "module";
    private static final String FULL_TID_KEY = "fullTid";
    private static final String NEWLINE = System.lineSeparator();
    private static final String LOG_CONNECTOR = " - ";
    private static final String NULL = "null";
    private static final String EMPTY = "";
    private static final String LEFT = "【";
    private static final String RIGHT = "】";
    private static final String LOG_PLACEHOLDER = "{}";
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\}");
    private static final String EMPTY_JSON_OBJECT = "{ }";

    private Log() {
    }

    // *****************************************************************************************************************
    //                                              LOG START
    // *****************************************************************************************************************

    /**
     * <p>
     * <p>使用方式
     * <pre> {@code
     *
     *  LogUtil.info("3入参数：{}", LogUtil.getToJsonSupplier(orderInfo));
     *  或
     *  LogUtil.info("33入参数：{}", () -> JSON.toJSONString(orderInfo));
     *
     * }</pre>
     */
    public static Supplier<String> getToJsonSupplier(Object obj) {
        return () -> JSON.toJSONString(obj);
    }

    /**
     * getSupplier
     */
    public static Supplier<Object> getSupplier(Object obj) {
        return () -> obj;
    }


    /**
     * <p>
     * <p>使用方式
     * <pre> {@code
     *
     *  LogUtil.info("3入参数：{}", LogUtil.getToJsonSupplier(orderInfo), LogUtil.getExceptionSupplier(e));
     *  或
     *  LogUtil.info("33入参数：{}", () -> JSON.toJSONString(orderInfo), () -> e);
     *
     * }</pre>
     */
    public static Supplier<Throwable> getExceptionSupplier(Throwable throwable) {
        return () -> throwable;
    }

    /**
     * <p>
     * 使用方式
     * <pre> {@code
     *
     *  Log.error("接口异常1", e);
     *
     *  Log.error("接口异常2 {}, {}", Log.getToJsonSupplier(orderInfo), () -> 11, () -> e);
     *
     *  Log.error("接口异常3 {}, {}", () -> Log.toJsonString(orderInfo), () -> 11, () -> e);
     *
     *  Log.errorToJson("接口异常4 {}, {}", orderInfo, 11, e);
     *
     * }</pre>
     */
    public static void error(String message, Throwable t) {
        if (logger.isErrorEnabled()) {
            logger.error(wrapMessage(message), t);
        }
    }

    public static void error(String message, Supplier<?>... args) {
        if (logger.isErrorEnabled()) {
            logger.error(wrapMessage(message), getLogArgs(args));
        }
    }

    public static void error(Logger logger, String message, Supplier<?>... args) {
        if (logger.isErrorEnabled()) {
            logger.error(wrapMessage(message), getLogArgs(args));
        }
    }

    public static void errorToJson(String message, Object... args) {
        if (logger.isErrorEnabled()) {
            logger.error(wrapMessage(message), getLogArgsToJson(args));
        }
    }

    public static void errorToJson(Logger logger, String message, Object... args) {
        if (logger.isErrorEnabled()) {
            logger.error(wrapMessage(message), getLogArgsToJson(args));
        }
    }

    public static void warn(String message, Supplier<?>... args) {
        if (logger.isWarnEnabled()) {
            logger.warn(wrapMessage(message), getLogArgs(args));
        }
    }

    public static void warn(Logger logger, String message, Supplier<?>... args) {
        if (logger.isWarnEnabled()) {
            logger.warn(wrapMessage(message), getLogArgs(args));
        }
    }

    public static void warnToJson(String message, Object... args) {
        if (logger.isWarnEnabled()) {
            logger.warn(wrapMessage(message), getLogArgsToJson(args));
        }
    }

    public static void warnToJson(Logger logger, String message, Object... args) {
        if (logger.isWarnEnabled()) {
            logger.warn(wrapMessage(message), getLogArgsToJson(args));
        }
    }

    public static void info(String message, Supplier<?>... args) {
        if (logger.isInfoEnabled()) {
            logger.info(wrapMessage(message), getLogArgs(args));
        }
    }

    public static void info(Logger logger, String message, Supplier<?>... args) {
        if (logger.isInfoEnabled()) {
            logger.info(wrapMessage(message), getLogArgs(args));
        }
    }

    public static void infoToJson(String message, Object... args) {
        if (logger.isInfoEnabled()) {
            logger.info(wrapMessage(message), getLogArgsToJson(args));
        }
    }

    public static void infoToJson(Logger logger, String message, Object... args) {
        if (logger.isInfoEnabled()) {
            logger.info(wrapMessage(message), getLogArgsToJson(args));
        }
    }

    public static void debug(String message, Supplier<?>... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(wrapMessage(message), getLogArgs(args));
        }
    }

    public static void debug(Logger logger, String message, Supplier<?>... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(wrapMessage(message), getLogArgs(args));
        }
    }

    public static void debugToJson(String message, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(wrapMessage(message), getLogArgsToJson(args));
        }
    }

    public static void debugToJson(Logger logger, String message, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(wrapMessage(message), getLogArgsToJson(args));
        }
    }

    public static void trace(String message, Supplier<?>... args) {
        if (logger.isTraceEnabled()) {
            logger.trace(wrapMessage(message), getLogArgs(args));
        }
    }

    public static void trace(Logger logger, String message, Supplier<?>... args) {
        if (logger.isTraceEnabled()) {
            logger.trace(wrapMessage(message), getLogArgs(args));
        }
    }

    public static void traceToJson(String message, Object... args) {
        if (logger.isTraceEnabled()) {
            logger.trace(wrapMessage(message), getLogArgsToJson(args));
        }
    }

    public static void traceToJson(Logger logger, String message, Object... args) {
        if (logger.isTraceEnabled()) {
            logger.trace(wrapMessage(message), getLogArgsToJson(args));
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
            for (int i = 0, len = args.length; i < len; i++) {
                if (args[i] instanceof Throwable) {
                    continue;
                }
                args[i] = JSON.toJSONString(args[i]);
            }
            return args;
        }
    }

    private static String wrapMessage(String message) {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[3];
        return caller.getMethodName() + "(" + caller.getFileName() + ":" + caller.getLineNumber() + ") - " +
                Log.getFullTid() +
                message;
    }

    // *****************************************************************************************************************
    //                                              LOG END
    // *****************************************************************************************************************

    // *****************************************************************************************************************
    //                                              TRACE START
    // *****************************************************************************************************************

    public static String uuid() {
        return String.valueOf(System.nanoTime());
    }

    private static String fixString(String str) {
        return str == null ? EMPTY : str;
    }

    /**
     * get
     */
    private static Map<String, String> getContextIfPresent() {
        Map<String, String> context = THREAD_LOCAL.get();
        if (context == null) {
            context = new HashMap<>(11);
            // 模块名称
            // tid
            context.put(TID_KEY, EMPTY);
            // orderNo etc.
            context.put(FIXED_KEY, EMPTY);
            // 模块名称
            context.put(MODULE_KEY, EMPTY);
            // tid orderNo 模块名称
            context.put(FULL_TID_KEY, EMPTY);
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
     * remove
     */
    private static void removeContext() {
        THREAD_LOCAL.remove();
    }

    /**
     * 请求一个标识
     *
     * <p>
     * 使用方式
     * <pre> {@code
     *
     *  LogUtil.start("订单号", "模块1");
     *  try {
     *      log.info(LogUtil.getInfoLog("入参: 【{}】", LogUtil.toJsonStringInfo(orderInfo)));
     *      log.info(LogUtil.getInfoLog("出参: 【{}】", "orderNo"));
     *
     *      LogUtil.setModule("模块2");
     *
     *      log.info(LogUtil.getInfoLog("入参: 【{}】", LogUtil.toJsonStringInfo(orderInfo)));
     *      log.info(LogUtil.getInfoLog("出参: 【{}】", "orderNo"));
     *  } finally {
     *      LogUtil.end();
     *  }
     *
     * }</pre>
     *
     * @param fixed 请求标识
     */
    public static void start(String fixed) {
        startByTid(uuid(), fixed, EMPTY);
    }

    public static void start(String fixed, String module) {
        startByTid(uuid(), fixed, module);
    }

    public static void startByTid(String tid) {
        startByTid(tid, EMPTY, EMPTY);
    }

    public static void startByTid(String tid, String fixed) {
        startByTid(tid, fixed, EMPTY);
    }

    public static void startByTid(String tid, String fixed, String module) {
        // get
        Map<String, String> context = getContextIfPresent();

        // tid
        context.put(TID_KEY, tid);
        // orderNo etc.
        context.put(FIXED_KEY, fixString(fixed));
        // 模块名称.
        context.put(MODULE_KEY, fixString(module));
        // tid orderNo 模块名称
        concatContext(context);

        // orderNo etc.
        context.put(FIXED_0_KEY, context.get(FIXED_KEY));
        // 模块名称.
        context.put(MODULE_0_KEY, context.get(MODULE_KEY));

        // set
        setContext(context);
    }

    /**
     * remove
     */
    public static void end() {
        Map<String, String> context = THREAD_LOCAL.get();
        if (context != null) {
            context.clear();
        }
        // removeContext
        removeContext();
    }

    /**
     * {tid} {fixed} {module}
     */
    private static void concatContext(Map<String, String> context) {
        StringBuilder builder = new StringBuilder();
        builder.append(context.get(TID_KEY));
        if (!context.get(FIXED_KEY).isEmpty()) {
            builder.append(LEFT).append(context.get(FIXED_KEY)).append(RIGHT);
        }
        if (!context.get(MODULE_KEY).isEmpty()) {
            builder.append(LEFT).append(context.get(MODULE_KEY)).append(RIGHT);
        }
        builder.append(LOG_CONNECTOR);
        context.put(FULL_TID_KEY, builder.toString());
    }

    /**
     * 重置 fixed
     *
     * @param fixed fixed
     */
    public static void setFixed(String fixed) {
        Map<String, String> context = getContextIfPresent();
        context.put(FIXED_PREV_KEY, context.get(FIXED_KEY));
        context.put(FIXED_KEY, fixString(fixed));
        concatContext(context);
    }

    public static void resetFixed() {
        Map<String, String> context = getContextIfPresent();
        context.put(FIXED_KEY, context.get(FIXED_PREV_KEY));
        concatContext(context);
    }

    public static void resetFixed0() {
        Map<String, String> context = getContextIfPresent();
        context.put(FIXED_KEY, context.get(FIXED_0_KEY));
        concatContext(context);
    }

    /**
     * 重置 module
     *
     * @param module module
     */
    public static void setModule(String module) {
        Map<String, String> context = getContextIfPresent();
        context.put(MODULE_PREV_KEY, context.get(MODULE_KEY));
        context.put(MODULE_KEY, fixString(module));
        concatContext(context);
    }

    public static void resetModule() {
        Map<String, String> context = getContextIfPresent();
        context.put(MODULE_KEY, context.get(MODULE_PREV_KEY));
        concatContext(context);
    }

    public static void resetModule0() {
        Map<String, String> context = getContextIfPresent();
        context.put(MODULE_KEY, context.get(MODULE_0_KEY));
        concatContext(context);
    }

    /**
     * 重置 fixed
     * 重置 module
     *
     * @param fixed  fixed
     * @param module module
     */
    public static void setFixAndModule(String fixed, String module) {
        Map<String, String> context = getContextIfPresent();
        context.put(FIXED_PREV_KEY, context.get(FIXED_KEY));
        context.put(MODULE_PREV_KEY, context.get(MODULE_KEY));
        context.put(FIXED_KEY, fixString(fixed));
        context.put(MODULE_KEY, fixString(module));
        concatContext(context);
    }

    public static void resetFixAndModule() {
        Map<String, String> context = getContextIfPresent();
        context.put(FIXED_KEY, context.get(FIXED_PREV_KEY));
        context.put(MODULE_KEY, context.get(MODULE_PREV_KEY));
        concatContext(context);
    }

    public static void resetFixAndModule0() {
        Map<String, String> context = getContextIfPresent();
        context.put(FIXED_KEY, context.get(FIXED_0_KEY));
        context.put(MODULE_KEY, context.get(MODULE_0_KEY));
        concatContext(context);
    }

    /**
     * 获取 tid
     *
     * @return tid
     */
    public static String getTid() {
        return getContextIfPresent().get(TID_KEY);
    }

    /**
     * 获取 tid
     *
     * @return tid
     */
    public static long getLongTid() {
        try {
            return Long.parseLong(getContextIfPresent().get(TID_KEY));
        } catch (Exception e) {
            long l = System.nanoTime();
            logger.info(wrapMessage("{} tidString ==> tidLong {}"), getContextIfPresent().get(FULL_TID_KEY), l);
            return l;
        }
    }

    /**
     * 获取 丰富的tid
     *
     * @return fullTid : {tid} {fixed} {module}
     */
    public static String getFullTid() {
        return getContextIfPresent().get(FULL_TID_KEY);
    }


    // *****************************************************************************************************************
    //                                              TRACE END
    // *****************************************************************************************************************

    // *****************************************************************************************************************
    //                                              获取日志 START
    // *****************************************************************************************************************

    public static String getErrorLog(String message, String... args) {
        if (logger.isErrorEnabled()) {
            return getLog(message, args);
        }
        return EMPTY;
    }

    public static String getErrorLogJson(String message, Object... args) {
        if (logger.isErrorEnabled()) {
            return getLogAutoJsonString(message, args);
        }
        return EMPTY;
    }

    public static String getWarnLog(String message, String... args) {
        if (logger.isWarnEnabled()) {
            return getLog(message, args);
        }
        return EMPTY;
    }

    public static String getWarnLogJson(String message, Object... args) {
        if (logger.isWarnEnabled()) {
            return getLogAutoJsonString(message, args);
        }
        return EMPTY;
    }

    public static String getInfoLog(String message, String... args) {
        if (logger.isInfoEnabled()) {
            return getLog(message, args);
        }
        return EMPTY;
    }

    public static String getInfoLogJson(String message, Object... args) {
        if (logger.isInfoEnabled()) {
            return getLogAutoJsonString(message, args);
        }
        return EMPTY;
    }

    public static String getDebugLog(String message, String... args) {
        if (logger.isDebugEnabled()) {
            return getLog(message, args);
        }
        return EMPTY;
    }

    public static String getDebugLogJson(String message, Object... args) {
        if (logger.isDebugEnabled()) {
            return getLogAutoJsonString(message, args);
        }
        return EMPTY;
    }


    /**
     * 获取   tid 固定前缀 模块标识 - 用户日志
     *
     * @param message 例如:  接单入参orderInfo：{} {}
     * @param args    例如: LogUtil.toJsonString(orderInfo)
     * @return String 例如: 接单入参orderInfo：{"id":123,"name":"xx"}
     */
    private static String getLog(String message, String... args) {
        return getLogAutoJsonString(message, (Object[]) args);
    }


    /**
     * 获取   tid 固定前缀 模块标识 - 用户日志
     *
     * @param message 例如:  接单入参orderInfo：{} {}
     * @param args    例如: orderInfo
     * @return String 例如: 接单入参orderInfo：{"id":123,"name":"xx"}
     */
    private static String getLogAutoJsonString(String message, Object... args) {
        Map<String, String> context = getContextIfPresent();

        if (isBlank(message)) {
            return context.get(FULL_TID_KEY) + message;
        }
        if (!message.contains(LOG_PLACEHOLDER)) {
            return arrayFormat(message, context, args);
        }
        if (args == null) {
            return context.get(FULL_TID_KEY) + getReplaceFirst(message);
        }
        if (args.length < 1) {
            return context.get(FULL_TID_KEY) + message;
        }

        // 空对象也是一个 `{}`, 防止被外层log.info解析 `{}` 转换成 `{ }`
        for (int i = 0; i < args.length; i++) {
            Object argObj = args[i];
            if (argObj == null || argObj instanceof Throwable) {
                continue;
            }
            if (argObj instanceof String) {
                if (LOG_PLACEHOLDER.equals(argObj)) {
                    args[i] = EMPTY_JSON_OBJECT;
                } else {
                    String argString = (String) argObj;
                    if (argString.contains(LOG_PLACEHOLDER)) {
                        args[i] = getReplaceAll(argString);
                    }
                }
            } else {
                String argJson = JSON.toJSONString(argObj);
                if (LOG_PLACEHOLDER.equals(argJson)) {
                    args[i] = EMPTY_JSON_OBJECT;
                } else if (argJson.contains(LOG_PLACEHOLDER)) {
                    args[i] = getReplaceAll(argJson);
                }
            }
        }

        return arrayFormat(message, context, args);
    }

    private static String arrayFormat(String message, Map<String, String> context, Object[] args) {
        FormattingTuple formattingTuple = MessageFormatter.arrayFormat(context.get(FULL_TID_KEY) + message, args);
        if (formattingTuple.getThrowable() != null) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            formattingTuple.getThrowable().printStackTrace(new PrintStream(bos));
            return formattingTuple.getMessage() + NEWLINE + bos;
        }
        return formattingTuple.getMessage();
    }


    private static String getReplaceFirst(String inString) {
        return PLACEHOLDER_PATTERN.matcher(inString).replaceFirst(Log.NULL);
    }

    private static String getReplaceAll(String inString) {
        return PLACEHOLDER_PATTERN.matcher(inString).replaceAll(Log.EMPTY_JSON_OBJECT);
    }

    public static boolean isBlank(final String cs) {
        if (cs == null || cs.isEmpty()) {
            return true;
        }
        for (int i = 0, strLen = cs.length(); i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    // *****************************************************************************************************************
    //                                              获取日志 END
    // *****************************************************************************************************************


    // *****************************************************************************************************************
    //                                              toJsonString START
    // *****************************************************************************************************************

    /**
     * @see JSON#toJSONString(Object)
     */
    public static String toJsonString(Object obj) {
        return JSON.toJSONString(obj);
    }

    public static String toErrorJsonString(Object obj) {
        if (logger.isErrorEnabled()) {
            return JSON.toJSONString(obj);
        }
        return EMPTY;
    }

    public static String toWarnJsonString(Object obj) {
        if (logger.isWarnEnabled()) {
            return JSON.toJSONString(obj);
        }
        return EMPTY;
    }

    public static String toInfoJsonString(Object obj) {
        if (logger.isInfoEnabled()) {
            return JSON.toJSONString(obj);
        }
        return EMPTY;
    }

    public static String toDebugJsonString(Object obj) {
        if (logger.isDebugEnabled()) {
            return JSON.toJSONString(obj);
        }
        return EMPTY;
    }

    // *****************************************************************************************************************
    //                                              toJsonString END
    // *****************************************************************************************************************

}
