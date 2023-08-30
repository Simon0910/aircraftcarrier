package com.aircraftcarrier.framework.tookit;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
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

    private Logger2Util() {
    }

    public static void info(org.slf4j.Logger logger, String message, Supplier<?>... args) {
        if (logger.isInfoEnabled()) {
            log(logger.atInfo(), formatLogMessage(getCallerStackTrace(), message), args);
        }
    }


    public static void debug(org.slf4j.Logger logger, String message, Supplier<?>... args) {
        if (logger.isDebugEnabled()) {
            log(logger.atDebug(), formatLogMessage(getCallerStackTrace(), message), args);
        }
    }

    public static void warn(org.slf4j.Logger logger, String message, Supplier<?>... args) {
        if (logger.isWarnEnabled()) {
            log(logger.atWarn(), formatLogMessage(getCallerStackTrace(), message), args);
        }
    }

    public static void error(org.slf4j.Logger logger, String message, Supplier<?>... args) {
        if (logger.isErrorEnabled()) {
            log(logger.atError(), formatLogMessage(getCallerStackTrace(), message), args);
        }

    }

    public static void trace(org.slf4j.Logger logger, String message, Supplier<?>... args) {
        if (logger.isTraceEnabled()) {
            log(logger.atTrace(), formatLogMessage(getCallerStackTrace(), message), args);
        }
    }

    public static void infoAutoJson(Logger logger, String message, Object... args) {
        if (logger.isInfoEnabled()) {
            logAutoJson(logger.atInfo(), formatLogMessage(getCallerStackTrace(), message), args);
        }
    }

    public static void debugAutoJson(Logger logger, String message, Object... args) {
        if (logger.isDebugEnabled()) {
            logAutoJson(logger.atDebug(), formatLogMessage(getCallerStackTrace(), message), args);
        }
    }

    public static void warnAutoJson(Logger logger, String message, Object... args) {
        if (logger.isWarnEnabled()) {
            logAutoJson(logger.atWarn(), formatLogMessage(getCallerStackTrace(), message), args);
        }
    }

    public static void errorAutoJson(Logger logger, String message, Object... args) {
        if (logger.isErrorEnabled()) {
            logAutoJson(logger.atError(), formatLogMessage(getCallerStackTrace(), message), args);
        }
    }

    public static void traceAutoJson(Logger logger, String message, Object... args) {
        if (logger.isTraceEnabled()) {
            logAutoJson(logger.atTrace(), formatLogMessage(getCallerStackTrace(), message), args);
        }
    }


    private static void log(LoggingEventBuilder loggingEventBuilder, String message, Supplier<?>[] args) {
        loggingEventBuilder.setMessage(message);
        for (Supplier<?> arg : args) {
            loggingEventBuilder.addArgument(arg);
        }
        loggingEventBuilder.log();
    }

    private static void logAutoJson(LoggingEventBuilder loggingEventBuilder, String message, Object... args) {
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
        return "(" + caller.getFileName() + ":" + caller.getLineNumber() + ")." + caller.getMethodName() + " " +
                LogUtil.getFullTid() +
                message;
    }
}
