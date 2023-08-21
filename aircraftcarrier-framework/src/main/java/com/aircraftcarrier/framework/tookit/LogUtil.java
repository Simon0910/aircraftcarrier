package com.aircraftcarrier.framework.tookit;

import cn.hutool.core.map.MapUtil;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class LogUtil {
    public static void main(String[] args) {
        try {
            log.info(LogUtil.getLog("1111: {}", "aaa"));
            log.info(LogUtil.getLog("2222: ", ""));
            LogUtil.requestStart("模块1");
            log.info(LogUtil.getLog("1111: {}", "aaa"));
            log.info(LogUtil.getLog("2222"));
            LogUtil.resetLogPre("模块2");
            LogUtil.resetFixedPre("orderNo");
            log.info(LogUtil.getLog("3333: {}"), "hhh");
            LogUtil.resetFixedPre(null);
            LogUtil.resetLogPre(null);
            log.info(LogUtil.getLog("4444"));
            log.info("" + LogUtil.getTid());
        } finally {
            LogUtil.remove();
        }
    }

    private static final ThreadLocal<Map<String, Object>> THREAD_LOCAL = new ThreadLocal<>();

    private static final String TID = "tid";

    private static final String FIXED_PRE = "fixedPre";
    private static final String LOG_PRE = "logPre";
    private static final String TID_LOG_PRE = "log";

    private static final String CONNECTOR = " - ";
    private static final String EMPTY = "";
    private static final String SPACE = " ";

    private LogUtil() {
    }

    /**
     * 请求一个标识
     *
     * <p>
     * 使用方式
     * <pre> {@code
     *  try {
     *      LogUtil.requestStart("模块名称1");
     *      log.info(LogUtil.getLog("开始..{}", "aaa"));
     *      log.info(LogUtil.getLog("结束.."));
     *      LogUtil.resetLogPre("模块名称2");
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
    public static void requestStart(String logPre) {
        requestStart(getTid(), logPre);
    }

    public static void requestStart(long tid, String logPre) {
        long uid = getTid();
        tid = tid != 0 ? tid : uid;
        logPre = logPre == null ? EMPTY : logPre;

        Map<String, Object> context = THREAD_LOCAL.get();
        if (context == null) {
            context = MapUtil.newHashMap(4);
            // orderNo etc.
            context.put(FIXED_PRE, EMPTY);
            THREAD_LOCAL.set(context);
        }

        // tid
        context.put(TID, tid);
        // 模块名称
        context.put(LOG_PRE, logPre);
        // tid orderNo 模块名称
        concat(context);
    }

    /**
     * {tid} {fixedPre} {logPre}
     */
    private static String concat(Map<String, Object> context) {
        String format = String.format("%s%s%s%s%s", context.get(TID), SPACE, context.get(FIXED_PRE), SPACE, context.get(LOG_PRE));
        context.put(TID_LOG_PRE, format);
        return format;
    }


    /**
     * 重置 fixedPre
     *
     * @param fixedPre fixedPre
     */
    public static String resetFixedPre(String fixedPre) {
        Map<String, Object> context = THREAD_LOCAL.get();
        if (context != null) {
            fixedPre = fixedPre == null ? EMPTY : fixedPre;
            Object preFixLog = context.get(FIXED_PRE);
            context.put(FIXED_PRE, fixedPre);
            concat(context);
            return String.valueOf(preFixLog);
        }
        return EMPTY;
    }

    /**
     * 重置 logPre
     *
     * @param logPre logPre
     */
    public static String resetLogPre(String logPre) {
        Map<String, Object> context = THREAD_LOCAL.get();
        if (context != null) {
            logPre = logPre == null ? EMPTY : logPre;
            Object preLog = context.get(LOG_PRE);
            context.put(LOG_PRE, logPre);
            concat(context);
            return String.valueOf(preLog);
        }
        return EMPTY;
    }

    /**
     * 获取 {tid} {fixedPre} {logPre}
     *
     * @return logPre logPre
     */
    public static String getTidAndLogPre() {
        Map<String, Object> context = THREAD_LOCAL.get();
        if (context != null) {
            return String.valueOf(context.get(TID_LOG_PRE));
        }
        return EMPTY;
    }

    /**
     * 获取   tid 固定前缀 模块标识 - 用户日志
     *
     * @param log 用户日志
     * @return tid 固定前缀 模块标识 - 用户日志
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
            return MessageFormatter.arrayFormat(String.format("%s%s%s", context.get(TID_LOG_PRE), CONNECTOR, log), args).getMessage();
        }
        return String.format("%s%s%s", context.get(TID_LOG_PRE), CONNECTOR, log);
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
     * 获取 tid （作用于接口入参）
     *
     * @return tid
     */
    public static String getTidStr() {
        return String.valueOf(getTid());
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