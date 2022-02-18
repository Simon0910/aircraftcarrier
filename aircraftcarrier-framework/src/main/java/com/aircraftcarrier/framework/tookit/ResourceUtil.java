package com.aircraftcarrier.framework.tookit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * ResourceUtil
 *
 * @author lzp
 */
@Slf4j
public class ResourceUtil {
    /**
     * ResourceUtil
     */
    private ResourceUtil() {
    }

    public static String getClassPathResource(String classPath) {
        if (StringUtils.isBlank(classPath)) {
            return StringPool.EMPTY;
        }

        try {
            Resource resource = new ClassPathResource(classPath);
            InputStream inputStream = resource.getInputStream();

            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String content;
            while ((content = br.readLine()) != null) {
                sb.append(content);
            }
            br.close();
            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
            log.error("read class path resource error", e);
        }

        return StringPool.EMPTY;
    }
}
