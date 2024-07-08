package com.aircraftcarrier.framework.tookit;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lzp
 */
public class ObjectMapperUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        // 全部字段序列化
        // 对象的所有字段全部列入
        OBJECT_MAPPER.setSerializationInclusion(Include.NON_EMPTY);
        // 取消默认转换timestamps形式
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        // 所有的日期格式都统一为以下的样式
        OBJECT_MAPPER.setDateFormat(new SimpleDateFormat(DateTimeUtil.STANDARD_FORMAT));
        // 忽略空Bean转json的错误
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // 忽略 在json字符串中存在，但是在java对象中不存在对应属性的情况。防止错误
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private ObjectMapperUtil() {
    }

    public static ObjectMapper getInstance() {
        return OBJECT_MAPPER;
    }

    /**
     * 转换为 JSON 字符串
     *
     * @param obj obj
     * @return String
     * @throws JsonProcessingException e
     */
    public static String obj2json(Object obj) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(obj);
    }

    /**
     * 转换为 JSON 字符串，忽略空值
     *
     * @param obj obj
     * @return String
     * @throws JsonProcessingException e
     */
    public static String obj2jsonIgnoreNull(Object obj) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.writeValueAsString(obj);
    }

    /**
     * 有格式的
     *
     * @param obj obj
     * @return String String
     */
    public static <T> String obj2JsonPretty(T obj) {
        if (obj == null) {
            return null;
        }
        try {
            return obj instanceof String ? (String) obj : OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 字符串转对象
     *
     * @param str         str
     * @param targetClass targetClass
     * @return T
     */
    public static <T> T json2Obj(String str, Class<T> targetClass) {
        if (StringUtil.isBlank(str) || targetClass == null) {
            return null;
        }

        try {
            return targetClass.equals(String.class) ? (T) str : OBJECT_MAPPER.readValue(str, targetClass);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 字段符转List之类的集合
     *
     * @param str           str
     * @param typeReference typeReference
     * @return T
     */
    public static <T> T json2Obj(String str, TypeReference<T> typeReference) {
        if (StringUtil.isBlank(str) || typeReference == null) {
            return null;
        }
        try {
            return (typeReference.getType().equals(String.class) ? (T) str : OBJECT_MAPPER.readValue(str, typeReference));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 差不多同上
     *
     * @param str             str
     * @param collectionClass collectionClass
     * @param elementClasses  elementClasses
     * @return T
     */
    public static <T> T json2Obj(String str, Class<?> collectionClass, Class<?>... elementClasses) {
        JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructParametricType(collectionClass, elementClasses);
        try {
            return OBJECT_MAPPER.readValue(str, javaType);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 转换为 JavaBean
     *
     * @param jsonString jsonString
     * @param clazz      clazz
     * @return T
     * @throws JsonProcessingException e
     */
    public static <T> T json2pojo(String jsonString, Class<T> clazz) throws JsonProcessingException {
        OBJECT_MAPPER.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        return OBJECT_MAPPER.readValue(jsonString, clazz);
    }

    /**
     * 字符串转换为 Map<String, Object>
     *
     * @param jsonString jsonString
     * @return Map
     * @throws JsonProcessingException e
     */
    public static Map<String, Object> json2map(String jsonString) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {
        });
    }

    /**
     * 字符串转换为 Map<String, T>
     *
     * @param jsonString jsonString
     * @param clazz      clazz
     * @return Map
     * @throws JsonProcessingException e
     */
    public static <T> Map<String, T> json2map(String jsonString, Class<T> clazz) throws JsonProcessingException {
        Map<String, Map<String, T>> map = OBJECT_MAPPER.readValue(jsonString, new TypeReference<Map<String, Map<String, T>>>() {
        });
        Map<String, T> result = MapUtil.newHashMap(map.size());
        for (Map.Entry<String, Map<String, T>> entry : map.entrySet()) {
            result.put(entry.getKey(), map2pojo(entry.getValue(), clazz));
        }
        return result;
    }

    /**
     * 深度转换 JSON 成 Map
     *
     * @param json json
     * @return Map
     * @throws JsonProcessingException e
     */
    public static Map<String, Object> json2mapDeeply(String json) throws JsonProcessingException {
        return json2MapRecursion(json, OBJECT_MAPPER);
    }

    /**
     * 把 JSON 解析成 List，如果 List 内部的元素存在 jsonString，继续解析
     *
     * @param json   json
     * @param mapper 解析工具
     * @return List
     * @throws JsonProcessingException e
     */
    private static List<Object> json2ListRecursion(String json, ObjectMapper mapper) throws JsonProcessingException {
        if (json == null) {
            return new ArrayList<>();
        }

        List<Object> list = mapper.readValue(json, new TypeReference<List<Object>>() {
        });

        for (Object obj : list) {
            if (obj instanceof String str) {
                if (str.startsWith("[")) {
                    json2ListRecursion(str, mapper);
                } else if (obj.toString().startsWith("{")) {
                    json2MapRecursion(str, mapper);
                }
            }
        }

        return list;
    }

    /**
     * 把 JSON 解析成 Map，如果 Map 内部的 Value 存在 jsonString，继续解析
     *
     * @param json   json
     * @param mapper ObjectMapper
     * @return Map
     * @throws JsonProcessingException e
     */
    private static Map<String, Object> json2MapRecursion(String json, ObjectMapper mapper) throws JsonProcessingException {
        if (json == null) {
            return new HashMap<>(16);
        }

        Map<String, Object> map = mapper.readValue(json, new TypeReference<Map<String, Object>>() {
        });

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object obj = entry.getValue();
            if (obj instanceof String str) {

                if (str.startsWith("[")) {
                    List<?> list = json2ListRecursion(str, mapper);
                    map.put(entry.getKey(), list);
                } else if (str.startsWith("{")) {
                    Map<String, Object> mapRecursion = json2MapRecursion(str, mapper);
                    map.put(entry.getKey(), mapRecursion);
                }
            }
        }

        return map;
    }

    /**
     * 将 JSON 数组转换为集合
     *
     * @param jsonArrayStr jsonArrayStr
     * @param clazz        clazz
     * @return List
     * @throws JsonProcessingException e
     */
    public static <T> List<T> json2list(String jsonArrayStr, Class<T> clazz) throws JsonProcessingException {
        JavaType javaType = getCollectionType(ArrayList.class, clazz);
        return OBJECT_MAPPER.readValue(jsonArrayStr, javaType);
    }


    /**
     * 获取泛型的 Collection Type
     *
     * @param collectionClass 泛型的Collection
     * @param elementClasses  元素类
     * @return JavaType Java类型
     * @since 1.0
     */
    public static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        return OBJECT_MAPPER.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }

    /**
     * 将 Map 转换为 JavaBean
     *
     * @param map   map
     * @param clazz clazz
     * @return T
     */
    public static <T> T map2pojo(Map<String, ?> map, Class<T> clazz) {
        return OBJECT_MAPPER.convertValue(map, clazz);
    }

    /**
     * 将 Map 转换为 JSON
     *
     * @param map map
     * @return String
     */
    public static String mapToJson(Map<String, Object> map) {
        try {
            return OBJECT_MAPPER.writeValueAsString(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 将 JSON 对象转换为 JavaBean
     *
     * @param obj   obj
     * @param clazz clazz
     * @return T
     */
    public static <T> T obj2pojo(Object obj, Class<T> clazz) {
        return OBJECT_MAPPER.convertValue(obj, clazz);
    }
}