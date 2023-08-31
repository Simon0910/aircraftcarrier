package com.aircraftcarrier.framework.tookit;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * 测试 {@link Logger2Util}
 *
 * @author zhipengliu
 * @date 2023/8/30
 * @since 1.0
 */
@Slf4j
public class Logger2UtilTest {
    private static final Logger logger = LoggerFactory.getLogger(LoggerUtilTest.class);
    // private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {
        LogUtil.requestStart("你好你好");

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
        Logger2Util.info("11入参数：{}", () -> JSON.toJSONString(orderInfo));
        Logger2Util.info("22入参数：{}", () -> JSON.toJSONString(orderInfo));
        Logger2Util.info("33入参数：{}", () -> JSON.toJSONString(orderInfo), () -> e);
        Logger2Util.info("333入参数：{}", () -> JSON.toJSONString(orderInfo), () -> e, () -> e2);

        Logger2Util.infoToJson("44参数：{}", orderInfo);
        Logger2Util.infoToJson("55入参数：{}", orderInfo);
        Logger2Util.infoToJson("66入参数：{}", orderInfo, e);
        Logger2Util.infoToJson("666入参数：{}", orderInfo, e, e2);
    }
}
