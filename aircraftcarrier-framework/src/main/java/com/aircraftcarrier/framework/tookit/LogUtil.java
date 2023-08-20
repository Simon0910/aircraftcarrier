package com.aircraftcarrier.framework.tookit;

import org.slf4j.helpers.MessageFormatter;

import java.util.HashMap;
import java.util.Map;

/**
 * LogUtil
 *
 * @author zhipengliu
 * @date 2023/8/20
 * @since 1.0
 */
public class LogUtil {

    private static final ThreadLocal<Map<String, Object>> THREAD_LOCAL = new ThreadLocal<>();

    private static final String TID = "tid";
    private static final String LOG_PRE = "logPre";
    private static final String LOG = "log";

    private static final String CONNECTOR = " - ";
    private static final String EMPTY = "";
    private static final String COLON = ":";

    private LogUtil() {
    }

    /**
     * 请求一个标识
     *
     * <p>
     * 使用方式
     * <pre> {@code
     *  try {
     *      LogUtil.logStart("前缀1");
     *      log.info(LogUtil.getLog("开始..{}", "aaa"));
     *      log.info(LogUtil.getLog("结束.."));
     *      LogUtil.logStart("前缀2");
     *      log.info(LogUtil.getLog("开始.."));
     *      log.info(LogUtil.getLog("结束.."));
     *  } finally {
     *      LogUtil.remove();
     *  }
     *
     * }</pre>
     *
     * @param logPre 请求标识
     */
    public static void logStart(String logPre) {
        logStart(getTid(), logPre);
    }

    public static void logStart(long tid, String logPre) {
        long uid = getTid();
        tid = tid != 0 ? tid : uid;
        logPre = logPre == null ? EMPTY : logPre;

        Map<String, Object> context = THREAD_LOCAL.get();
        if (context == null) {
            context = new HashMap<>(5);
            THREAD_LOCAL.set(context);
        }

        context.put(TID, tid);
        context.put(LOG_PRE, logPre);
        context.put(LOG, context.get(TID) + COLON + logPre);
    }

    /**
     * 重置 logPre
     *
     * @param logPre logPre
     */
    public static void reset(String logPre) {
        Map<String, Object> context = THREAD_LOCAL.get();
        if (context != null) {
            context.put(LOG_PRE, logPre);
            context.put(LOG, context.get(TID) + COLON + logPre);
        }
    }

    /**
     * 获取 tid:请求标识 - 用户日志
     *
     * @param log 用户日志
     * @return tid:请求标识 - 用户日志
     */
    public static String getLog(String log, String... args) {
        Map<String, Object> context = THREAD_LOCAL.get();
        if (context == null) {
            if (args.length > 0) {
                return MessageFormatter.arrayFormat(log, args).getMessage();
            }
            return log;
        }
        if (args.length > 0) {
            return MessageFormatter.arrayFormat(String.format("%s%s%s", context.get(LOG), CONNECTOR, log), args).getMessage();
        }
        return String.format("%s%s%s", context.get(LOG), CONNECTOR, log);
    }


    /**
     * 获取 tid （作用于接口入参）
     *
     * @return tid
     */
    public static long getTid() {
        Map<String, Object> context = THREAD_LOCAL.get();
        if (context == null) {
            return System.nanoTime();
        }
        return (long) context.get(TID);
    }

    /**
     * remove
     */
    public static void remove() {
        Map<String, Object> context = THREAD_LOCAL.get();
        if (context != null) {
            context.clear();
        }
        THREAD_LOCAL.remove();
    }
}