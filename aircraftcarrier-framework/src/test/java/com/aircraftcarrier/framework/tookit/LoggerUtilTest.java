package com.aircraftcarrier.framework.tookit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * 测试 {@link LoggerUtil}
 *
 * @author zhipengliu
 * @date 2023/8/30
 * @since 1.0
 */
public class LoggerUtilTest {
    private static final Logger logger = LoggerFactory.getLogger(LoggerUtilTest.class);
    // private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {
        // LogUtil.requestStart("你好");

        RuntimeException e = new RuntimeException("错误了！");
        RuntimeException e2 = new RuntimeException("最后一个错误了！");
        Object nullObject = null;
        HashMap<Object, Object> emptyObject = new HashMap<>();
        HashMap<Object, Object> orderInfo = new HashMap<>();
        orderInfo.put("id", "1");
        orderInfo.put("orderNo", "123");
        orderInfo.put("orderInfoDetail", emptyObject);
        orderInfo.put("isNull", nullObject);

        // 如何解决行号问题

        LoggerUtil.info(logger, "入参数：{}", () -> orderInfo);
    }
}
