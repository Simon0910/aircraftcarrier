package com.aircraftcarrier.framework.tookit.completablefuture;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

/**
 * @author meituan
 */
public class ExceptionUtil {

    private ExceptionUtil() {
    }

    /**
     * 提取真正的异常
     *
     * @param throwable throwable
     * @return Throwable
     */
    public static Throwable extractRealException(Throwable throwable) {
        //这里判断异常类型是否为CompletionException、ExecutionException，如果是则进行提取，否则直接返回。
        if (isSpecialException(throwable) && throwable.getCause() != null) {
            return throwable.getCause();
        }
        return throwable;
    }

    private static boolean isSpecialException(Throwable throwable) {
        return throwable instanceof CompletionException || throwable instanceof ExecutionException;
    }
}