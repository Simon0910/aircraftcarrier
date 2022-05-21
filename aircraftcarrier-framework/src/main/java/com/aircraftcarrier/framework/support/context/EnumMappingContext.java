package com.aircraftcarrier.framework.support.context;


import com.aircraftcarrier.framework.enums.IEnum;
import com.aircraftcarrier.framework.tookit.MapUtil;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.util.*;

/**
 * @author lzp
 */
@Slf4j
public class EnumMappingContext {

    private static final String NAME = "code";
    private static final String DESC = "name";
    /**
     * MAPPING
     */
    private static Map<String, List<Map<String, Object>>> MAPPINGS;

    private final String scanBasePackages;

    public EnumMappingContext(String scanBasePackages) {
        this.scanBasePackages = scanBasePackages;
        init();
    }

    public static List<Map<String, Object>> queryEnumsByName(String enumName) {
        List<Map<String, Object>> maps = MAPPINGS.get(enumName);
        if (maps == null) {
            return new ArrayList<>();
        }
        return maps;
    }

    public static Map<String, List<Map<String, Object>>> queryAllEnums() {
        return MAPPINGS;
    }

    private static List<Map<String, Object>> transferEnums(IEnum[] enumArr) {
        List<Map<String, Object>> list = new ArrayList<>(enumArr.length);
        for (IEnum anEnum : enumArr) {
            Map<String, Object> map = MapUtil.newHashMap(2);
            map.put(NAME, ((Enum) anEnum).name());
            map.put(DESC, anEnum.desc());
            list.add(map);
        }
        return list;
    }

    public void init() {
        // 扫包
        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .setScanners(
                                // 设置Annotation的Scanner.
                                new TypeAnnotationsScanner(),
                                // 设置扫描子类型的scanner.
                                new SubTypesScanner(false)
                        )
                        // 设置需要扫描的包，虽然指定了包路径，但是其实还是扫描整个root路径.
                        .setUrls(ClasspathHelper.forPackage(scanBasePackages))
                        // 因为上面的原因，所以这里加上了inputs过滤器
                        .filterInputsBy(new FilterBuilder().includePackage(scanBasePackages))
        );

        Set<Class<? extends IEnum>> allTypes = reflections.getSubTypesOf(IEnum.class);

        Map<String, List<Map<String, Object>>> map = MapUtil.newHashMap(allTypes.size());
        for (Class<? extends IEnum> anEnum : allTypes) {
            IEnum[] enumArr = anEnum.getEnumConstants();
            List<Map<String, Object>> maps = transferEnums(enumArr);
            map.put(anEnum.getSimpleName(), maps);
        }
        MAPPINGS = Collections.unmodifiableMap(map);
        log.info("DisplayEnumMapping init finish!");
    }
}
