package com.aircraftcarrier.framework.core;


import com.aircraftcarrier.framework.tookit.Log;

import java.util.function.Supplier;

/**
 * AbstractLogger
 *
 * @author zhipengliu
 * @since 1.0
 */
public class AbstractLogger implements Logger {

    public static Supplier<String> getToJsonSupplier(Object obj) {
        return Log.getToJsonSupplier(obj);
    }

    public static Supplier<Object> getSupplier(Object obj) {
        return Log.getSupplier(obj);
    }

    public static Supplier<Throwable> getExceptionSupplier(Throwable throwable) {
        return Log.getExceptionSupplier(throwable);
    }


    @Override
    public void error(String msg) {

    }

    @Override
    public void error(String format, Object arg) {

    }

    @Override
    public void error(String format, Object arg1, Object arg2) {

    }

    @Override
    public void error(String format, Object... arguments) {

    }

    @Override
    public void error(String msg, Throwable t) {

    }

    @Override
    public void info(String msg) {

    }

    @Override
    public void info(String format, Object arg) {

    }

    @Override
    public void info(String format, Object arg1, Object arg2) {

    }

    @Override
    public void info(String format, Object... arguments) {

    }

    @Override
    public void warn(String msg) {

    }

    @Override
    public void warn(String format, Object arg) {

    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {

    }

    @Override
    public void warn(String format, Object... arguments) {

    }

    @Override
    public void debug(String msg) {

    }

    @Override
    public void debug(String format, Object arg) {

    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {

    }

    @Override
    public void debug(String format, Object... arguments) {

    }

}
