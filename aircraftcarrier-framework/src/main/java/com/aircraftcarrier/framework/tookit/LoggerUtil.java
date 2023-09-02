package com.aircraftcarrier.framework.tookit;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * 封装LogUtils实现延迟加载与日志等级判断
 * <a href="https://dioxide-cn.ink/archives/why-package-logger">...</a>
 *
 * @author Dioxide.CN
 * @date 2023/5/24
 */
public class LoggerUtil {

    private static final Logger logger = LoggerFactory.getLogger(LoggerUtil.class);

    private LoggerUtil() {
    }

    public static void info(String message, Supplier<?>... args) {
        if (logger.isInfoEnabled()) {
            logger.info(formatLogMessage(message), getLogArgs(args));
        }
    }

    public static void debug(String message, Supplier<?>... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(formatLogMessage(message), getLogArgs(args));
        }
    }

    public static void warn(String message, Supplier<?>... args) {
        if (logger.isWarnEnabled()) {
            logger.warn(formatLogMessage(message), getLogArgs(args));
        }
    }

    public static void error(String message, Supplier<?>... args) {
        if (logger.isErrorEnabled()) {
            logger.error(formatLogMessage(message), getLogArgs(args));
        }
    }

    public static void trace(String message, Supplier<?>... args) {
        if (logger.isTraceEnabled()) {
            logger.trace(formatLogMessage(message), getLogArgs(args));
        }
    }

    public static void infoToJson(String message, Object... args) {
        if (logger.isInfoEnabled()) {
            logger.info(formatLogMessage(message), getLogArgsToJson(args));
        }
    }

    public static void debugToJson(String message, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(formatLogMessage(message), getLogArgsToJson(args));
        }
    }

    public static void warnToJson(String message, Object... args) {
        if (logger.isWarnEnabled()) {
            logger.warn(formatLogMessage(message), getLogArgsToJson(args));
        }
    }

    public static void errorToJson(String message, Object... args) {
        if (logger.isErrorEnabled()) {
            logger.error(formatLogMessage(message), getLogArgsToJson(args));
        }
    }

    public static void traceToJson(String message, Object... args) {
        if (logger.isTraceEnabled()) {
            logger.trace(formatLogMessage(message), getLogArgsToJson(args));
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

    private static String formatLogMessage(String message) {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[3];
        return caller.getMethodName() + "(" + caller.getFileName() + ":" + caller.getLineNumber() + ") - " +
                Log.getFullTid() +
                message;
    }


}