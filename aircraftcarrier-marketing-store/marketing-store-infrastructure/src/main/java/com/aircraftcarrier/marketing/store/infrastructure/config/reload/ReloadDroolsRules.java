package com.aircraftcarrier.marketing.store.infrastructure.config.reload;

import com.aircraftcarrier.marketing.store.domain.drools.FileUtil;
import com.aircraftcarrier.marketing.store.domain.drools.KieTemplate;
import com.aircraftcarrier.marketing.store.domain.drools.KieUtils;
import com.aircraftcarrier.marketing.store.domain.drools.Rule;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.internal.utils.KieHelper;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


/**
 * @author neo
 * @date 17/7/31
 */
@Component
public class ReloadDroolsRules {

    public static final String content = "package plausibcheck.adress\n\n rule \"Postcode 6 numbers\"\n\n    when\n  then\n        System.out.println(\"规则2中打印日志：校验通过!\");\n end";
    public KieServices kieServices = KieUtils.getKieServices();
    @javax.annotation.Resource
    private KieFileSystem kieFileSystem;

    /**
     * loadRulesFromDatabase
     *
     * @return List<Rule>
     */
    private List<Rule> loadRulesFromDatabase() {
        // 从数据库加载的规则
        Rule drlRule = new Rule();
        drlRule.setPath(drlRule.getPath());
        drlRule.setContent(content);
        return Arrays.asList(drlRule);
    }

    /**
     * for (Rule drlRule : loadRulesFromDatabase()) {
     * kieFileSystem.write(drlRule.getPath(), drlRule.getContent());
     * }
     *
     * @return
     * @throws IOException
     */
    public KieFileSystem kieFileSystem() throws IOException {
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        for (Resource fileResource : FileUtil.getFileResources()) {
            kieFileSystem.write(KieUtils.newClassPathResource(FileUtil.RULES_PATH + fileResource.getFilename()));
        }
        return kieFileSystem;
    }

    /**
     * 重新加载
     *
     * @throws IOException
     */
    public void reload() throws IOException {
        KieFileSystem kieFileSystem = kieFileSystem();

        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem).buildAll();
        Results results = kieBuilder.getResults();
        if (results.hasMessages(Message.Level.ERROR)) {
            System.out.println(results.getMessages());
            throw new IllegalStateException("### errors ###");
        }

        KieContainer kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());
        KieTemplate.resetKieContainer(kieContainer);
        System.out.println("reload新规则重载成功");
    }


    /**
     * 重新加载
     */
    public void reloadByHelper() {
        KieHelper kieHelper = new KieHelper();
        for (Rule drlRule : loadRulesFromDatabase()) {
            kieHelper.addContent(drlRule.getContent(), ResourceType.DRL);
        }

        Results results = kieHelper.verify();
        if (results.hasMessages(Message.Level.ERROR)) {
            System.out.println(results.getMessages());
            throw new IllegalStateException("### errors ###");
        }

        KieContainer kieContainer = kieHelper.getKieContainer();
        KieTemplate.resetKieContainer(kieContainer);
        System.out.println("reloadByHelper新规则重载成功");
    }


}
