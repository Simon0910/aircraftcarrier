package com.aircraftcarrier.framework.tookit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ResourceUtil
 * 类似@Value功能
 *
 * @author lzp
 */
@Slf4j
public class ResourceUtil {

    /**
     * cache
     */
    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();

    /**
     * ResourceUtil
     */
    private ResourceUtil() {
    }

    public static String getClassPathResource(String classPath) {
        if (StringUtil.isBlank(classPath)) {
            return StringPool.EMPTY;
        }

        String resourceCache = CACHE.get(classPath);
        if (resourceCache != null) {
            return resourceCache;
        }

        synchronized (ResourceUtil.class) {
            resourceCache = CACHE.get(classPath);
            if (resourceCache != null) {
                return resourceCache;
            }

            String resourceString = StringPool.EMPTY;
            InputStream in = null;
            try {
                Resource resource = new ClassPathResource(classPath);
                in = resource.getInputStream();

                StringBuilder sb = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                String content;
                while ((content = br.readLine()) != null) {
                    sb.append(content);
                }
                br.close();
                resourceString = sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
                log.error("read class path resource error", e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            CACHE.put(classPath, resourceString);
            return resourceString;
        }
    }

    /**
     * 清楚缓存
     *
     * @param classPath classPath
     */
    public static void removeCache(String classPath) {
        CACHE.remove(classPath);
    }

    /**
     * 清楚缓存
     */
    public static void clearAllCache() {
        CACHE.clear();
    }
}
