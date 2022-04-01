package com.aircraftcarrier.framework.tookit;

import org.reflections.Reflections;

import java.util.Set;

/**
 * @author lzp
 */
public class ReflectionUtil {
    private static final Reflections REFLECTIONS = new Reflections("com.farm.enums");

    private ReflectionUtil() {
    }

    public static <P> Set<Class<? extends P>> getSubTypesOf(Class<P> parentClass) {
        return REFLECTIONS.getSubTypesOf(parentClass);
    }
}
