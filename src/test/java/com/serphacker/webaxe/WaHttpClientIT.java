package com.serphacker.webaxe;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;
import org.apache.hc.core5.http.message.BasicHeader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WaHttpClientIT {

    public final static ObjectMapper JSON = new ObjectMapper();

    String httpBinDomain = System.getProperty("httpBinDomain");
    String httpBinUrl = "http://" + httpBinDomain;

    @BeforeAll
    public static void beforeAll() {

        if(System.getProperty("httpBinDomain") == null){
            throw new IllegalStateException("httpBinDomain not initialized (use -DhttpBinDomain=hostname)");
        }

    }

    @Test
    public void doGet() {
        WaHttpClient client = new WaHttpClient();
        final WaHttpResponse response = client.doGet(httpBinUrl + "/get");
        assertEquals(200, response.code());
        assertTrue(response.getContentAsString().length() > 0);
    }

    @Test
    public void userAgent() throws IOException {
        WaHttpClient client = new WaHttpClient();

        {
            final WaHttpResponse response = client.doGet(httpBinUrl + "/get");
            final JsonNode jsonResponse = JSON.readTree(response.getContentAsString());
            assertEquals(client.config().getUserAgent(), jsonResponse.at("/headers/User-Agent").asText());
        }

        {
            client.config().setUserAgent("bip bop");
            final WaHttpResponse response = client.doGet(httpBinUrl + "/get");
            final JsonNode jsonResponse = JSON.readTree(response.getContentAsString());
            assertEquals("bip bop", jsonResponse.at("/headers/User-Agent").asText());
        }

        {
            HttpGet req = new HttpGet(httpBinUrl + "/get");
            req.setHeader("user-agent", "xxxx");
            final WaHttpResponse response = client.doRequest(req);
            final JsonNode jsonResponse = JSON.readTree(response.getContentAsString());
            assertEquals("xxxx", jsonResponse.at("/headers/User-Agent").asText());
        }

        {
            final WaHttpResponse response = client.doGet(httpBinUrl + "/get");
            final JsonNode jsonResponse = JSON.readTree(response.getContentAsString());
            assertEquals("bip bop", jsonResponse.at("/headers/User-Agent").asText());
        }

        {
            client.config().setUserAgent(null);
            final WaHttpResponse response = client.doGet(httpBinUrl + "/get");
            final JsonNode jsonResponse = JSON.readTree(response.getContentAsString());
            assertEquals("", jsonResponse.at("/headers/User-Agent").asText());
        }

    }

    @Test
    public void headers() throws IOException {
        WaHttpResponse response;
        JsonNode jsonResponse;
        WaHttpClient client = new WaHttpClient();

        client.config().defaultHeaders().add("X-Header-1", "h1.v1");
        response = client.doGet(httpBinUrl + "/get");
        jsonResponse = JSON.readTree(response.getContentAsString());
        assertEquals("h1.v1", jsonResponse.at("/headers/X-Header-1").asText());

        client.config().defaultHeaders().add("X-Header-1", "h1.v2");
        client.config().defaultHeaders().add("X-Header-2", "h2.v1");
        response = client.doGet(httpBinUrl + "/get");
        jsonResponse = JSON.readTree(response.getContentAsString());
        assertEquals("h1.v1,h1.v2", jsonResponse.at("/headers/X-Header-1").asText());
        assertEquals("h2.v1", jsonResponse.at("/headers/X-Header-2").asText());

        HttpGet req = new HttpGet(httpBinUrl + "/get");
        req.addHeader(new BasicHeader("X-Header-3", "h3.v1"));
        response = client.doRequest(req);
        jsonResponse = JSON.readTree(response.getContentAsString());
        assertEquals("h1.v1,h1.v2", jsonResponse.at("/headers/X-Header-1").asText());
        assertEquals("h2.v1", jsonResponse.at("/headers/X-Header-2").asText());
        assertEquals("h3.v1", jsonResponse.at("/headers/X-Header-3").asText());

        client.config().defaultHeaders().clear();
        response = client.doGet(httpBinUrl + "/get");
        jsonResponse = JSON.readTree(response.getContentAsString());
        assertTrue(jsonResponse.at("/headers/X-Header-1").isMissingNode());
        assertTrue(jsonResponse.at("/headers/X-Header-2").isMissingNode());
        assertTrue(jsonResponse.at("/headers/X-Header-3").isMissingNode());
    }

    @Test
    public void cookies() throws Exception{
        WaHttpResponse response;
        JsonNode jsonResponse;
        WaHttpClient cli = new WaHttpClient();


        cli.doGet(httpBinUrl + "/cookies/set?testcookie1=value1");

        response = cli.doGet(httpBinUrl + "/cookies");
        jsonResponse = JSON.readTree(response.getContentAsString());
        assertEquals("value1", jsonResponse.at("/cookies/testcookie1").asText());

        response = cli.doGet(httpBinUrl + "/cookies");
        jsonResponse = JSON.readTree(response.getContentAsString());
        assertEquals("value1", jsonResponse.at("/cookies/testcookie1").asText());

        final List<Cookie> cookies = cli.cookies().list();
        assertEquals("testcookie1", cookies.get(0).getName());
        assertEquals("value1", cookies.get(0).getValue());

        cli.cookies().clear();
        assertTrue(cli.cookies().list().isEmpty());

        response = cli.doGet(httpBinUrl + "/cookies");
        jsonResponse = JSON.readTree(response.getContentAsString());
        assertTrue(jsonResponse.at("/cookies/testcookie1").isMissingNode());

        final BasicClientCookie cookie = new BasicClientCookie("forcedcookie", "forcedvalue");
        cookie.setDomain(httpBinDomain);
        cli.cookies().add(cookie);
        response = cli.doGet(httpBinUrl + "/cookies");
        jsonResponse = JSON.readTree(response.getContentAsString());
        assertEquals("forcedvalue", jsonResponse.at("/cookies/forcedcookie").asText());
    }



}