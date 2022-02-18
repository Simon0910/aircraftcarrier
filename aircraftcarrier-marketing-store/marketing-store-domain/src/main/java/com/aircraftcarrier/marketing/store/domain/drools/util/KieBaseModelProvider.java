package com.aircraftcarrier.marketing.store.domain.drools.util;

import org.kie.api.KieBaseConfiguration;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.conf.KieBaseOption;

import java.util.Optional;

/**
 * Basic provider class for KieBaseModel instances.
 */
public interface KieBaseModelProvider {
    KieBaseModel getKieBaseModel(KieModuleModel kieModuleModel);

    KieBaseConfiguration getKieBaseConfiguration();

    void setAdditionalKieBaseOptions(KieBaseOption... options);

    boolean isIdentity();

    boolean isStreamMode();

    Optional<Class<? extends KieBuilder.ProjectType>> getExecutableModelProjectClass();

    boolean useAlphaNetworkCompiler();
}