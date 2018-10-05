/*
 * WebAce - Java Http Client for webscraping https://gitlab.com/serphacker/webace
 *
 * Copyright (c) 2018 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

package com.serphacker.webace;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serphacker.webace.proxy.BindProxy;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class WaHttpClientProxyIT {

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
        final WaHttpResponse response = client.doGet("http://localhost/ip.php");
        System.out.println(response.text());

        client.setProxy(new BindProxy("127.0.0.2"));
        System.out.println(client.doGet("http://localhost/ip.php").text());
    }

}