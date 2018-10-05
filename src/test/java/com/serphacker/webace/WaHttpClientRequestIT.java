/*
 * WebAce - Java Http Client for webscraping https://gitlab.com/serphacker/webace
 *
 * Copyright (c) 2018 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

package com.serphacker.webace;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serphacker.webace.requests.FormBody;
import com.serphacker.webace.requests.JsonBody;
import com.serphacker.webace.requests.MultiPartBody;
import com.serphacker.webace.requests.PostBodyEntity;
import org.apache.hc.client5.http.entity.mime.InputStreamBody;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class WaHttpClientRequestIT {

    @FunctionalInterface
    public interface TestRequest {
        WaHttpResponse apply(String uri);
    }

    @FunctionalInterface
    public interface TestRequestWithHeaders {
        WaHttpResponse apply(String uri, List<Header> headers);
    }

    @FunctionalInterface
    public interface TestBodyRequest {
        WaHttpResponse apply(String uri, PostBodyEntity entity);
    }

    @FunctionalInterface
    public interface TestBodyRequestWithHeaders {
        WaHttpResponse apply(String uri, PostBodyEntity entity, List<Header> headers);
    }

    public final static ObjectMapper JSON = new ObjectMapper();

    String httpBinDomain = System.getProperty("httpBinDomain");
    String httpBinUrl = "http://" + httpBinDomain;

    @BeforeAll
    public static void beforeAll() {

        if (System.getProperty("httpBinDomain") == null) {
            throw new IllegalStateException("httpBinDomain not initialized (use -DhttpBinDomain=hostname)");
        }

    }

    @Test
    public void doGet() {
        WaHttpClient client = new WaHttpClient();
        final WaHttpResponse response = client.doGet(httpBinUrl + "/get");
        assertEquals(200, response.code());
        assertTrue(response.text().length() > 0);
    }

    public static Stream<Arguments> requestSupplier() {
        WaHttpClient client = new WaHttpClient();
        return Stream.of(
            Arguments.of("GET", (TestRequest)client::doGet),
            Arguments.of("DELETE", (TestRequest)client::doDelete)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("requestSupplier")
    void resquest(String method, TestRequest request) throws Exception {
        assertRequest(method, null, null, request.apply(httpBinUrl + "/anything"));
    }


    public static Stream<Arguments> requestWithHeadersSupplier() {
        WaHttpClient client = new WaHttpClient();
        return Stream.of(
            Arguments.of("GET", (TestRequestWithHeaders)client::doGet),
            Arguments.of("DELETE", (TestRequestWithHeaders)client::doDelete)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("requestWithHeadersSupplier")
    void requestWithHeaders(String method, TestRequestWithHeaders request) throws Exception {
        List<Header> headers = List.of(
            new BasicHeader("X-Header-1", "value-1"),
            new BasicHeader("X-Header-2", "value-2")
        );
        assertRequest(method, headers, null, request.apply(httpBinUrl + "/anything", headers));
    }

    public static Stream<Arguments> bodyRequestSupplier() throws Exception {
        WaHttpClient client = new WaHttpClient();

        return Stream.of(
            Arguments.of("POST", null, (TestBodyRequest)client::doPost),
            Arguments.of("POST", createFormBody(), (TestBodyRequest)client::doPost),
            Arguments.of("POST", createJsonBody(), (TestBodyRequest)client::doPost),
            Arguments.of("POST", createMultiPartBody(), (TestBodyRequest)client::doPost),
            Arguments.of("PUT", null, (TestBodyRequest)client::doPut),
            Arguments.of("PUT", createFormBody(), (TestBodyRequest)client::doPut),
            Arguments.of("PUT", createJsonBody(), (TestBodyRequest)client::doPut),
            Arguments.of("PUT", createMultiPartBody(), (TestBodyRequest)client::doPut),
            Arguments.of("PATCH", null, (TestBodyRequest)client::doPatch),
            Arguments.of("PATCH", createFormBody(), (TestBodyRequest)client::doPatch),
            Arguments.of("PATCH", createJsonBody(), (TestBodyRequest)client::doPatch),
            Arguments.of("PATCH", createMultiPartBody(), (TestBodyRequest)client::doPatch)
        );
    }

    public static FormBody createFormBody() {
        return FormBody.of(StandardCharsets.UTF_8, "p1", "p1v1", "p2", "p2v1", "p2", "p2v2");
    }

    public static JsonBody createJsonBody() throws Exception {
        return JsonBody.of(Map.of("k1", "v1", "k2", Map.of("k2.k1", "v1")));
    }

    public static MultiPartBody createMultiPartBody() {
        return MultiPartBody.builder(StandardCharsets.UTF_8)
            .add("form-key", "form-value")
            .add("binary-key", "binary-value".getBytes())
            .add("file-key", new InputStreamBody(
                new ByteArrayInputStream("file-value".getBytes()),
                "file-name"
            ))
            .build();
    }

    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("bodyRequestSupplier")
    void bodyRequest(String method, PostBodyEntity entity, TestBodyRequest request) throws Exception {
        assertRequest(method, null, entity, request.apply(httpBinUrl + "/anything", entity));
    }

    void assertRequest(String method, List<Header> headers, PostBodyEntity entity, WaHttpResponse response) throws Exception {

        assertEquals(200, response.code());
        final JsonNode jsonResponse = JSON.readTree(response.text());
        assertEquals(method, jsonResponse.get("method").asText());

        if(entity == null) {
            assertEquals(0, jsonResponse.get("data").asText().length());
            assertEquals(0, jsonResponse.get("args").size());
            assertEquals(0, jsonResponse.get("files").size());
            assertEquals(0, jsonResponse.get("form").size());
        } else if (entity.getClass() == FormBody.class) {
            assertFormBody(jsonResponse);
        } else if (entity.getClass() == JsonBody.class) {
            assertJsonBody(jsonResponse);
        } else if (entity.getClass() == MultiPartBody.class) {
            assertMultiPartBody(jsonResponse);
        } else {
            fail();
        }

        if(headers != null) {
            for (var header: headers) {
                assertEquals(
                    header.getValue(),
                    jsonResponse.at("/headers/" + header.getName()).asText(),
                    () -> "incorrect header " + header.getName() + " from response\n" + response.text()
                );
            }
        }

    }

    void assertFormBody(JsonNode jsonResponse) {
        assertEquals("application/x-www-form-urlencoded; charset=UTF-8", jsonResponse.at("/headers/Content-Type").asText());
        assertEquals("p1v1", jsonResponse.at("/form/p1").asText());
        assertEquals("p2v1", jsonResponse.at("/form/p2/0").asText());
        assertEquals("p2v2", jsonResponse.at("/form/p2/1").asText());
    }

    void assertJsonBody(JsonNode jsonResponse) throws IOException {
        assertEquals("application/json; charset=UTF-8", jsonResponse.at("/headers/Content-Type").asText());
        final JsonNode postedJson = JSON.readTree(jsonResponse.at("/data").asText());
        assertEquals("v1", postedJson.at("/k1").asText());
        assertEquals("v1", postedJson.at("/k2/k2.k1").asText());
    }

    void assertMultiPartBody(JsonNode jsonResponse) {
        assertTrue(jsonResponse.at("/headers/Content-Type").asText().contains("multipart/form-data"));
        assertTrue(jsonResponse.at("/headers/Content-Type").asText().contains("charset=UTF-8"));
        assertEquals("form-value", jsonResponse.at("/form/form-key").asText());
        assertEquals("binary-value", jsonResponse.at("/form/binary-key").asText());
        assertEquals("file-value", jsonResponse.at("/files/file-key").asText());
    }

}