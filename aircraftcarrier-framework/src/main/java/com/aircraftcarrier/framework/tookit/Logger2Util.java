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
            log(formatLogMessage(getCallerStackTrace(), message), args, logger.atInfo());
        }
    }


    public static void debug(org.slf4j.Logger logger, String message, Supplier<?>... args) {
        if (logger.isDebugEnabled()) {
            log(formatLogMessage(getCallerStackTrace(), message), args, logger.atDebug());
        }
    }

    public static void warn(org.slf4j.Logger logger, String message, Supplier<?>... args) {
        if (logger.isWarnEnabled()) {
            log(formatLogMessage(getCallerStackTrace(), message), args, logger.atWarn());
        }
    }

    public static void error(org.slf4j.Logger logger, String message, Supplier<?>... args) {
        if (logger.isErrorEnabled()) {
            log(formatLogMessage(getCallerStackTrace(), message), args, logger.atError());
        }

    }

    public static void trace(org.slf4j.Logger logger, String message, Supplier<?>... args) {
        if (logger.isTraceEnabled()) {
            log(formatLogMessage(getCallerStackTrace(), message), args, logger.atTrace());
        }
    }

    public static void infoAutoJson(Logger logger, String message, Supplier<?>... args) {
        if (logger.isInfoEnabled()) {
            logAutoJson(formatLogMessage(getCallerStackTrace(), message), args, logger.atInfo());
        }
    }

    public static void debugAutoJson(Logger logger, String message, Supplier<?>... args) {
        if (logger.isDebugEnabled()) {
            logAutoJson(formatLogMessage(getCallerStackTrace(), message), args, logger.atDebug());
        }
    }

    public static void warnAutoJson(Logger logger, String message, Supplier<?>... args) {
        if (logger.isWarnEnabled()) {
            logAutoJson(formatLogMessage(getCallerStackTrace(), message), args, logger.atWarn());
        }
    }

    public static void errorAutoJson(Logger logger, String message, Supplier<?>... args) {
        if (logger.isErrorEnabled()) {
            logAutoJson(formatLogMessage(getCallerStackTrace(), message), args, logger.atError());
        }
    }

    public static void traceAutoJson(Logger logger, String message, Supplier<?>... args) {
        if (logger.isTraceEnabled()) {
            logAutoJson(formatLogMessage(getCallerStackTrace(), message), args, logger.atTrace());
        }
    }


    private static void log(String message, Supplier<?>[] args, LoggingEventBuilder loggingEventBuilder) {
        loggingEventBuilder.setMessage(message);
        for (Supplier<?> arg : args) {
            loggingEventBuilder.addArgument(arg);
        }
        loggingEventBuilder.log();
    }

    private static void logAutoJson(String message, Supplier<?>[] args, LoggingEventBuilder loggingEventBuilder) {
        loggingEventBuilder.setMessage(message);
        for (Supplier<?> arg : args) {
            loggingEventBuilder.addArgument(JSON.toJSONString(arg.get()));
        }
        loggingEventBuilder.log();
    }

    private static StackTraceElement getCallerStackTrace() {
        return Thread.currentThread().getStackTrace()[3];
    }

    private static String formatLogMessage(StackTraceElement caller, String message) {
        return caller.getMethodName() +
                " (" + caller.getFileName() + ":" + caller.getLineNumber() + ") " +
                LogUtil.getFullTid() +
                message;
    }
}
