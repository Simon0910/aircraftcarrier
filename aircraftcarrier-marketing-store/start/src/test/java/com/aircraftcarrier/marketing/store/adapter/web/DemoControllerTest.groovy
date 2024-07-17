package com.aircraftcarrier.marketing.store.adapter.web

import com.aircraftcarrier.marketing.store.app.DemoServiceImpl
import com.aircraftcarrier.marketing.store.client.DemoService
import com.aircraftcarrier.marketing.store.client.demo.view.DemoVo
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Subject
import spock.lang.Title

/**
 * {@link com.aircraftcarrier.marketing.store.adapter.web.DemoController}
 */
@Title("A test to demonstrate the use of the SpringBean annotation using a ContextConfiguration")
@ContextConfiguration(classes = [DemoController, DemoServiceImpl])
class DemoControllerTest extends spock.lang.Specification {
    @SpringBean
    DemoService mockDemoService = Stub()

    @Autowired
    private DemoService demoService

    @Autowired
    @Subject
    DemoController demoController


    void setup() {}

    void cleanup() {}

    def "PageList"() {}

    def "Add"() {}

    def "Update"() {}

    def "GetById"() {
        def demoVo = new DemoVo()
        demoVo.setBizNo("aaa")

        given: "a stubbed response"
        mockDemoService.getById(_ as Serializable) >> demoVo

        when: "we fetch our data"
        def result = demoController.getById(1)

        then: "our SpringBean overrode the original dependency"
        result.success() && result.getData().getBizNo() == "aaa"
    }

    def "SelectList"() {}

    def "Delete"() {}

    def "Export"() {}

    def "ImportExcel"() {}
}
