package com.aircraftcarrier.framework.tookit;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lzp
 */
public class JsonUtils {
    /**
     * JsonUtil
     */
    private JsonUtils() {
    }

    public static String obj2Json(Object object) {
        return JSON.toJSONString(object);
    }

    public static <T> T json2Obj(String jsonStr, Class<T> targetClass) {
        return JSON.parseObject(jsonStr, targetClass);
    }

    public static Map<String, Object> obj2Map(Object object) {
        if (null == object) {
            return MapUtil.newHashMap();
        }
        return (Map<String, Object>) JSON.toJSON(object);
    }

    public static <T> T map2Obj(Map<String, Object> map, Class<T> targetClass) {
        return JSON.toJavaObject(new JSONObject(map), targetClass);
    }

    public static <T> List<T> json2ObjList(List<Map<String, Object>> source, Class<T> targetClass) {
        if (source == null || source.isEmpty()) {
            return new ArrayList<>(0);
        }
        List<T> list = new ArrayList<>(source.size());
        for (Map<String, Object> s : source) {
            list.add(map2Obj(s, targetClass));
        }
        return list;
    }
}