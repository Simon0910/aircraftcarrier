package com.aircraftcarrier.framework.tookit;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.HashSet;

/**
 * 验证 {@link LogUtil}
 *
 * @author zhipengliu
 * @date 2023/8/26
 * @since 1.0
 */
@Slf4j
public class LogUtilTest {
    public static HashSet caseSet = new HashSet<>();
    private static final String lineSeparator = System.lineSeparator();

    public static void main(String[] args) {
        RuntimeException e = new RuntimeException("错误了！");
        RuntimeException e2 = new RuntimeException("最后一个错误了！");
        Object nullObject = null;
        HashMap<Object, Object> emptyObject = new HashMap<>();
        HashMap<Object, Object> orderInfo = new HashMap<>();
        orderInfo.put("id", "1");
        orderInfo.put("orderNo", "123");
        orderInfo.put("orderInfoDetail", emptyObject);
        orderInfo.put("isNull", nullObject);

        String rr;
        try {
            log.info("==================================================================case1");
            LogUtil.resetModule("没有启动tid case1");

            long tidLong = LogUtil.getTidLong();
            log.info("long: " + tidLong);

            rr = LogUtil.getTid();
            log.info(rr);
            Assert.isTrue("".equals(rr));


            rr = LogUtil.getFullTid();
            log.info(rr);
            Assert.isTrue("".equals(rr));


            rr = LogUtil.getLog(null, null);
            log.info(rr);
            Assert.isTrue("null".equals(rr));


            rr = LogUtil.getLog("", "");
            log.info(rr);
            Assert.isTrue("".equals(rr));


            rr = LogUtil.getLog(null, "");
            log.info(rr);
            Assert.isTrue("null".equals(rr));


            rr = LogUtil.getLog("", null);
            log.info(rr);
            Assert.isTrue("".equals(rr));


            rr = LogUtil.getLog("1111");
            log.info(rr);
            Assert.isTrue("1111".equals(rr));


            rr = LogUtil.getLog("1111", null);
            log.info(rr);
            Assert.isTrue("1111".equals(rr));


            rr = LogUtil.getLog("1111", "");
            log.info(rr);
            Assert.isTrue("1111".equals(rr));


            rr = LogUtil.getLog("2222: {}", null);
            log.info(rr);
            Assert.isTrue("2222: null".equals(rr));


            rr = LogUtil.getLog("2222: {}", "");
            log.info(rr);
            Assert.isTrue("2222: ".equals(rr));


            rr = LogUtil.getLog("2222: {}", "2222");
            log.info(rr);
            Assert.isTrue("2222: 2222".equals(rr));


            rr = LogUtil.getLog("3333: {}, {}", null, null);
            log.info(rr);
            Assert.isTrue("3333: null, null".equals(rr));


            rr = LogUtil.getLog("3333: {}, {}", "", "");
            log.info(rr);
            Assert.isTrue("3333: , ".equals(rr));


            rr = LogUtil.getLog("3333: {}, {}", null);
            log.info(rr, "");
            Assert.isTrue("3333: null, {}".equals(rr));


            rr = LogUtil.getLog("3333: {}, {}", "");
            log.info(rr, "");
            Assert.isTrue("3333: , {}".equals(rr));


            rr = LogUtil.getLog("3333: {}, {}", "");
            log.info(rr, nullObject);
            Assert.isTrue("3333: , {}".equals(rr));


            rr = LogUtil.getLog("4444: {}, {}", JSON.toJSONString(nullObject));
            log.info(rr, "4444");
            Assert.isTrue("4444: null, {}".equals(rr));


            rr = LogUtil.getLog("4444: {}, {}", JSON.toJSONString(emptyObject));
            log.info(rr, "4444");
            Assert.isTrue("4444: { }, {}".equals(rr));


            rr = LogUtil.getLog("4444: {}, {}", JSON.toJSONString(orderInfo));
            log.info(rr, "4444");
            Assert.isTrue("4444: {\"orderNo\":\"123\",\"id\":\"1\",\"orderInfoDetail\":{ }}, {}".equals(rr));


            log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            rr = LogUtil.getLog("5555 .");
            log.error(rr, e); // ok
            Assert.isTrue("5555 .".equals(rr));

            log.error(rr, e, e2); // ok

            rr = LogUtil.getLog("5555 {}.");
            log.error(rr, "1", e); // ok
            Assert.isTrue("5555 {}.".equals(rr));

            rr = LogUtil.getLog("5555 {}.");
            log.error(rr, e);                   // 多一个{}
            Assert.isTrue("5555 {}.".equals(rr));

            rr = LogUtil.getLog("5555 {} {}.");
            log.error(rr, "1", e, "3");      // e参数中间只打印messge 当string使用 3被忽略
            Assert.isTrue("5555 {} {}.".equals(rr));

            rr = LogUtil.getLog("5555 {} {} {}.");
            log.error(rr, "1", e, "3");   // e参数中间只打印messge 当string使用
            Assert.isTrue("5555 {} {} {}.".equals(rr));
            log.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

            Assert.isTrue(validGetLog());
            caseSet.clear();


            log.info("==================================================================case2");
            LogUtil.requestStart("启动tid case2");
            String tid = LogUtil.getTid();


            rr = LogUtil.getTid();
            log.info(rr);
            Assert.isTrue(tid.equals(rr));


            rr = LogUtil.getFullTid();
            log.info(rr);
            Assert.isTrue(LogUtil.getFullTid().equals(rr));


            rr = LogUtil.getLog(null, null);
            log.info(rr);
            Assert.isTrue((LogUtil.getFullTid() + "null").equals(rr));


            rr = LogUtil.getLog("", "");
            log.info(rr);
            Assert.isTrue((LogUtil.getFullTid()).equals(rr));


            rr = LogUtil.getLog("", null);
            log.info(rr);
            Assert.isTrue((LogUtil.getFullTid()).equals(rr));


            rr = LogUtil.getLog(null, "");
            log.info(rr);
            Assert.isTrue((LogUtil.getFullTid() + "null").equals(rr));


            rr = LogUtil.getLog("1111");
            log.info(rr);
            Assert.isTrue((LogUtil.getFullTid() + "1111").equals(rr));


            rr = LogUtil.getLog("1111", null);
            log.info(rr);
            Assert.isTrue((LogUtil.getFullTid() + "1111").equals(rr));


            rr = LogUtil.getLog("1111", "");
            log.info(rr);
            Assert.isTrue((LogUtil.getFullTid() + "1111").equals(rr));


            rr = LogUtil.getLog("2222: {}", null);
            log.info(rr);
            Assert.isTrue((LogUtil.getFullTid() + "2222: null").equals(rr));


            rr = LogUtil.getLog("2222: {}", "");
            log.info(rr);
            Assert.isTrue((LogUtil.getFullTid() + "2222: ").equals(rr));


            rr = LogUtil.getLog("2222: {}", "2222");
            log.info(rr);
            Assert.isTrue((LogUtil.getFullTid() + "2222: 2222").equals(rr));


            rr = LogUtil.getLog("3333: {}, {}", null, null);
            log.info(rr);
            Assert.isTrue((LogUtil.getFullTid() + "3333: null, null").equals(rr));


            rr = LogUtil.getLog("3333: {}, {}", "", "");
            log.info(rr);
            Assert.isTrue((LogUtil.getFullTid() + "3333: , ").equals(rr));


            rr = LogUtil.getLog("3333: {}, {}", null);
            log.info(rr, "");
            Assert.isTrue((LogUtil.getFullTid() + "3333: null, {}").equals(rr));


            rr = LogUtil.getLog("3333: {}, {}", "");
            log.info(rr, "");
            Assert.isTrue((LogUtil.getFullTid() + "3333: , {}").equals(rr));


            rr = LogUtil.getLog("3333: {}, {}", "");
            log.info(rr, nullObject);
            Assert.isTrue((LogUtil.getFullTid() + "3333: , {}").equals(rr));


            rr = LogUtil.getLog("4444: {}, {}", JSON.toJSONString(nullObject));
            log.info(rr, "4444");
            Assert.isTrue((LogUtil.getFullTid() + "4444: null, {}").equals(rr));


            rr = LogUtil.getLog("4444: {}, {}", JSON.toJSONString(emptyObject));
            log.info(rr, "4444");
            Assert.isTrue((LogUtil.getFullTid() + "4444: { }, {}").equals(rr));


            rr = LogUtil.getLog("4444: {}, {}", JSON.toJSONString(orderInfo));
            log.info(rr, "4444");
            Assert.isTrue((LogUtil.getFullTid() + "4444: {\"orderNo\":\"123\",\"id\":\"1\",\"orderInfoDetail\":{ }}, {}").equals(rr));


            rr = LogUtil.getLog(null, null);

            Assert.isTrue(validGetLog());
            caseSet.clear();

            log.info("==================================================================case3");
            LogUtil.resetFixed("同一个tid哦 orderNo");
            LogUtil.resetModule("模块2");
            LogUtil.resetModule("同一个tid哦 case3");
            log.info(LogUtil.getTid());
            log.info(LogUtil.getFullTid());

            log.info("==================================================================case4");
            LogUtil.resetModule("同一个tid哦 case4");
            LogUtil.resetModule(null);
            log.info(LogUtil.getFullTid());

            LogUtil.resetFixed(null);
            log.info(LogUtil.getFullTid());

            log.info("==================================================================case5");
            LogUtil.requestStart("新的Tid哦", "case5");
            tid = LogUtil.getTid();

            rr = LogUtil.getTid();
            log.info(rr);
            Assert.isTrue(tid.equals(LogUtil.getTid()));


            rr = LogUtil.getFullTid();
            log.info(rr);


            rr = LogUtil.getLogToJson(null, null);
            log.info(rr);
            Assert.isTrue((LogUtil.getFullTid() + "null").equals(rr));


            rr = LogUtil.getLogToJson("", "");
            log.info(rr);
            Assert.isTrue((LogUtil.getFullTid()).equals(rr));


            rr = LogUtil.getLogToJson("", null);
            log.info(rr);
            Assert.isTrue((LogUtil.getFullTid()).equals(rr));


            rr = LogUtil.getLogToJson(null, "");
            log.info(rr);
            Assert.isTrue((LogUtil.getFullTid() + "null").equals(rr));


            rr = LogUtil.getLogToJson("1111");
            log.info(rr);
            Assert.isTrue((LogUtil.getFullTid() + "1111").equals(rr));


            rr = LogUtil.getLogToJson("1111", null);
            log.info(rr);
            Assert.isTrue((LogUtil.getFullTid() + "1111").equals(rr));


            rr = LogUtil.getLogToJson("1111", "");
            log.info(rr);
            Assert.isTrue((LogUtil.getFullTid() + "1111").equals(rr));


            rr = LogUtil.getLogToJson("2222: {}", null);
            log.info(rr);
            Assert.isTrue((LogUtil.getFullTid() + "2222: null").equals(rr));


            rr = LogUtil.getLogToJson("2222: {}", "");
            log.info(rr);
            Assert.isTrue((LogUtil.getFullTid() + "2222: ").equals(rr));


            rr = LogUtil.getLogToJson("2222: {}", "2222");
            log.info(rr);
            Assert.isTrue((LogUtil.getFullTid() + "2222: 2222").equals(rr));


            rr = LogUtil.getLogToJson("3333: {}, {}", null, null);
            log.info(rr);
            Assert.isTrue((LogUtil.getFullTid() + "3333: null, null").equals(rr));


            rr = LogUtil.getLogToJson("3333: {}, {}", "", "");
            log.info(rr);
            Assert.isTrue((LogUtil.getFullTid() + "3333: , ").equals(rr));


            rr = LogUtil.getLogToJson("3333: {}, {}", null);
            log.info(rr, "");
            Assert.isTrue((LogUtil.getFullTid() + "3333: null, {}").equals(rr));


            rr = LogUtil.getLogToJson("3333: {}, {}", "");
            log.info(rr, "");
            Assert.isTrue((LogUtil.getFullTid() + "3333: , {}").equals(rr));


            rr = LogUtil.getLogToJson("3333: {}, {}", "");
            log.info(rr, nullObject);
            Assert.isTrue((LogUtil.getFullTid() + "3333: , {}").equals(rr));


            rr = LogUtil.getLogToJson("4444: {}, {}", JSON.toJSONString(nullObject));
            log.info(rr, "4444");
            Assert.isTrue((LogUtil.getFullTid() + "4444: null, {}").equals(rr));


            rr = LogUtil.getLogToJson("4444: {}, {}", JSON.toJSONString(emptyObject));
            log.info(rr, "4444");
            Assert.isTrue((LogUtil.getFullTid() + "4444: { }, {}").equals(rr));


            rr = LogUtil.getLogToJson("4444: {}, {}", JSON.toJSONString(orderInfo));
            log.info(rr, "4444");
            Assert.isTrue((LogUtil.getFullTid() + "4444: {\"orderNo\":\"123\",\"id\":\"1\",\"orderInfoDetail\":{ }}, {}").equals(rr));


            rr = LogUtil.getLogToJson("4444: {}, {}", nullObject);
            log.info(rr, "4444");
            Assert.isTrue((LogUtil.getFullTid() + "4444: null, {}").equals(rr));


            rr = LogUtil.getLogToJson("4444: {}, {}", emptyObject);
            log.info(rr, "4444");
            Assert.isTrue((LogUtil.getFullTid() + "4444: { }, {}").equals(rr));


            rr = LogUtil.getLogToJson("4444: {}, {}", orderInfo);
            log.info(rr, "4444");
            Assert.isTrue((LogUtil.getFullTid() + "4444: {\"orderNo\":\"123\",\"id\":\"1\",\"orderInfoDetail\":{ }}, {}").equals(rr));


            log.info("==================================================================case6");
            LogUtil.resetModule("case6");

            log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            rr = LogUtil.getLogToJson("5555 .");
            log.error(rr, e); // ok
            Assert.isTrue((LogUtil.getFullTid() + "5555 .").equals(rr));

            log.error(rr, e, e2); // ok

            rr = LogUtil.getLogToJson("5555 {}.");
            log.error(rr, "1", e); // ok
            Assert.isTrue((LogUtil.getFullTid() + "5555 {}.").equals(rr));

            rr = LogUtil.getLogToJson("5555 {}.");
            log.error(rr, e);                   // 多一个{}
            Assert.isTrue((LogUtil.getFullTid() + "5555 {}.").equals(rr));

            rr = LogUtil.getLogToJson("5555 {} {}.");
            log.error(rr, "1", e, "3");      // e参数中间只打印messge 当string使用 3被忽略
            Assert.isTrue((LogUtil.getFullTid() + "5555 {} {}.").equals(rr));

            rr = LogUtil.getLogToJson("5555 {} {} {}.");
            log.error(rr, "1", e, "3");   // e参数中间只打印messge 当string使用
            Assert.isTrue((LogUtil.getFullTid() + "5555 {} {} {}.").equals(rr));
            log.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");


            rr = LogUtil.getLogToJson("5555.", e);
            log.error(rr); // ok
            String expect = LogUtil.getFullTid() + "5555." + lineSeparator +
                    "java.lang.RuntimeException: 错误了！" + lineSeparator +
                    "\tat com.aircraftcarrier.framework.tookit.LogUtilTest.main(LogUtilTest.java:23)" + lineSeparator;
            Assert.isTrue(expect.equals(rr));

            rr = LogUtil.getLogToJson("5555.", e, e2);
            log.error(rr); // ok
            expect = LogUtil.getFullTid() + "5555." + lineSeparator +
                    "java.lang.RuntimeException: 最后一个错误了！" + lineSeparator +
                    "\tat com.aircraftcarrier.framework.tookit.LogUtilTest.main(LogUtilTest.java:24)" + lineSeparator;
            Assert.isTrue(expect.equals(rr));

            rr = LogUtil.getLogToJson("5555 {}.", "1", e);
            log.error(rr); // ok
            expect = LogUtil.getFullTid() + "5555 1." + lineSeparator +
                    "java.lang.RuntimeException: 错误了！" + lineSeparator +
                    "\tat com.aircraftcarrier.framework.tookit.LogUtilTest.main(LogUtilTest.java:23)" + lineSeparator;
            Assert.isTrue(expect.equals(rr));

            rr = LogUtil.getLogToJson("5555 {}.", e);
            log.error(rr); // 多一个{}
            expect = LogUtil.getFullTid() + "5555 {}." + lineSeparator +
                    "java.lang.RuntimeException: 错误了！" + lineSeparator +
                    "\tat com.aircraftcarrier.framework.tookit.LogUtilTest.main(LogUtilTest.java:23)" + lineSeparator;
            Assert.isTrue(expect.equals(rr));

            rr = LogUtil.getLogToJson("5555 {} {}.", "1", e, "3");
            log.error(rr); // e参数中间只打印messge 当string使用
            expect = LogUtil.getFullTid() + "5555 1 java.lang.RuntimeException: 错误了！.";
            Assert.isTrue(expect.equals(rr));

            rr = LogUtil.getLogToJson("5555 {} {} {}.", "1", e, "3");
            log.error(rr); // e参数中间只打印messge 当string使用
            expect = LogUtil.getFullTid() + "5555 1 java.lang.RuntimeException: 错误了！ 3.";
            Assert.isTrue(expect.equals(rr));

            log.info("==================================================================case7");
            LogUtil.resetModule("case7");

            rr = LogUtil.getLogToJson("5555 {} {} {}", "1", e);
            log.error(rr, "2", "3");
            expect = LogUtil.getFullTid() + "5555 1 {} {}" + lineSeparator +
                    "java.lang.RuntimeException: 错误了！" + lineSeparator +
                    "\tat com.aircraftcarrier.framework.tookit.LogUtilTest.main(LogUtilTest.java:23)" + lineSeparator;
            Assert.isTrue(expect.equals(rr));

            log.error(rr, "2", e2);

            rr = LogUtil.getLogToJson("5555 {} {} {}", e);
            log.error(rr, e2);
            expect = LogUtil.getFullTid() + "5555 {} {} {}" + lineSeparator +
                    "java.lang.RuntimeException: 错误了！" + lineSeparator +
                    "\tat com.aircraftcarrier.framework.tookit.LogUtilTest.main(LogUtilTest.java:23)" + lineSeparator;
            Assert.isTrue(expect.equals(rr));

            log.info("==================================================================case8 推荐");
            LogUtil.resetModule("case8 推荐写法");

            rr = LogUtil.getLog("666 {}, {}, {}");
            log.error(rr, LogUtil.toJsonString(orderInfo), LogUtil.toJsonString(emptyObject), LogUtil.toJsonString(orderInfo), e); // 推荐
            Assert.isTrue((LogUtil.getTid() + "【新的Tid哦】【case8 推荐写法】 - 666 {}, {}, {}").equals(rr));


            rr = LogUtil.getLog("666 {}, {}, {}");
            log.error(rr, LogUtil.toJsonString(orderInfo), LogUtil.toJsonString(nullObject), LogUtil.toJsonString(orderInfo), e); // 推荐
            Assert.isTrue((LogUtil.getTid() + "【新的Tid哦】【case8 推荐写法】 - 666 {}, {}, {}").equals(rr));


            rr = LogUtil.getLogToJson("666 {}, {}, {}", orderInfo, emptyObject, orderInfo);
            log.error(rr, e); // 推荐
            Assert.isTrue((LogUtil.getTid() + "【新的Tid哦】【case8 推荐写法】 - 666 {\"orderNo\":\"123\",\"id\":\"1\",\"orderInfoDetail\":{ }}, { }, {\"orderNo\":\"123\",\"id\":\"1\",\"orderInfoDetail\":{ }}").equals(rr));


            rr = LogUtil.getLogToJson("666 {}, {}, {}", orderInfo, nullObject, orderInfo);
            log.error(rr, e); // 推荐
            Assert.isTrue((LogUtil.getTid() + "【新的Tid哦】【case8 推荐写法】 - 666 {\"orderNo\":\"123\",\"id\":\"1\",\"orderInfoDetail\":{ }}, null, {\"orderNo\":\"123\",\"id\":\"1\",\"orderInfoDetail\":{ }}").equals(rr));


            Assert.isTrue(validGetLog());
            caseSet.clear();

            // 如何解决行号问题
            LoggerUtil.info(log, "入参数：{}", () -> orderInfo);

        } finally {
            try {
                Thread.sleep(1000); // 等待log4j2 异步日志结束
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            // remove
            LogUtil.requestEnd();
        }
    }

    private static boolean validGetLog() {
        System.out.println("valid case: " + JSON.toJSONString(caseSet));
        return true;
    }
}
