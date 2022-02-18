//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.aircraftcarrier.framework.tookit;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

/**
 * @author lzp
 */
public class ModelMapperUtil {
    private static final ModelMapper MODEL_MAPPER = new ModelMapper();

    private ModelMapperUtil() {
    }

    public static <D> D convert(Object source, Class<D> targetClass) {
        return MODEL_MAPPER.map(source, targetClass);
    }

    public static <T, D> List<D> convertList(List<T> sources) {
        Type type = new TypeToken<List<D>>() {
        }.getType();
        return MODEL_MAPPER.map(sources, type);
    }
}
