package com.aircraftcarrier.marketing.store.adapter.config;

import com.aircraftcarrier.framework.model.response.Response;
import com.aircraftcarrier.framework.support.trace.TraceIdUtil;
import com.aircraftcarrier.framework.tookit.MessageUtil;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * <a href="https://segmentfault.com/a/1190000041900931">...</a>
 * <p>
 * 使用 @ControllerAdvice & ResponseBodyAdvice
 * 拦截Controller方法默认返回参数，统一处理返回值/响应体
 */
@ControllerAdvice
public class ResponseHandler implements ResponseBodyAdvice<Response> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        // HttpServletRequest request = sra.getRequest();
        return true;
    }

    @Override
    public Response beforeBodyWrite(Response responseBody,
                                    MethodParameter returnType,
                                    MediaType selectedContentType,
                                    Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                    ServerHttpRequest request,
                                    ServerHttpResponse response) {
        responseBody.setResponseId(TraceIdUtil.getTraceId());
        responseBody.setMsg(MessageUtil.getMessage(responseBody.getCode(), responseBody.getMsg()));
        return responseBody;
    }
}
