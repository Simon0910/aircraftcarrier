package com.aircraftcarrier.framework.security.core.aop;

import com.aircraftcarrier.framework.exception.FrameworkException;
import com.aircraftcarrier.framework.security.core.annotations.PreAuthenticated;
import com.aircraftcarrier.framework.security.core.util.SecurityFrameworkUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;


/**
 * @author lzp
 */
@Aspect
@Slf4j
public class PreAuthenticatedAspect {

    @Around("@annotation(preAuthenticated)")
    public Object around(ProceedingJoinPoint joinPoint, PreAuthenticated preAuthenticated) throws Throwable {
        if (SecurityFrameworkUtil.getLoginUser() == null) {
            throw new FrameworkException("获取用户异常");
        }
        return joinPoint.proceed();
    }

}
