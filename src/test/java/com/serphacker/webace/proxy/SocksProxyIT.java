package com.serphacker.webace.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serphacker.webace.WaHttpClient;
import com.serphacker.webace.WaHttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class SocksProxyIT {

    public final static ObjectMapper JSON = new ObjectMapper();

    String httpBinDomain = System.getProperty("httpBinDomain");
    String httpBinUrl = "http://" + httpBinDomain;
    String socksIp = "172.29.1.3";

    @Test
    @EnabledIfSystemProperty(named = "test.service-backend", matches = "docker-compose")
    public void socksProxy() throws IOException {
        WaHttpResponse response;
        var client = new WaHttpClient();

        client.setProxy(new SocksProxy(socksIp, 1080));
        response = client.doGet(httpBinUrl + "/ip");
        assertEquals(socksIp, JSON.readTree(response.text()).get("origin").asText());

        client.setProxy(DirectNoProxy.INSTANCE);
        response = client.doGet(httpBinUrl + "/ip");
        assertNotEquals(socksIp, JSON.readTree(response.text()).get("origin").asText());
    }

}