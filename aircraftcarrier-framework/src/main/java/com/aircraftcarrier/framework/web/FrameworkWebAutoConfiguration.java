package com.aircraftcarrier.framework.web;

import com.aircraftcarrier.framework.exception.SysException;
import com.aircraftcarrier.framework.web.client.MastercardRestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;
import java.util.List;


/**
 * @author yudao
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({WebProperties.class})
public class FrameworkWebAutoConfiguration implements WebMvcConfigurer {

    /**
     * getMaxCpuCore
     *
     * @return cpu num
     */
    private static int getMaxCpuCore() {
        return Runtime.getRuntime().availableProcessors();
    }


    /**
     * buildMastercardClientHttpRequestFactory
     *
     * @return HttpComponentsClientHttpRequestFactory
     */
    private HttpComponentsClientHttpRequestFactory buildMastercardClientHttpRequestFactory() {
        SSLContext sslcontext;
        try {
            sslcontext = SSLUtil.getSSLContext("env/dev/jd-mtf-mastercard-keystore.jks", "JKS", "123456");
        } catch (Exception e) {
            log.error("SSL context init error: ", e);
            throw new SysException("SSL context init error...");
        }
        // Allow TLSv1 protocol only
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslcontext, new String[]{"TLSv1.2"}, null, NoopHostnameVerifier.INSTANCE);

        //设置协议http和https对应的处理socket链接工厂的对象
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                // register
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                // register
                .register("https", sslConnectionSocketFactory)
                // build
                .build();

        // 开始设置连接池
        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        // 最大连接数
        poolingHttpClientConnectionManager.setMaxTotal(2 * getMaxCpuCore() + 3);
        // 同路由并发数
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(2 * getMaxCpuCore());

        //创建自定义的httpclient对象
        CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(poolingHttpClientConnectionManager)
                // 重试次数3次，并开启
                .setRetryHandler(new DefaultHttpRequestRetryHandler(3, true))
                // 保持长链接配置，keep-alive
                .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
                // build
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpclient);
        // 链接超时配置 16秒
        requestFactory.setConnectTimeout(16000);
        // 连接读取超时配置
        requestFactory.setReadTimeout(16000);
        // 连接池不够用时候等待时间长度设置，分词那边 500毫秒 ，我们这边设置成3秒
        requestFactory.setConnectionRequestTimeout(3000);
        // 缓冲请求数据，POST大量数据，可以设定为true 我们这边机器比较内存较大
        requestFactory.setBufferRequestBody(true);
        return requestFactory;
    }

    @Bean
    public MastercardRestTemplate mastercardRestTemplate() {
        RestTemplate restTemplate = new RestTemplateBuilder()
                // errorHandler
                .errorHandler(new CustomHttpResponseErrorHandler())
                // interceptors
                .interceptors(new CustomClientHttpRequestInterceptor())
                // ClientHttpRequestFactory
                .requestFactory(this::buildMastercardClientHttpRequestFactory)
                // build
                .build();
        List<HttpMessageConverter<?>> httpMessageConverters = restTemplate.getMessageConverters();
        httpMessageConverters.forEach(httpMessageConverter -> {
            if (httpMessageConverter instanceof StringHttpMessageConverter) {
                StringHttpMessageConverter messageConverter = (StringHttpMessageConverter) httpMessageConverter;
                messageConverter.setDefaultCharset(StandardCharsets.UTF_8);
            }
        });
        return new MastercardRestTemplate(restTemplate);
    }


}
