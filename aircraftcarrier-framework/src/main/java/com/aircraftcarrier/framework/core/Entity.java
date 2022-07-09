package com.aircraftcarrier.framework.core;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Entity, Entity Object is prototype and is not thread-safe
 *
 * @author Frank Zhang
 * @date 2019-01-03 2:53 PM
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public @interface Entity {
}
