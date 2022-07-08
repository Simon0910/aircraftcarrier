package com.aircraftcarrier.framework.web.client;

import com.aircraftcarrier.framework.exception.SysException;
import com.aircraftcarrier.framework.support.trace.TraceIdUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author liuzhipeng
 * @since 2020-01-15 10:13
 */
@Slf4j
public class MastercardRestTemplate extends AbstractRestTemplate {

    public MastercardRestTemplate(RestTemplate restTemplate) {
        super(restTemplate);
        this.restTemplate = restTemplate;
    }

    public <T> ApiResponse<T> exchange(String url, List<Pair> queryParams, Object localVarPostBody, HttpHeaders httpHeaders, HttpMethod httpMethod, Class<T> responseType, Object... uriVariables) throws ApiException {
        checkParams(httpMethod, responseType, uriVariables);
        appendHeaders(httpHeaders);
        final String requestUrl = buildUrl(url, queryParams);
        String requestBody;
        HttpEntity<?> httpEntity;
        switch (httpMethod) {
            case POST:
            case PUT:
                requestBody = JSON.toJSONString(localVarPostBody);
                httpEntity = new HttpEntity<>(requestBody, httpHeaders);
                break;
            case GET:
                httpEntity = new HttpEntity<>(null, httpHeaders);
                break;
            default:
                throw new ApiException("HttpMethod \"" + httpMethod + "\" is not supported");
        }
        ResponseEntity<T> response = restTemplate.exchange(requestUrl, httpMethod, httpEntity, responseType, uriVariables);
        return handleResponse(response, responseType);
    }

    private <T> void checkParams(HttpMethod httpMethod, Class<T> responseType, Object... uriVariables) throws ApiException {
        if (Objects.isNull(responseType)) {
            // the returnType is not defined,
            throw new ApiException("responseType can not be null");
        }
        if (httpMethod == null) {
            throw new ApiException("httpMethod can not be null");
        }
        if (uriVariables == null) {
            throw new ApiException("uriVariables can not be null");
        }
    }

    private void appendHeaders(HttpHeaders httpHeaders) {
        if (httpHeaders == null) {
            httpHeaders = new HttpHeaders();
        }
        String uuidFor32Bit = TraceIdUtil.generatorTraceId();
        httpHeaders.set("x-request-id", uuidFor32Bit);
        MediaType contentType = httpHeaders.getContentType();
        // ensuring a default content type
        if (contentType == null) {
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        }
    }

    private String buildUrl(String url, List<Pair> queryParams) {
        if (queryParams == null) {
            return url;
        }

        final StringBuilder sb = new StringBuilder();
        sb.append(url);

        if (!queryParams.isEmpty()) {
            // support (constant) query string in `path`, e.g. "/posts?draft=1"
            String prefix = url.contains("?") ? "&" : "?";
            for (Pair param : queryParams) {
                if (param.getValue() != null) {
                    if (prefix != null) {
                        sb.append(prefix);
                        prefix = null;
                    } else {
                        sb.append("&");
                    }
                    String value = parameterToString(param.getValue());
                    sb.append(escapeString(param.getName())).append("=").append(escapeString(value));
                }
            }
        }
        return sb.toString();
    }


    /**
     * Format the given parameter object into string.
     *
     * @param param Parameter
     * @return String representation of the parameter
     */
    private String parameterToString(Object param) {
        if (param == null) {
            return "";
        } else if (param instanceof Date || param instanceof OffsetDateTime || param instanceof LocalDate) {
            //Serialize to json string and remove the " enclosing characters
            String jsonStr = JSON.toJSONString(param);
            return jsonStr.substring(1, jsonStr.length() - 1);
        } else if (param instanceof Collection) {
            StringBuilder b = new StringBuilder();
            for (Object o : (Collection) param) {
                if (b.length() > 0) {
                    b.append(",");
                }
                b.append(o);
            }
            return b.toString();
        } else {
            return String.valueOf(param);
        }
    }

    /**
     * Escape the given string to be used as URL query value.
     *
     * @param str String to be escaped
     * @return Escaped string
     */
    private String escapeString(String str) {
        try {
            return URLEncoder.encode(str, "utf8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            return str;
        }
    }

    private <T> ApiResponse<T> handleResponse(ResponseEntity<T> response, Class<T> responseType) throws ApiException {
        if (response == null) {
            return new ApiResponse<>(9999, "fail", null, null);
        }
        HttpStatus statusCode = response.getStatusCode();
        Map<String, List<String>> headers = convertHttpHeaders(response.getHeaders());
        if (statusCode.is2xxSuccessful()) {
            if (HttpStatus.NO_CONTENT == statusCode) {
                // the status code is 204 (No Content)
                // or body is null
                return new ApiResponse<>(response.getStatusCodeValue(), statusCode.toString(), headers, response.getBody());
            }
            T data = serialize(response, responseType);
            return new ApiResponse<>(response.getStatusCodeValue(), statusCode.toString(), headers, data);
        }
        return new ApiResponse<>(response.getStatusCodeValue(), statusCode.toString(), headers, response.getBody());
    }

    private <T> T serialize(ResponseEntity<T> response, Class<T> responseType) throws ApiException {
        if (response == null || responseType == null) {
            log.warn("response or responseType is null!");
            return null;
        }
        T body = response.getBody();
        if (body == null) {
            log.warn("body is null!");
            return null;
        }
        if ("byte[]".equals(responseType.toString())) {
            // Handle binary response (byte array).
            return body;
        } else if (File.class.equals(responseType)) {
            // Handle file downloading.
            return downloadFileFromResponse(response);
        }
        String respBody = String.valueOf(body);
        if ("".equals(respBody)) {
            return null;
        }

        MediaType contentType = response.getHeaders().getContentType();
        if (contentType == null) {
            throw new SysException("response contentType is null");
        }
        if (isJsonMime(contentType.toString())) {
            return body;
        } else if (String.class.equals(responseType)) {
            // Expecting string, return the raw response body.
            return body;
        } else {
            throw new ApiException("Content type \"" + contentType + "\" is not supported for type: " + responseType, response.getStatusCodeValue(), response.getHeaders(), respBody);
        }
    }

    /**
     * Check if the given MIME is a JSON MIME.
     * JSON MIME examples:
     * application/json
     * application/json; charset=UTF8
     * APPLICATION/JSON
     * application/vnd.company+json
     * "* / *" is also default to JSON
     *
     * @param mime MIME (Multipurpose Internet Mail Extensions)
     * @return True if the given MIME is JSON, false otherwise.
     */
    private boolean isJsonMime(String mime) {
        String jsonMime = "(?i)^(application/json|[^;/ \t]+/[^;/ \t]+[+]json)[ \t]*(;.*)?$";
        return mime != null && (mime.matches(jsonMime) || "*/*".equals(mime));
    }

    private <T> T downloadFileFromResponse(ResponseEntity<T> response) throws ApiException {
        try {
            return response.getBody();
        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

    private Map<String, List<String>> convertHttpHeaders(HttpHeaders headers) {
        Map<String, List<String>> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        if (headers == null) {
            return result;
        }
        Set<Map.Entry<String, List<String>>> entries = headers.entrySet();
        for (Map.Entry<String, List<String>> map : entries) {
            String name = map.getKey().toLowerCase(Locale.US);
            List<String> values = result.get(name);
            if (values == null) {
                result.put(name, map.getValue());
            } else {
                values.addAll(map.getValue());
                result.put(name, values);
            }
        }
        return result;
    }

}
