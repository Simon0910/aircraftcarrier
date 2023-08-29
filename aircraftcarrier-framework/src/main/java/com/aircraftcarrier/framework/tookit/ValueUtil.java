package com.aircraftcarrier.framework.tookit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

/**
 * ValueUtil
 *
 * @author lzp
 */
@Slf4j
public class ValueUtil {

    /**
     * ValueUtil
     */
    private ValueUtil() {
    }


    public static String getValue(String key) {
        return ResourceHolder.getEnvironment().getProperty(key);
    }

    private static class ResourceHolder {
        private static Environment environment; // This will be lazily initialised

        private static Environment getEnvironment() {
            if (ResourceHolder.environment == null) {
                ResourceHolder.environment = ApplicationContextUtil.getApplicationContext().getEnvironment();
            }
            return ResourceHolder.environment;
        }
    }
}