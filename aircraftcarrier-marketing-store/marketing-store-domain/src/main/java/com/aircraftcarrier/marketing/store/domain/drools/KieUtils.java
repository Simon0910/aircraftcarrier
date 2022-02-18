package com.aircraftcarrier.marketing.store.domain.drools;

import com.aircraftcarrier.marketing.store.domain.drools.util.KieBaseTestConfiguration;
import com.aircraftcarrier.marketing.store.domain.drools.util.KieUtil;
import lombok.extern.slf4j.Slf4j;
import org.drools.core.impl.KnowledgeBaseImpl;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.Message;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.Results;
import org.kie.api.definition.KiePackage;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderError;
import org.kie.internal.builder.KnowledgeBuilderErrors;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.utils.KieHelper;

import java.nio.charset.StandardCharsets;
import java.util.Collection;

/**
 * @author lzp
 */
@Slf4j
public class KieUtils {

    public static final String RESOURCES_FOLDER = "src/main/resources/";

    public static KieServices getKieServices() {
        return KieServices.Factory.get();
    }

    public static Resource getResourcesFromDrl(String drlStr) {
        return ResourceFactory.newByteArrayResource(drlStr.getBytes(StandardCharsets.UTF_8));
    }

    public static Resource newClassPathResource(String classPathResource) {
        return ResourceFactory.newClassPathResource(classPathResource, "UTF-8");
    }

    public static KieBase buildKieBase(String drlStr) {
        KieHelper kieHelper = new KieHelper();
        kieHelper.addContent(drlStr, ResourceType.DRL);
        Results results = kieHelper.verify();
        if (results.hasMessages(Message.Level.ERROR)) {
            System.out.println(results.getMessages());
            throw new IllegalStateException("### errors ###");
        }
        KieBase kieBase = kieHelper.build();
        return kieBase;
    }

    public static KieBase buildKieBaseFromClassPath(String classPathResource) {
        KieHelper kieHelper = new KieHelper();
        kieHelper.addResource(newClassPathResource(classPathResource), ResourceType.DRL);
        Results results = kieHelper.verify();
        if (results.hasMessages(Message.Level.ERROR)) {
            System.out.println(results.getMessages());
            throw new IllegalStateException("### errors ###");
        }
        KieBase kieBase = kieHelper.build();
        return kieBase;
    }

    public static void addRule(KnowledgeBaseImpl kieBase, String drlStr) {
        Resource resource = getResourcesFromDrl(drlStr);
        Collection<KiePackage> kiePackages = loadPackages(resource, ResourceType.DRL);
        kieBase.addPackages(kiePackages);
    }

    public static Collection<KiePackage> loadPackages(Resource res, ResourceType type) {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(res, type);
        KnowledgeBuilderErrors errors = kbuilder.getErrors();
        if (errors.size() > 0) {
            for (KnowledgeBuilderError error : errors) {
                System.err.println(error);
            }
            throw new IllegalArgumentException("Could not parse knowledge.");
        }
        Collection<KiePackage> knowledgePackages = kbuilder.getKnowledgePackages();
        return knowledgePackages;
    }

    private static void removeRule(KnowledgeBaseImpl kieBase, String packageName, String ruleName) {
        kieBase.removeRule(packageName, ruleName);
    }

    public static void updateToVersion(String... drlStr) {
        KieServices ks = getKieServices();

        // Create a new jar for version 1.1.0
        ReleaseId releaseId = ks.newReleaseId("org.default", "test-artifact", "1.0.0");
//        releaseId = ks.getRepository().getDefaultReleaseId();
        KieModule km = KieUtil.getKieModuleFromDrls(releaseId, KieBaseTestConfiguration.CLOUD_EQUALITY, drlStr);

        // try to update the container to version 1.1.0
        KieTemplate.getKieContainer().updateToVersion(releaseId);

        // create and use a new session
        //  ksession.insert( new Message( "Hello World" ) );
    }

}
