/*
 * WebAce - Java Http Client for webscraping https://gitlab.com/serphacker/webace
 *
 * Copyright (c) 2018 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

package com.serphacker.webace.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serphacker.webace.WaHttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WaProxyIT {

    public final static ObjectMapper JSON = new ObjectMapper();

    String httpBinDomain = System.getProperty("httpBinDomain");
    String httpBinUrl = "http://" + httpBinDomain;

    @Test
    @EnabledIfSystemProperty(named = "test.service-backend", matches = "docker-compose")
    public void switchProxy() throws IOException {
        String defaultIp = "172.29.0.1";
        String httpProxy1 = "172.29.1.2";
        String socksProxy1 = "172.29.1.3";

        var client = new WaHttpClient();

        assertEquals(defaultIp, JSON.readTree(client.doGet(httpBinUrl + "/ip").text()).get("origin").asText());

        client.setProxy(new SocksProxy(socksProxy1, 1080));
        assertEquals(socksProxy1, JSON.readTree(client.doGet(httpBinUrl + "/ip").text()).get("origin").asText());

        client.setProxy(DirectNoProxy.INSTANCE);
        assertEquals(defaultIp, JSON.readTree(client.doGet(httpBinUrl + "/ip").text()).get("origin").asText());

        client.setProxy(new SocksProxy(socksProxy1, 1080));
        assertEquals(socksProxy1, JSON.readTree(client.doGet(httpBinUrl + "/ip").text()).get("origin").asText());

        client.setProxy(new SocksProxy(socksProxy1, 1080));
        assertEquals(socksProxy1, JSON.readTree(client.doGet(httpBinUrl + "/ip").text()).get("origin").asText());

        client.setProxy(new HttpProxy(httpProxy1, 3128));
        assertEquals(httpProxy1, JSON.readTree(client.doGet(httpBinUrl + "/ip").text()).get("origin").asText());

        client.setProxy(new SocksProxy(socksProxy1, 1080));
        assertEquals(socksProxy1, JSON.readTree(client.doGet(httpBinUrl + "/ip").text()).get("origin").asText());

        client.setProxy(new HttpProxy(httpProxy1, 3128));
        assertEquals(httpProxy1, JSON.readTree(client.doGet(httpBinUrl + "/ip").text()).get("origin").asText());

        client.setProxy(DirectNoProxy.INSTANCE);
        assertEquals(defaultIp, JSON.readTree(client.doGet(httpBinUrl + "/ip").text()).get("origin").asText());
    }


}