package org.ga4gh.dataset.cli.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.ga4gh.dataset.cli.exception.InvalidHttpStatusException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;

//@Slf4j
public class HttpUtils {

    private static OkHttpClient createAuthenticatedClient() {
        String username = ConfigUtil.getUserConfig().getUsername();
        String password = ConfigUtil.getUserConfig().getPassword();
        // build client with authentication information.
        OkHttpClient httpClient = new OkHttpClient.Builder().authenticator((route, response) -> {
            String credential = Credentials.basic(username, password);
            return response.request().newBuilder().header("Authorization", credential).build();
        }).build();
        return httpClient;
    }


    public static String get(String url) {

//        log.debug("GET "+url);
        Request request = new Request.Builder().url(url).build();

        OkHttpClient client = HttpUtils.createAuthenticatedClient();
        ObjectMapper objectMapper = new ObjectMapper();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new InvalidHttpStatusException("Server returned unexpected status "+response.code(), response.code());
            }
            String json = response.body().string();
          //  log.debug("Response JSON: "+json);
            return json;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static String get(String url, String accessToken) {

//        log.debug("GET "+url);
        Request request = new Request.Builder().url(url).header("Authorization", "Bearer " + accessToken).build();

        OkHttpClient client = HttpUtils.createAuthenticatedClient();
        ObjectMapper objectMapper = new ObjectMapper();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new InvalidHttpStatusException("Server returned unexpected status "+response.code(), response.code());
            }
            String json = response.body().string();
            //  log.debug("Response JSON: "+json);
            return json;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static String post(String url, String requestBody) {
//        log.debug("POST to "+url + " with REQUEST BODY " + requestBody);
        //RequestBody body = RequestBody.create(requestBody, MediaType.parse("application/json"));
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), requestBody);
        Request request = new Request.Builder().url(url).post(body).build();

        OkHttpClient client = HttpUtils.createAuthenticatedClient();
        ObjectMapper objectMapper = new ObjectMapper();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new InvalidHttpStatusException("Server returned unexpected status "+response.code(), response.code());
            }
            String json = response.body().string();
            //  log.debug("Response JSON: "+json);
            return json;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static String post(String url, String requestBody, String accessToken) {
//        log.debug("POST to "+url + " with REQUEST BODY " + requestBody);
        //RequestBody body = RequestBody.create(requestBody, MediaType.parse("application/json"));
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), requestBody);
        Request request = new Request.Builder().url(url).post(body).header("Authorization", "Bearer " + accessToken).build();

        OkHttpClient client = HttpUtils.createAuthenticatedClient();
        ObjectMapper objectMapper = new ObjectMapper();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new InvalidHttpStatusException("Server returned unexpected status "+response.code(), response.code());
            }
            String json = response.body().string();
            //  log.debug("Response JSON: "+json);
            return json;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static <T> T postAs(String url, String requestBody, Class<T> clazz) {
        try {
            String json = post(url, requestBody);

            ObjectMapper om = new ObjectMapper();

            return om.readValue(json, clazz);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static <T> T postAs(String url, String requestBody, Class<T> clazz, String accessToken) {
        try {
            String json = accessToken == null ? post(url, requestBody) : post(url, requestBody, accessToken);

            ObjectMapper om = new ObjectMapper();

            return om.readValue(json, clazz);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static <T> T getAs(String url, Class<T> clazz) {
        try {
            String json = get(url);

            ObjectMapper om = new ObjectMapper();

            return om.readValue(json, clazz);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static <T> T getAs(String url, Class<T> clazz, String accessToken) {
        try {
            String json = accessToken == null ? get(url) : get(url, accessToken);

            ObjectMapper om = new ObjectMapper();

            return om.readValue(json, clazz);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static <T> T getAs(String url, TypeReference<T> typeReference) {
        try {
            String json = get(url);
            ObjectMapper om = new ObjectMapper();

            return om.readValue(json, typeReference);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

}
