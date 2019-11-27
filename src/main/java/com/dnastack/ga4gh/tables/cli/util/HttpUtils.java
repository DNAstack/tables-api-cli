package com.dnastack.ga4gh.tables.cli.util;

import com.dnastack.ga4gh.tables.cli.exception.InvalidHttpStatusException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtils {


    public static ObjectMapper getMapper() {
        return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    private static OkHttpClient createClient() {

        return new OkHttpClient.Builder().connectTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)
            .build();

    }

    private static String execute(Request request) {
        OkHttpClient client = createClient();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new InvalidHttpStatusException(
                    "Server returned unexpected status " + response.code(), response.code());
            }
            String json = response.body().string();
            //  log.debug("Response JSON: "+json);
            return json;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static String get(String url) {
        return get(url, null);
    }

    public static String get(String url, RequestAuthorization authorization) {

        Request.Builder requestBuilder = new Request.Builder().url(url);
        if (authorization != null && authorization.hasAuth()) {
            requestBuilder.header("Authorization", authorization.getAuthorizationHeader());
        }
        Request request = requestBuilder.build();
        return execute(request);
    }

    public static String post(String url, String requestBody) {
        return post(url, requestBody, null);
    }

    public static String post(String url, String requestBody, RequestAuthorization authorization) {
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), requestBody);
        Request.Builder requestBuilder = new Request.Builder().url(url).post(body);

        if (authorization != null && authorization.hasAuth()) {
            requestBuilder.header("Authorization", authorization.getAuthorizationHeader())
                .build();
        }

        return execute(requestBuilder.build());
    }

    public static <T> T postAs(String url, String requestBody, Class<T> clazz) {
        return postAs(url, requestBody, clazz, null);
    }

    public static <T> T postAs(String url, String requestBody, Class<T> clazz, RequestAuthorization authorization) {
        try {
            String json = post(url, requestBody, authorization);
            ObjectMapper om = getMapper();
            return om.readValue(json, clazz);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static <T> T getAs(String url, Class<T> clazz) {
        return getAs(url, clazz, null);
    }

    public static <T> T getAs(String url, Class<T> clazz, RequestAuthorization authorization) {
        try {
            String json = get(url, authorization);
            ObjectMapper om = getMapper();
            return om.readValue(json, clazz);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static <T> T getAs(String url, TypeReference<T> typeReference) {
        return getAs(url, typeReference, null);
    }

    public static <T> T getAs(String url, TypeReference<T> typeReference, RequestAuthorization authorization) {
        try {
            String json = get(url, authorization);
            ObjectMapper om = getMapper();
            return om.readValue(json, typeReference);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
