package com.aircraftcarrier.framework.web.client;

import com.aircraftcarrier.framework.web.client.model.GetTokenRequestSchema;
import com.aircraftcarrier.framework.web.client.model.GetTokenResponseSchema;
import com.aircraftcarrier.framework.web.config.FrameworkWebAutoConfiguration;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.UUID;

@Slf4j
public class RestTemplateTest {

    @Test
    public void restTest() throws ApiException {
        FrameworkWebAutoConfiguration config = new FrameworkWebAutoConfiguration();
        MastercardRestTemplate restTemplate = config.mastercardRestTemplate();

        GetTokenRequestSchema requestSchema = new GetTokenRequestSchema();
        requestSchema.requestId(UUID.randomUUID().toString())
                // responseHost
                .responseHost("pre-tr.cjdfintech.com/master")
                // paymentAppInstanceId
                .paymentAppInstanceId("iYBVQzoZIuwVARezwT3spA--")
                // tokenUniqueReference
                .tokenUniqueReference("DCJDMC00001441364f3c9db87c6742c5af5754a73e07ed8f");

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        ApiResponse<GetTokenResponseSchema> apiResponse = restTemplate.exchange(
                // url
                "https://mtf.services.mastercard.com/mtf/mdes/digitization/1/0/getToken",
                // queryParams
                null,
                // requestSchema
                requestSchema,
                // httpHeaders
                httpHeaders,
                // HttpMethod
                HttpMethod.POST,
                // ResponseSchema
                GetTokenResponseSchema.class);

        System.out.println(" ==> " + JSON.toJSONString(apiResponse));

    }
}
