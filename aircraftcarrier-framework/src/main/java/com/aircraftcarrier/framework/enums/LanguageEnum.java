package com.aircraftcarrier.framework.enums;

import com.aircraftcarrier.framework.tookit.MapUtil;
import lombok.Getter;

import java.util.Locale;
import java.util.Map;

/**
 * @author lzp
 */
@Getter
public enum LanguageEnum implements IEnum<String> {
    /**
     * zh-CN
     */
    CHINA(Locale.CHINA.toLanguageTag(), Locale.CHINA.getDisplayLanguage()),
    /**
     * zh-TW
     */
    TAIWAN(Locale.TAIWAN.toLanguageTag(), Locale.TAIWAN.getDisplayLanguage()),
    /**
     * en-US
     */
    US(Locale.US.toLanguageTag(), Locale.US.getDisplayLanguage()),
    /**
     * ja-JP
     */
    JAPAN(Locale.JAPAN.toLanguageTag(), Locale.JAPAN.getDisplayLanguage()),
    /**
     * ko-KR
     */
    KOREA(Locale.KOREA.toLanguageTag(), Locale.KOREA.getDisplayLanguage()),
    ;

    /**
     * MAPPINGS
     */
    private static final Map<String, LanguageEnum> MAPPINGS = MapUtil.newHashMap(values().length);

    static {
        for (LanguageEnum value : values()) {
            MAPPINGS.put(value.getCode(), value);
        }
    }

    private final String code;
    private final String desc;

    LanguageEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * languageTag => 枚举
     */
    public static LanguageEnum resolve(String code) {
        return MAPPINGS.get(code);
    }

    public static LanguageEnum convertCode(String desc) {
        return MAPPINGS.get(desc);
    }

}
