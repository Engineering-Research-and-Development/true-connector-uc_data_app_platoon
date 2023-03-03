/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tecnalia.datausage.utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.FormBody.Builder;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.NotImplementedException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

/**
 *
 * @author 107126
 * 
 * This class builds up HTTP or HTTPS endpoint connections and sends GET requests.
 */
@Service
public class HttpUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);
    
    /**
     * Sends a GET request to an external HTTP endpoint
     *
     * @param address the URL.
     * @return the HTTP response if HTTP code is OK (200).
     * @throws URISyntaxException if the input address is not a valid URI.
     * @throws RuntimeException if an error occurred when connecting or processing the HTTP
     *                               request.
     */
    public String sendHttpGetRequest(String address) throws
        RuntimeException, URISyntaxException {
        OkHttpClient client = new OkHttpClient();
        try {
            Request request = new Request.Builder()
                .url(address)
                .build();

            Response response = client.newCall(request).execute();

            final var responseCodeOk = 200;
            final var responseCodeUnauthorized = 401;
            final var responseMalformed = -1;

            final var responseCode = response.code();

            if(responseCode == responseCodeOk){
                return Objects.requireNonNull(response.body()).string();
            } else if (responseCode == responseCodeUnauthorized) {
                // The request is not authorized.
                LOGGER.debug("Could not retrieve data. Unauthorized access. [url=({})]", address);
                throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
            } else if (responseCode == responseMalformed) {
                // The response code could not be read.
                LOGGER.debug("Could not retrieve data. Expectation failed. [url=({})]", address);
                throw new HttpClientErrorException(HttpStatus.EXPECTATION_FAILED);
            } else {
                // This function should never be thrown.
                LOGGER.warn("Could not retrieve data. Something else went wrong. [url=({})]", address);
                throw new NotImplementedException("Unsupported return value " +
                        "from getResponseCode.");
            }
        } catch (IOException exception) {
            // Catch all the HTTP, IOExceptions.
            LOGGER.warn("Failed to send the http get request. [url=({})]", address);
            LOGGER.warn("Failed to send the http get request. [exception=({})]", exception);
            throw new RuntimeException("Failed to send the http get request.", exception);
        }
    }


    /**
     * Sends a GET request with basic authentication to an external HTTP endpoint.
     *
     * @param address the URL.
     * @param username The username.
     * @param password The password.
     * @return The HTTP response when HTTP code is OK (200).
     * @throws URISyntaxException if the input address is not a valid URI.
     * @throws RuntimeException if an error occurred when connecting or processing the HTTP
     *                               request.
     */
    public String sendHttpGetRequestWithBasicAuth(String address, String username,
        String password) throws URISyntaxException, RuntimeException {
        String credential = Credentials.basic(username, password);
        OkHttpClient client = new OkHttpClient();
        try {
            Request request = new Request.Builder()
                .url(address)
                .header("Authorization", credential)
                .build();

            Response response = client.newCall(request).execute();
            if (response.code() < 200 || response.code() >= 300) {
                response.close();
                // Not the expected response code.
                LOGGER.debug("Could not retrieve data. Expectation failed. [url=({})]", address);
                throw new HttpClientErrorException(HttpStatus.EXPECTATION_FAILED);
            } else {
                return Objects.requireNonNull(response.body()).string();
            }
        } catch (IOException exception) {
            // Catch all the HTTP, IOExceptions.
            LOGGER.warn("Failed to send the http get request. [url=({})]", address);
            throw new RuntimeException("Failed to send the http get request.", exception);
        }
                
    }

    /**
     * Sends a POST request to an external HTTP endpoint
     *
     * @param address the URL.
     * @param params additional parameters
     * @param jsonData payload part
     * @return the HTTP response if HTTP code is OK (200).
     * @throws URISyntaxException if the input address is not a valid URI.
     * @throws RuntimeException if an error occurred when connecting or processing the HTTP
     *                               request.
     */
    public String sendHttpPostRequest(String address,  Map<String,String>params, String jsonData) throws
        RuntimeException, URISyntaxException {
        
        OkHttpClient client = new OkHttpClient();
        try {
            HttpUrl.Builder httpBuilder = HttpUrl.parse(address).newBuilder();
            if (params != null) {
               for(Map.Entry<String, String> param : params.entrySet()) {
                   httpBuilder.addQueryParameter(param.getKey(),param.getValue());
               }
            }            
            RequestBody body = RequestBody.create(jsonData, MediaType.parse("application/json"));
            Request request = new Request.Builder()
                .url(httpBuilder.build())
                .post(body)
                .build();
 
            Response response = client.newCall(request).execute();

            final var responseCodeOk = 200;
            final var responseCodeUnauthorized = 401;
            final var responseMalformed = -1;

            final var responseCode = response.code();

            if(responseCode == responseCodeOk){
                return Objects.requireNonNull(response.body()).string();
            } else if (responseCode == responseCodeUnauthorized) {
                // The request is not authorized.
                LOGGER.debug("Could not retrieve data. Unauthorized access. [url=({})]", address);
                throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
            } else if (responseCode == responseMalformed) {
                // The response code could not be read.
                LOGGER.debug("Could not retrieve data. Expectation failed. [url=({})]", address);
                throw new HttpClientErrorException(HttpStatus.EXPECTATION_FAILED);
            } else {
                // This function should never be thrown.
                LOGGER.warn("Could not retrieve data. Something else went wrong. [url=({})]", address);
                throw new NotImplementedException("Unsupported return value " +
                        "from getResponseCode.");
            }
        } catch (IOException exception) {
            // Catch all the HTTP, IOExceptions.
            LOGGER.warn("Failed to send the http get request. [url=({})]", address);
            throw new RuntimeException("Failed to send the http get request.", exception);
        }
    }

    /**
     * Sends a POST request to an external HTTP endpoint
     *
     * @param address the URL.
     * @param params additional parameters
     * @return the HTTP response if HTTP code is OK (200).
     * @throws URISyntaxException if the input address is not a valid URI.
     * @throws RuntimeException if an error occurred when connecting or processing the HTTP
     *                               request.
     */
    public String sendHttpPostRequestFormEncoded(String address, Map<String,String> params) throws
        RuntimeException, URISyntaxException {
        
        OkHttpClient client = new OkHttpClient();
        try {
            HttpUrl.Builder httpBuilder = HttpUrl.parse(address).newBuilder();
            
            FormBody formBody = null;
            Builder formBodyBuilder = new FormBody.Builder();
            if (params != null) {
                for(Map.Entry<String, String> param : params.entrySet()) {
                    formBodyBuilder.add(param.getKey(),param.getValue());
                }
            } 
            formBody = formBodyBuilder.build();
            
            Request request = new Request.Builder()
                .url(address)
                .post(formBody)
                .build();
 
            Response response = client.newCall(request).execute();

            final var responseCodeOk = 200;
            final var responseCodeUnauthorized = 401;
            final var responseMalformed = -1;

            final var responseCode = response.code();

            if(responseCode == responseCodeOk){
                return Objects.requireNonNull(response.body()).string();
            } else if (responseCode == responseCodeUnauthorized) {
                // The request is not authorized.
                LOGGER.debug("Could not retrieve data. Unauthorized access. [url=({})]", address);
                throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
            } else if (responseCode == responseMalformed) {
                // The response code could not be read.
                LOGGER.debug("Could not retrieve data. Expectation failed. [url=({})]", address);
                throw new HttpClientErrorException(HttpStatus.EXPECTATION_FAILED);
            } else {
                // This function should never be thrown.
                LOGGER.warn("Could not retrieve data. Something else went wrong. [url=({})]", address);
                throw new NotImplementedException("Unsupported return value " +
                        "from getResponseCode.");
            }
        } catch (IOException exception) {
            // Catch all the HTTP, IOExceptions.
            LOGGER.warn("Failed to send the http get request. [url=({})]", address);
            throw new RuntimeException("Failed to send the http get request.", exception);
        }
    }

    /**
     * Sends a POST request to an external HTTP endpoint
     *
     * @param address the URL.
     * @param params additional parameters
     * @param jsonData payload part
     * @param authServerUrl authorization server URL
     * @param authClientId authorization clientId
     * @param authClientSecret authorization clientSecret
     * @param authGrantType authorization grant type
     * @return the HTTP response if HTTP code is OK (200).
     * @throws URISyntaxException if the input address is not a valid URI.
     * @throws RuntimeException if an error occurred when connecting or processing the HTTP
     *                               request.
     */
    public String sendHttpPostRequestWithOAuth(String address,  Map<String,String>params, String jsonData,
            String authServerUrl, String authClientId, String authClientSecret, String authGrantType) throws
        RuntimeException, URISyntaxException {
        
        //Get access token from Auth. Server
        Map<String,String>authParams = new HashMap<String, String>();
        authParams.put("client_id", authClientId);
        authParams.put("client_secret", authClientSecret);
        authParams.put("grant_type", authGrantType);
        String accessTokenJsonStr = sendHttpPostRequestFormEncoded(authServerUrl, authParams);
        JSONObject accessTokenJson = new JSONObject(accessTokenJsonStr);
        String accessToken = accessTokenJson.getString("access_token");
        
        OkHttpClient client = new OkHttpClient();
        try {
            HttpUrl.Builder httpBuilder = HttpUrl.parse(address).newBuilder();
            if (params != null) {
               for(Map.Entry<String, String> param : params.entrySet()) {
                   httpBuilder.addQueryParameter(param.getKey(),param.getValue());
               }
            }            
            RequestBody body = RequestBody.create(jsonData, MediaType.parse("application/json"));
            Request request = new Request.Builder()
                .url(httpBuilder.build())
                .addHeader("Authorization", "Bearer " + accessToken)
                .post(body)
                .build();
 
            Response response = client.newCall(request).execute();

            final var responseCodeOk = 200;
            final var responseCodeUnauthorized = 401;
            final var responseMalformed = -1;

            final var responseCode = response.code();

            if(responseCode == responseCodeOk){
                return Objects.requireNonNull(response.body()).string();
            } else if (responseCode == responseCodeUnauthorized) {
                // The request is not authorized.
                LOGGER.debug("Could not retrieve data. Unauthorized access. [url=({})]", address);
                throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
            } else if (responseCode == responseMalformed) {
                // The response code could not be read.
                LOGGER.debug("Could not retrieve data. Expectation failed. [url=({})]", address);
                throw new HttpClientErrorException(HttpStatus.EXPECTATION_FAILED);
            } else {
                // This function should never be thrown.
                LOGGER.warn("Could not retrieve data. Something else went wrong. [url=({})]", address);
                throw new NotImplementedException("Unsupported return value " +
                        "from getResponseCode.");
            }
        } catch (IOException exception) {
            // Catch all the HTTP, IOExceptions.
            LOGGER.warn("Failed to send the http get request. [url=({})]", address);
            throw new RuntimeException("Failed to send the http get request.", exception);
        }
    }

}
