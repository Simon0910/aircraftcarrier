package com.aircraftcarrier.framework.tookit;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lzp
 */
public class JsonUtil {

    private static final Gson GSON = new GsonBuilder().create();

    /**
     * JsonUtil
     */
    private JsonUtil() {
    }

    public static String toJson(Object object) {
        return GSON.toJson(object);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return GSON.fromJson(json, classOfT);
    }

    public static <T> T fromJson(String json, Type typeOfT) {
        return GSON.fromJson(json, typeOfT);
    }

    public static String toJsonString(Object object) {
        return JSON.toJSONString(object);
    }

    public static <T> T parseObj(String jsonStr, Class<T> targetClass) {
        return JSON.parseObject(jsonStr, targetClass);
    }

    public static Map<String, Object> obj2Map(Object object) {
        if (null == object) {
            return MapUtil.newHashMap();
        }
        return (JSONObject) JSON.toJSON(object);
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