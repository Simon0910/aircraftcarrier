package com.aircraftcarrier.framework.tookit;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LoggingEventBuilder;

import java.util.function.Supplier;

/**
 * Log
 * <a href="https://liuzhihang.com/archives/logger-util">...</a>
 *
 * @author zhipengliu
 * @date 2023/8/30
 * @since 1.0
 */
public class Logger2Util {
    private static final Logger logger = LoggerFactory.getLogger(Logger2Util.class);

    private Logger2Util() {
    }

    public static void info(String message, Supplier<?>... args) {
        if (logger.isInfoEnabled()) {
            log(logger.atInfo(), formatLogMessage(getCallerStackTrace(), message), args);
        }
    }


    public static void debug(String message, Supplier<?>... args) {
        if (logger.isDebugEnabled()) {
            log(logger.atDebug(), formatLogMessage(getCallerStackTrace(), message), args);
        }
    }

    public static void warn(String message, Supplier<?>... args) {
        if (logger.isWarnEnabled()) {
            log(logger.atWarn(), formatLogMessage(getCallerStackTrace(), message), args);
        }
    }

    public static void error(String message, Supplier<?>... args) {
        if (logger.isErrorEnabled()) {
            log(logger.atError(), formatLogMessage(getCallerStackTrace(), message), args);
        }

    }

    public static void trace(String message, Supplier<?>... args) {
        if (logger.isTraceEnabled()) {
            log(logger.atTrace(), formatLogMessage(getCallerStackTrace(), message), args);
        }
    }

    public static void infoToJson(String message, Object... args) {
        if (logger.isInfoEnabled()) {
            logToJson(logger.atInfo(), formatLogMessage(getCallerStackTrace(), message), args);
        }
    }

    public static void debugToJson(String message, Object... args) {
        if (logger.isDebugEnabled()) {
            logToJson(logger.atDebug(), formatLogMessage(getCallerStackTrace(), message), args);
        }
    }

    public static void warnToJson(String message, Object... args) {
        if (logger.isWarnEnabled()) {
            logToJson(logger.atWarn(), formatLogMessage(getCallerStackTrace(), message), args);
        }
    }

    public static void errorToJson(String message, Object... args) {
        if (logger.isErrorEnabled()) {
            logToJson(logger.atError(), formatLogMessage(getCallerStackTrace(), message), args);
        }
    }

    public static void traceToJson(String message, Object... args) {
        if (logger.isTraceEnabled()) {
            logToJson(logger.atTrace(), formatLogMessage(getCallerStackTrace(), message), args);
        }
    }


    private static void log(LoggingEventBuilder loggingEventBuilder, String message, Supplier<?>[] args) {
        loggingEventBuilder.setMessage(message);
        for (Supplier<?> arg : args) {
            loggingEventBuilder.addArgument(arg);
        }
        loggingEventBuilder.log();
    }

    private static void logToJson(LoggingEventBuilder loggingEventBuilder, String message, Object... args) {
        loggingEventBuilder.setMessage(message);
        for (Object arg : args) {
            if (arg instanceof Throwable) {
                loggingEventBuilder.setCause((Throwable) arg);
            }
            loggingEventBuilder.addArgument(JSON.toJSONString(arg));
        }
        loggingEventBuilder.log();
    }

    private static StackTraceElement getCallerStackTrace() {
        return Thread.currentThread().getStackTrace()[3];
    }

    private static String formatLogMessage(StackTraceElement caller, String message) {
        return "(" + caller.getFileName() + ":" + caller.getLineNumber() + ")." + caller.getMethodName() + "() " +
                Log.getFullTid() +
                message;
    }
}
