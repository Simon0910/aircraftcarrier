package com.aircraftcarrier.marketing.store.adapter.test;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * yml先注入 properties后注入
 * map和自定义对象会覆盖或追加， list和set直接替换
 *
 * @author zhipengliu
 * @date 2023/5/12
 * @since 1.0
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties("test")
public class ConfigurationTestProperties {

    private List<String> list;

    private Set<String> set;

    public Map<String, String> map;

    private Aa aa = new Aa();

    public void test() {
        System.out.println();
    }

    @Getter
    @Setter
    public class Aa {
        private Bb bb = new Bb();
    }

    @Getter
    @Setter
    public class Bb {
        private String b1;
        private String b2;
        private String b3;
    }
}
