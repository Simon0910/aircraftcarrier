package com.aircraftcarrier.framework.tookit;

import com.aircraftcarrier.framework.exception.HttpException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * OkHttpUtil
 *
 * @author mcjohn
 * @date 2020/11/16
 */
public class OkHttpUtil {
    /**
     * MediaType
     */
    private static final MediaType HTTP_JSON = MediaType.get("application/json;charset=UTF-8");
    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OkHttpUtil.class);
    /**
     * OkHttpClient
     */
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            // connectTimeout
            .connectTimeout(10, TimeUnit.SECONDS)
            // writeTimeout
            .writeTimeout(10, TimeUnit.SECONDS)
            // readTimeout
            .readTimeout(10, TimeUnit.SECONDS)
            // build
            .build();

    /**
     * OkHttpUtil
     */
    private OkHttpUtil() {
    }

    /**
     * handleResponse
     *
     * @param response   response
     * @param returnType returnType
     * @param <T>        T
     * @return T
     * @throws HttpException HttpException
     */
    private static <T> T handleResponse(Response response, TypeReference<T> returnType) throws HttpException {
        if (response.isSuccessful()) {
            if (returnType == null || response.code() == 204) {
                // returning null if the returnType is not defined,
                // or the status code is 204 (No Content)
                if (response.body() != null) {
                    try {
                        response.body().close();
                    } catch (Exception e) {
                        throw new HttpException(response.message(), e, response.code(), response.headers().toMultimap());
                    }
                }
                return null;
            }

            return deserialize(response, returnType);

        } else {
            String respBody = null;
            if (response.body() != null) {
                try {
                    respBody = response.body().string();
                } catch (IOException e) {
                    throw new HttpException(response.message(), e, response.code(), response.headers().toMultimap());
                }
            }
            throw new HttpException(response.message(), response.code(), response.headers().toMultimap(), respBody);
        }

    }

    /**
     * deserialize
     *
     * @param response   response
     * @param returnType returnType
     * @param <T>        T
     * @return T
     * @throws HttpException HttpException
     */
    private static <T> T deserialize(Response response, TypeReference<T> returnType) throws HttpException {
        if (response == null || returnType == null) {
            return null;
        }

        String respBody;
        try {
            if (response.body() != null) {
                respBody = response.body().string();
            } else {
                respBody = null;
            }
        } catch (IOException e) {
            throw new HttpException(e);
        }

        if (respBody == null || "".equals(respBody)) {
            return null;
        }

        if (returnType.getType().equals(String.class)) {
            // Expecting string, return the raw response body.
            return (T) respBody;
        }

        String contentType = response.headers().get("Content-Type");
        if (contentType == null) {
            // ensuring a default content type
            contentType = "application/json";
        }
        if (isJsonMime(contentType)) {
            return JSON.parseObject(respBody, new TypeReference<T>() {
            });
        }
        throw new HttpException("Content type \"" + contentType + "\" is not supported for type: " + returnType, response.code(), response.headers().toMultimap(), respBody);
    }

    /**
     * isJsonMime
     *
     * @param mime mime
     * @return boolean
     */
    private static boolean isJsonMime(String mime) {
        String jsonMime = "(?i)^(application/json|[^;/ \t]+/[^;/ \t]+[+]json)[ \t]*(;.*)?$";
        return mime != null && (mime.matches(jsonMime) || mime.equals("*/*"));
    }

    /**
     * POST
     *
     * @param url     url
     * @param params  params
     * @param headers headers
     * @throws HttpException HttpException
     */
    public static void postVoid(String url, Object params, Map<String, String> headers) throws HttpException {
        post(url, headers, params, null);
    }

    /**
     * POST
     *
     * @param url          url
     * @param params       params
     * @param responseType responseType
     * @return String
     * @throws HttpException HttpException
     */
    public static <T> T post(String url, Object params, TypeReference<T> responseType) throws HttpException {
        Map<String, String> headers = new HashMap<>(2);
        headers.put("token", "token");
        return post(url, headers, params, responseType);
    }

    /**
     * POST
     *
     * @param url          url
     * @param headers      headers
     * @param params       params
     * @param responseType responseType
     * @return String
     * @throws HttpException HttpException
     */
    public static <T> T post(String url, Map<String, String> headers, Object params, TypeReference<T> responseType) throws HttpException {
        String jsonString = JSON.toJSONString(params);
        RequestBody requestBody = RequestBody.create(jsonString, HTTP_JSON);
        Request.Builder builder = new Request.Builder().url(url).post(requestBody)
                // content-type
                .addHeader("content-type", "application/json");

        if (MapUtils.isNotEmpty(headers)) {
            headers.forEach(builder::addHeader);
        }

        Call call = HTTP_CLIENT.newCall(builder.build());
        try {
            Response response = call.execute();
            return handleResponse(response, responseType);
        } catch (IOException e) {
            LOGGER.error("HTTP POST IOException: ", e);
            throw new HttpException(e);
        }
    }

    /**
     * GET
     *
     * @param url          url
     * @param params       params
     * @param responseType responseType
     * @param headers      headers
     * @return String
     * @throws HttpException HttpException
     */
    public static <T> T get(String url, Map<String, Object> params, TypeReference<T> responseType, Map<String, String> headers) throws HttpException {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(url);
        if (MapUtils.isNotEmpty(params)) {
            urlBuilder.append("?");
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                urlBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            urlBuilder.deleteCharAt(urlBuilder.length() - 1);
        }
        Request.Builder builder = new Request.Builder()
                // url
                .url(urlBuilder.toString())
                // method
                .method("GET", null)
                // Content-Type
                .addHeader("Content-Type", "application/json")
                // Connection
                .addHeader("Connection", "close");

        if (MapUtils.isNotEmpty(headers)) {
            headers.forEach(builder::addHeader);
        }

        Call call = HTTP_CLIENT.newCall(builder.build());
        try {
            Response response = call.execute();
            return handleResponse(response, responseType);
        } catch (IOException e) {
            LOGGER.error("HTTP GET IOException: ", e);
            throw new HttpException(e);
        }
    }


}
