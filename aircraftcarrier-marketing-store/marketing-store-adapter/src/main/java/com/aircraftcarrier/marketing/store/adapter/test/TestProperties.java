package com.aircraftcarrier.marketing.store.adapter.test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 类注释内容
 *
 * @author zhipengliu
 * @date 2023/5/12
 * @since 1.0
 */
@Configuration
public class TestProperties {

    private static String CLUSTER_NAME;
    /**
     * 静态变量 （private防止修改）
     */
    @Value("${ES.CLUSTER_NAME}")
    private void setClusterName(String clusterName) {
        if (CLUSTER_NAME != null) {
            return;
        }
        CLUSTER_NAME = clusterName;
    }

    @Value("${receive.emails}")
    private String emails;

    @Value("${receive.emails}")
    private String[] emailArr1;

    @Value("${receive.emails}")
    private List<String> emailList1;

    @Value("${receive.emails}")
    private Set<String> emailSet1;

    @Value("#{'${receive.emails}'.split(',')}")
    private List<String> emailList2;

    @Value("#{'${receive.emails}'.split(',')}")
    private Set<String> emailSet2;

    @Value("#{${dept.pin.map1}}")
    private Map<String, String> map1;

    @Value("#{${dept.pin.map2}}")
    private Map<String, Integer> map2;


    // ===========yml===========================

    @Value("${test.receive.emails}")
    private String emails2;

    @Value("${test.receive.emails}")
    private String[] emailArr2;

    @Value("${test.receive.emails}")
    private List<String> emailList3;

    @Value("${test.receive.emails}")
    private Set<String> emailSet3;

    @Value("#{'${test.receive.emails}'.split(',')}")
    private List<String> emailList4;

    @Value("#{'${test.receive.emails}'.split(',')}")
    private Set<String> emailSet4;

    @Value("#{${test.dept.pin.map1}}")
    private Map<String, String> map11;

    @Value("#{${test.dept.pin.map2}}")
    private Map<String, Integer> map22;


    public void test() {
        // debug
        System.out.println();
    }

}
