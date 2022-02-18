package com.aircraftcarrier.marketing.store.domain.drools;

import lombok.extern.slf4j.Slf4j;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.springframework.stereotype.Component;

/**
 * @author lzp
 */
@Slf4j
@Component
public class KieTemplate {

    private static KieContainer kieContainer;

    public KieTemplate(KieContainer kieContainer) {
        KieTemplate.kieContainer = kieContainer;
    }

    public static KieContainer getKieContainer() {
        return KieTemplate.kieContainer;
    }

    public static void resetKieContainer(KieContainer newKieContainer) {
        KieTemplate.kieContainer = newKieContainer;
    }


    public void execute(Object obj) {
        StatelessKieSession statelessKieSession = kieContainer.newStatelessKieSession();
        statelessKieSession.execute(obj);
    }

    public void fireAllRules(Object obj) {
        KieSession kieSession = kieContainer.newKieSession();
        kieSession.insert(obj);
        kieSession.fireAllRules();
        kieSession.dispose();
    }
}
