package com.aircraftcarrier.framework.support.validation;

import com.aircraftcarrier.framework.enums.IEnum;
import com.aircraftcarrier.framework.tookit.MapUtil;
import com.aircraftcarrier.framework.tookit.StringPool;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collections;
import java.util.Map;

/**
 * @author lzp
 */
public class InEnumValidator implements ConstraintValidator<InEnum, Object> {

    private Map<Object, String> mappings;

    @Override
    public void initialize(InEnum annotation) {
        IEnum<?>[] enumConstants = annotation.value().getEnumConstants();
        if (enumConstants.length == 0) {
            this.mappings = Collections.emptyMap();
        } else {
            mappings = MapUtil.newHashMap(enumConstants.length);
            for (IEnum<?> enumConstant : enumConstants) {
                mappings.put(enumConstant.code(), StringPool.EMPTY);
            }
        }
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        // 为空时，默认不校验，即认为通过
        if (value == null) {
            return true;
        }
        // 校验通过
        if (mappings.get(value) != null) {
            return true;
        }
        // 校验不通过，自定义提示语句（因为，注解上的 value 是枚举类，无法获得枚举类的实际值）
        context.disableDefaultConstraintViolation(); // 禁用默认的 message 的值
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate()
                // 重新添加错误提示语句
                .replaceAll("\\{value}", mappings.keySet().toString())).addConstraintViolation();
        return false;
    }

}

