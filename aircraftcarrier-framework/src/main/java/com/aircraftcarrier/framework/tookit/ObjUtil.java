package com.aircraftcarrier.framework.tookit;


import cn.hutool.core.collection.CollUtil;
import org.springframework.cglib.beans.BeanMap;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * @author lzp
 */
public class ObjUtil {
    private ObjUtil() {
    }

    public static Map<String, Object> obj2Map(Object obj) {
        Map<String, Object> map = MapUtil.newHashMap(128);
        // 获取f对象对应类中的所有属性域
        Field[] fields = obj.getClass().getDeclaredFields();
        try {
            for (Field field : fields) {
                // 获取在对象f中属性fields[i]对应的对象中的变量
                field.setAccessible(true);
                map.put(field.getName(), field.get(obj));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static <T> Map<String, Object> obj2MapBySpring(T object) {
        return null == object ? null : BeanMap.create(object);
    }

    public static <T> T map2Obj(Map<String, Object> map, Class<T> targetClass) {
        if (map == null) {
            return null;
        }
        T t = null;
        try {
            t = targetClass.getDeclaredConstructor().newInstance();
            Field[] fields = t.getClass().getDeclaredFields();
            for (Field field : fields) {
                int mod = field.getModifiers();
                if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
                    continue;
                }
                if (map.containsKey(field.getName())) {
                    field.setAccessible(true);
                    field.set(t, map.get(field.getName()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return t;
    }

    public static <T> T map2ObjBySpring(Map<String, Object> map, Class<T> targetClass) {
        T bean = ClassUtil.newInstance(targetClass);
        BeanMap.create(bean).putAll(map);
        return bean;
    }

    /**
     * @param beans 转换对象集合
     * @return 返回转换后的 bean 列表
     */
    public static <T> List<Map<String, Object>> objects2Maps(List<T> beans) {
        if (CollUtil.isEmpty(beans)) {
            return Collections.emptyList();
        }
        return beans.stream().map(ObjUtil::obj2MapBySpring).collect(toList());
    }

    /**
     * @param maps  转换 MAP 集合
     * @param clazz 对象 Class
     * @return 返回转换后的 bean 集合
     */
    public static <T> List<T> maps2Objects(List<? extends Map<String, Object>> maps, Class<T> clazz) {
        if (CollUtil.isEmpty(maps)) {
            return Collections.emptyList();
        }
        return maps.stream().map(e -> map2ObjBySpring(e, clazz)).collect(toList());
    }
}
