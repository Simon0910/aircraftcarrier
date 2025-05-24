package com.aircraftcarrier.framework.gen;

import com.aircraftcarrier.framework.data.PageUtil;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.ISelect;
import com.google.common.base.CaseFormat;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;

/**
 * 打印 user.setName("数据库值")
 *
 * @author zhipengliu
 * @date 2025/5/24
 * @since 1.0
 */
@Slf4j
public class TestUtil {
    private static final String CUSTOM_NAME = "fullEntity";
    private static final String NOT_BLANK = "NOT_BLANK";
    private static final Object NULL_PARAMS = null;
    private static final LinkedHashMap<String, Method> METHODS = new LinkedHashMap<>();

    public static void searchFullEntity(ISelect select, Predicate<Object> predicate) {
        List<Object> pageList;
        int pageNum = 1;
        do {
            pageList = PageUtil.getPageList(pageNum, 100, select);
            for (Object entity : pageList) {
                if (allEquals(entity,
                        "name", "lisi",
                        "status", "normal")) {

                    sleep(30); // 防止 predicate 调用接口频繁

                    if (predicate.test(entity)) {
                        printlnEntity(entity, CUSTOM_NAME);
                        return;
                    }
                }
            }
            pageNum++;
        } while (!pageList.isEmpty());
        log.error("数据未找到！！");
    }

    public static boolean allEquals(Object obj, String... properties) {
        if (obj == null) {
            return false;
        }
        if (properties == null) {
            return false;
        }
        if (METHODS.isEmpty()) {
            saveEntity(obj);
        }
        try {
            Iterator<String> iterator = Arrays.stream(properties).iterator();
            while (iterator.hasNext()) {
                String property = iterator.next();
                if (!property.startsWith("get")) {
                    property = "get" + convertToTitleCase(property);
                }
                if (!iterator.hasNext()) {
                    return false;
                }

                Method declaredMethod = METHODS.get(property);
                if (declaredMethod == null) {
                    log.error("property error: {} {}", property, JSON.toJSONString(METHODS));
                    throw new RuntimeException("property error");
                }

                String eqValue = iterator.next();
                Object dbValue = declaredMethod.invoke(obj, NULL_PARAMS);
                if (NOT_BLANK.equals(eqValue)) {
                    return !Objects.isNull(dbValue);
                }

                Class<?> returnType = declaredMethod.getReturnType();
                Object realEqValue;

                String returnTypeName = returnType.getName();
                switch (returnTypeName) {
                    case "java.lang.String":
                        realEqValue = String.valueOf(eqValue);
                        break;
                    case "java.lang.Integer":
                        realEqValue = Integer.parseInt(eqValue);
                        break;
                    case "java.lang.Long":
                        realEqValue = Long.parseLong(eqValue);
                        break;
                    case "java.lang.Byte":
                        realEqValue = Byte.parseByte(eqValue);
                        break;
                    case "java.lang.Short":
                        realEqValue = Short.parseShort(eqValue);
                        break;
                    case "java.math.BigDecimal":
                        realEqValue = new BigDecimal(eqValue);
                        break;
                    case "java.lang.Boolean":
                        realEqValue = Boolean.parseBoolean(eqValue);
                        break;
                    default:
                        realEqValue = eqValue;
                        break;
                }
                if (!Objects.equals(dbValue, realEqValue)) {
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("invoke error {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
        return true;
    }

    public static boolean allNonNull(Object obj) {
        if (obj == null) {
            return false;
        }
        if (METHODS.isEmpty()) {
            saveEntity(obj);
        }

        try {
            for (Map.Entry<String, Method> entry : METHODS.entrySet()) {
                Method declaredMethod = entry.getValue();
                Object dbValue = declaredMethod.invoke(obj, NULL_PARAMS);
                if (dbValue == null) {
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("invoke error {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
        return true;
    }

    public static boolean allNonNull(Object obj, String... properties) {
        if (obj == null) {
            return false;
        }
        if (properties == null) {
            return false;
        }
        if (METHODS.isEmpty()) {
            saveEntity(obj);
        }
        try {
            for (String property : properties) {
                if (property == null) {
                    return false;
                }
                if (!property.startsWith("get")) {
                    property = "get" + convertToTitleCase(property);
                }

                Method declaredMethod = METHODS.get(property);
                Object dbValue = declaredMethod.invoke(obj, NULL_PARAMS);
                if (dbValue == null) {
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("invoke error {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
        return true;
    }


    public static String convertToTitleCase(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        if (text.contains("_")) {
            // CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, text)
            return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, text);
        }
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, text);
    }

    public static void printlnEntity(Object obj, String varName) {
        if (obj == null) {
            throw new RuntimeException("testEntity未找到");
        }
        if (varName == null) {
            varName = "entity";
        }
        if (METHODS.isEmpty()) {
            saveEntity(obj);
        }
        String entity = varName + ".";
        Method declaredMethod;
        Object dbValue;
        try {
            for (Map.Entry<String, Method> entry : METHODS.entrySet()) {
                declaredMethod = entry.getValue();
                dbValue = declaredMethod.invoke(obj, NULL_PARAMS);
                String setFileNameXxx = genSetString(dbValue, entity, declaredMethod);
                System.err.println(setFileNameXxx);
            }
        } catch (Exception e) {
            log.error("invoke error {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }

    @NotNull
    private static String genSetString(Object value, String entity, Method declaredMethod) {
        String str;
        if (value == null) {
            str = entity + declaredMethod.getName() + "(null);";
        } else if (value instanceof Long) {
            str = entity + declaredMethod.getName() + "(" + value + "L);";
        } else if (value instanceof Short) {
            str = entity + declaredMethod.getName() + "((short) " + value + "L);";
        } else if (value instanceof Byte) {
            str = entity + declaredMethod.getName() + "((byte) " + value + "L);";
        } else if (value instanceof CharSequence) {
            str = entity + declaredMethod.getName() + "(\"" + value + "\");";
        } else {
            str = entity + declaredMethod.getName() + "(" + value + ");";
        }
        return str.replace("get", "set");
    }

    private static void saveEntity(Object entity) {
        Method[] declaredMethods = entity.getClass().getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            if (!declaredMethod.getName().startsWith("get")) {
                continue;
            }
            if (declaredMethod.getParameterCount() != 0) {
                System.err.println(declaredMethod.getName());
                continue;
            }
            METHODS.put(declaredMethod.getName(), declaredMethod);
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

