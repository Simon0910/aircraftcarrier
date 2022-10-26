package com.aircraftcarrier.framework.basic;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * JdTopicConstant
 *
 * @author zhipengliu
 * @date 2022/8/23
 * @since 1.0
 */
public class JdTopicConstant {
    /**
     * 订单完成
     */
    public static final String ORDER_ORDER_FINISH = "order_order_finish";

    /**
     * 订单付款
     */
    public static final String ORDER_ORDER_PAY = "order_order_pay";

    /**
     * MAPPING
     */
    private static final Map<String, String> MAPPINGS = new HashMap<>(16);

    static {
        Field[] fields = JdTopicConstant.class.getFields();
        for (Field f : fields) {
            String name = f.getName();
            if ("MAPPINGS".equals(name)) {
                continue;
            }
            String value;
            try {
                value = (String) f.get(name);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            String s = MAPPINGS.get(value);
            if (s != null) {
                throw new RuntimeException("Duplicate value!");
            }
            MAPPINGS.put(value, name);
        }
    }

    private JdTopicConstant() {

    }

    public static String resolve(String jdTopic) {
        return MAPPINGS.get(jdTopic);
    }
}
