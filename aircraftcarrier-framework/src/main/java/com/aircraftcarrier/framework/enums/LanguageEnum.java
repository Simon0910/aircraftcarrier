package com.aircraftcarrier.framework.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Locale;
import java.util.Map;

/**
 * @author lzp
 */

@Getter
@AllArgsConstructor
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
    private static final Map<String, LanguageEnum> MAPPINGS = EnumUtil.initMapping(LanguageEnum.class);

    private final String code;
    private final String desc;

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
