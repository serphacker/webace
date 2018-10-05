package com.serphacker.webace.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serphacker.webace.WaHttpClient;
import com.serphacker.webace.WaHttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class HttpProxyIT {

    public final static ObjectMapper JSON = new ObjectMapper();

    String httpBinDomain = System.getProperty("httpBinDomain");
    String httpBinUrl = "http://" + httpBinDomain;

    @Test
    @EnabledIfSystemProperty(named = "test.service-backend", matches = "docker-compose")
    public void httpProxy() throws IOException {
        String squidIp = "172.29.1.2";
        WaHttpResponse response;
        var client = new WaHttpClient();

        client.setProxy(new HttpProxy(squidIp, 3128));
        response = client.doGet(httpBinUrl + "/ip");
        assertEquals(200, response.code());
        assertEquals(squidIp, JSON.readTree(response.text()).get("origin").asText());

        client.setProxy(DirectNoProxy.INSTANCE);
        response = client.doGet(httpBinUrl + "/ip");
        assertEquals(200, response.code());
        assertNotEquals(squidIp, JSON.readTree(response.text()).get("origin").asText());
    }

    @Test
    @EnabledIfSystemProperty(named = "test.service-backend", matches = "docker-compose")
    public void httpProxyAuth() throws IOException {
        String squidAuthIp = "172.29.1.4";
        WaHttpResponse response;
        var client = new WaHttpClient();

        client.setProxy(new HttpProxy(squidAuthIp, 3128));
        response = client.doGet(httpBinUrl + "/ip");
        assertEquals(407, response.code());

        client.setProxy(new HttpProxy(squidAuthIp, 3128, "user", "pass"));
        response = client.doGet(httpBinUrl + "/ip");
        assertEquals(200, response.code());
        assertEquals(squidAuthIp, JSON.readTree(response.text()).get("origin").asText());

        client.setProxy(DirectNoProxy.INSTANCE);
        response = client.doGet(httpBinUrl + "/ip");
        assertEquals(200, response.code());
        assertNotEquals(squidAuthIp, JSON.readTree(response.text()).get("origin").asText());
    }

}