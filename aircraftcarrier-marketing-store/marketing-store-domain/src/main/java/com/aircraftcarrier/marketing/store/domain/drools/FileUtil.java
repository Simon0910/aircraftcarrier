package com.aircraftcarrier.marketing.store.domain.drools;

import org.kie.api.builder.ReleaseId;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author lzp
 */
public class FileUtil {

    public static final String RULES_PATH = "rules/";

    public static Resource[] getFileResources() throws IOException {
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        return resourcePatternResolver.getResources("classpath*:" + RULES_PATH + "**/*.drl");
    }

    public static File bytesToTempKJARFile(ReleaseId releaseId, byte[] bytes, String extension) {
        File file = new File(System.getProperty("java.io.tmpdir"), releaseId.getArtifactId() + "-" + releaseId.getVersion() + extension);
        try {
            new PrintWriter(file).close();
            FileOutputStream fos = new FileOutputStream(file, false);
            fos.write(bytes);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }
}
