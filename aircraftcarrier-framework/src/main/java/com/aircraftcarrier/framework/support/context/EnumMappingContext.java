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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author lzp
 */
@Slf4j
public class EnumMappingContext {

    private static final String NAME = "name";
    private static final String DESC = "description";
    /**
     * MAPPING
     */
    private static Map<String, List<Map<String, Object>>> MAPPINGS;

    private final String scanBasePackages;

    public EnumMappingContext(String scanBasePackages) {
        this.scanBasePackages = scanBasePackages;
        init();
    }

    public static List<Map<String, Object>> getEnumByClassName(String className) {
        List<Map<String, Object>> iEnum = MAPPINGS.get(className);
        if (iEnum == null) {
            return new ArrayList<>();
        }
        return iEnum;
    }

    public static Map<String, List<Map<String, Object>>> getEnumList() {
        return MAPPINGS;
    }

    private static List<Map<String, Object>> transferMembers(IEnum<?>[] members) {
        List<Map<String, Object>> list = new ArrayList<>(members.length);
        for (IEnum<?> member : members) {
            Map<String, Object> anObj = MapUtil.newHashMap(2);
            anObj.put(NAME, ((Enum<?>) member).name());
            anObj.put(DESC, member.desc());
            list.add(anObj);
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

        Set<Class<? extends IEnum>> iEnums = reflections.getSubTypesOf(IEnum.class);

        Map<String, List<Map<String, Object>>> iEnumMap = MapUtil.newHashMap(iEnums.size());
        for (Class<? extends IEnum> iEnum : iEnums) {
            List<Map<String, Object>> pre = iEnumMap.get(iEnum.getSimpleName());
            if (pre != null) {
                throw new RuntimeException("Duplicate enum name");
            }

            IEnum<?>[] members = iEnum.getEnumConstants();
            List<Map<String, Object>> objs = transferMembers(members);
            iEnumMap.put(iEnum.getSimpleName(), objs);
        }
        MAPPINGS = Collections.unmodifiableMap(iEnumMap);
        log.info("DisplayEnumMapping init finish!");
    }
}
