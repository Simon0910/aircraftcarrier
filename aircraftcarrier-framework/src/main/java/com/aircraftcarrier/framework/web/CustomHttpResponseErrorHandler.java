package com.aircraftcarrier.framework.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

/**
 * @author liuzhipeng
 * @since 2020-01-15 10:13
 */
@Slf4j
public class CustomHttpResponseErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
        HttpStatus statusCode = clientHttpResponse.getStatusCode();
        log.info("hasError statusCode = " + statusCode);
        int unknownStatusCode = clientHttpResponse.getRawStatusCode();
        HttpStatus statusCode2 = HttpStatus.resolve(unknownStatusCode);
        return statusCode2 != null ? hasError(statusCode2) : hasError(unknownStatusCode);
    }

    private boolean hasError(int unknownStatusCode) {
        HttpStatus.Series series = HttpStatus.Series.resolve(unknownStatusCode);
        return (series == HttpStatus.Series.CLIENT_ERROR || series == HttpStatus.Series.SERVER_ERROR);
    }

    private boolean hasError(HttpStatus statusCode) {
        return statusCode.isError();
    }

    @Override
    public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
        // 默认处理非200的返回，会抛异常
        HttpStatus statusCode = clientHttpResponse.getStatusCode();
        log.info("handleError statusCode = " + statusCode);
        int unknownStatusCode = clientHttpResponse.getRawStatusCode();
        log.info("unknownStatusCode: {}", unknownStatusCode);
        HttpStatus statusCode2 = HttpStatus.resolve(unknownStatusCode);
        log.info("statusCode2: {}", statusCode2);
    }


}
