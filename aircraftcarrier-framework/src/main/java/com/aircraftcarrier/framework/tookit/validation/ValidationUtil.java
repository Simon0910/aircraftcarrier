package com.aircraftcarrier.framework.tookit.validation;

import org.hibernate.validator.HibernateValidator;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.util.Set;

/**
 * @author lzp
 */
public class ValidationUtil {
    /**
     * 开启快速结束模式 failFast (true)
     */
    private static final Validator VALIDATOR = Validation.byProvider(HibernateValidator.class)
            .configure()
            .failFast(false)
            .buildValidatorFactory()
            .getValidator();

    /**
     * 私有
     */
    private ValidationUtil() {
    }

    /**
     * 校验对象
     *
     * @param t bean
     * @return ValidResult
     */
    public static <T> ValidResult validateBean(T t) {
        return validateBean(t, Default.class);
    }

    /**
     * 校验对象
     *
     * @param t      bean
     * @param groups 校验组
     * @return ValidResult
     */
    public static <T> ValidResult validateBean(T t, Class<?>... groups) {
        ValidResult result = new ValidResult();
        Set<ConstraintViolation<Object>> violationSet = VALIDATOR.validate(t, groups);
        result.setHasErrors(!violationSet.isEmpty());
        if (result.hasErrors()) {
            for (ConstraintViolation<Object> violation : violationSet) {
                result.addError(violation.getPropertyPath().toString(), violation.getMessage());
            }
        }
        return result;
    }

    /**
     * 校验bean的某一个属性
     *
     * @param obj          bean
     * @param propertyName 属性名称
     * @return ValidResult
     */
    public static <T> ValidResult validateProperty(T obj, String propertyName) {
        ValidResult result = new ValidResult();
        Set<ConstraintViolation<T>> violationSet = VALIDATOR.validateProperty(obj, propertyName);
        result.setHasErrors(!violationSet.isEmpty());
        if (result.hasErrors()) {
            for (ConstraintViolation<T> violation : violationSet) {
                result.addError(propertyName, violation.getMessage());
            }
        }
        return result;
    }


}
