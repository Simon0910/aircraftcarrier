package com.aircraftcarrier.generator;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Architect
 *
 * @author lzp
 */
public class MainArchitect {
    private static final String outputDir = "/Users/zhipengliu/IdeaProjects/aircraftcarrier";
    private static final String projectName = "bpm";


    public static void main(String[] args) {

        final String mavenPath = "src/main/java/com/aircraftcarrier/" + projectName;
        final String commonProjectModule = "common";
        final String appProjectModule = "app";
        final String clientProjectModule = "client";
        final String controllerProjectModule = "adapter";
        final String domainProjectModule = "domain";
        final String infrastructureProjectModule = "infrastructure";
        final String start = "start";

        Properties p = new Properties();
        p.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        p.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, "");
        p.setProperty(Velocity.ENCODING_DEFAULT, StandardCharsets.UTF_8.name());
        p.setProperty(Velocity.INPUT_ENCODING, StandardCharsets.UTF_8.name());
        p.setProperty("file.resource.loader.unicode", "true");

        String parentProjectName = "aircraftcarrier-" + projectName;
        File file = new File(outputDir + "/" + parentProjectName);
        if (!file.exists()) {
            file.mkdirs();
        }
        VelocityEngine velocityEngine = new VelocityEngine(p);
        Template pTemplate = velocityEngine.getTemplate("/architect/project-pom.xml.vm", StandardCharsets.UTF_8.name());
        try (FileOutputStream fos = new FileOutputStream(outputDir + "/" + parentProjectName + "/pom.xml"); OutputStreamWriter ow = new OutputStreamWriter(fos, StandardCharsets.UTF_8); BufferedWriter writer = new BufferedWriter(ow)) {
            Map<String, Object> contextVar = new HashMap<>();
            contextVar.put("projectName", projectName);
            contextVar.put("projectName_U", projectName.toUpperCase());
            pTemplate.merge(new VelocityContext(contextVar), writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<String> modules = List.of(
                commonProjectModule,
                appProjectModule,
                clientProjectModule,
                controllerProjectModule,
                domainProjectModule,
                infrastructureProjectModule,
                start);
        for (String module : modules) {
            Map<String, Object> context = new HashMap<>();
            context.put("projectName", projectName);
            context.put("projectModuleName", projectName + "-" + module);
            context.put("projectModuleName_U", projectName.toUpperCase() + " " + module.toUpperCase());
            context.put("revision", "${revision}");
            Template template = velocityEngine.getTemplate("/architect/" + module + "-pom.xml.vm", StandardCharsets.UTF_8.name());
            file = new File(outputDir + "/" + parentProjectName + "/" + projectName + "-" + module + "/" + mavenPath + "/" + (start.equals(module) ? "" : module));
            if (!file.exists()) {
                file.mkdirs();
            }
            try (FileOutputStream fos = new FileOutputStream(outputDir + "/" + parentProjectName + "/" + projectName + "-" + module + "/pom.xml"); OutputStreamWriter ow = new OutputStreamWriter(fos, StandardCharsets.UTF_8); BufferedWriter writer = new BufferedWriter(ow)) {
                template.merge(new VelocityContext(context), writer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
