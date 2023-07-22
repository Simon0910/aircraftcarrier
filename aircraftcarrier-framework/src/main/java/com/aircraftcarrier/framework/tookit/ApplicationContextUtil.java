package com.aircraftcarrier.framework.tookit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.Map;

/**
 * Spring Context 工具类
 *
 * @author Mark sunlightcs@gmail.com
 */
@Slf4j
public class ApplicationContextUtil {
    private static ApplicationContext applicationContext;

    private ApplicationContextUtil() {
    }

    public static boolean contains(String name) {
        return applicationContext.containsBean(name);
    }

    public static boolean notContains(String name) {
        return !contains(name);
    }

    public static Object getBean(String name) {
        return applicationContext.getBean(name);
    }

    public static <T> T getBean(Class<T> requiredType) {

        return applicationContext.getBean(requiredType);
    }

    public static <T> T getBean(String name, Class<T> requiredType) {
        return applicationContext.getBean(name, requiredType);
    }

    public static <T> Map<String, T> getBeansOfType(Class<T> requiredType) {
        return applicationContext.getBeansOfType(requiredType);
    }

    public static boolean containsBean(String name) {
        return applicationContext.containsBean(name);
    }

    public static boolean isSingleton(String name) {
        return applicationContext.isSingleton(name);
    }

    public static Class<?> getType(String name) {
        return applicationContext.getType(name);
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void setApplicationContext(ApplicationContext applicationContext) {
        ApplicationContextUtil.applicationContext = applicationContext;
    }

}