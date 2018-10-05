package com.serphacker.webace.proxy;

import com.serphacker.webace.WaHttpClient;
import com.serphacker.webace.WaHttpResponse;
import org.apache.hc.core5.http.config.SocketConfig;
import org.apache.hc.core5.http.impl.BasicEndpointDetails;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.impl.bootstrap.ServerBootstrap;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.apache.hc.core5.io.ShutdownType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BindProxyIT {

    final static int SERVER_PORT = 8989;
    final static String SERVER_URL = "http://localhost:" + SERVER_PORT;
    static HttpServer SERVER;

    @BeforeAll
    public static void beforeAll() throws IOException {
        SERVER = ServerBootstrap.bootstrap()
            .setListenerPort(SERVER_PORT)
            .setSocketConfig(SocketConfig.custom().setSoReuseAddress(true).build())
            .register("/", (request, response, context) -> {
                response.setCode(200);
                BasicEndpointDetails endpoint = (BasicEndpointDetails) context.getAttribute(HttpCoreContext.CONNECTION_ENDPOINT);
                String clientAddress = ((InetSocketAddress) endpoint.getRemoteAddress()).getHostString();
                response.setEntity(new StringEntity(clientAddress));
            })
            .create();
        SERVER.start();
    }

    @AfterAll
    public static void afterAll() {
        SERVER.shutdown(ShutdownType.IMMEDIATE);
        SERVER.close();
    }

    @Test
    public void bindProxy() throws IOException {
        WaHttpResponse response;
        var cli = new WaHttpClient();

        String[] interfaces = new String[]{"127.0.0.2", "127.0.0.3"};
        response = cli.doGet(SERVER_URL);
        assertEquals("127.0.0.1", response.text());

        for (String aInterface : interfaces) {
            cli.setProxy(new BindProxy(aInterface));
            response = cli.doGet(SERVER_URL);
            assertEquals(aInterface, response.text());
        }

        cli.setProxy(DirectNoProxy.INSTANCE);
        response = cli.doGet(SERVER_URL);
        assertEquals("127.0.0.1", response.text());

        cli.close();
    }

}