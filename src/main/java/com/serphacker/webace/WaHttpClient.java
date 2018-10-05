package com.serphacker.webace;

import com.serphacker.webace.proxy.DirectNoProxy;
import com.serphacker.webace.proxy.WaProxy;
import com.serphacker.webace.requests.PostBodyEntity;
import com.serphacker.webace.routes.WaRoutePlanner;
import com.serphacker.webace.routes.WaRoutes;
import com.serphacker.webace.sockets.PlainSocksConnectionSocketFactory;
import com.serphacker.webace.sockets.SecureConnectionSocketFactory;
import org.apache.hc.client5.http.StandardMethods;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.config.SocketConfig;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class WaHttpClient implements Closeable {

    public final static Logger LOG = LoggerFactory.getLogger(WaHttpClient.class);

    WaCookieStore cookies = new WaCookieStore();
    WaHttpConfig config = new WaHttpConfig();
    WaRoutes routes = new WaRoutes();
    WaProxy proxy = DirectNoProxy.INSTANCE;
    WaProxy previousProxy = DirectNoProxy.INSTANCE;


    CloseableHttpClient client;
    BasicHttpClientConnectionManager connectionManager;
    PlainSocksConnectionSocketFactory plainSocketFactory = new PlainSocksConnectionSocketFactory();
    SecureConnectionSocketFactory secureSocketFactory = new SecureConnectionSocketFactory(plainSocketFactory);

    WaRoutePlanner routePlanner = new WaRoutePlanner(routes);

    public WaHttpClient() {

        connectionManager = new BasicHttpClientConnectionManager(
            RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", plainSocketFactory)
                .register("https", secureSocketFactory)
                .build()
        );
        connectionManager.setSocketConfig(SocketConfig.custom().setSoTimeout(Timeout.ofMillis(config.timeoutMilli)).build());

        client = HttpClients
            .custom()
            .setRoutePlanner(routePlanner)
            .setDefaultCredentialsProvider(new BasicCredentialsProvider())
            .setDefaultCookieStore(cookies.store)
            //.setConnectionReuseStrategy(this.new SCliConnectionReuseStrategy())
            .setConnectionManager(connectionManager)
            .build();

    }

    public WaCookieStore cookies() {
        return cookies;
    }

    public WaHttpConfig config() {
        return config;
    }

    public WaRoutes routes() {return routes;}

    public WaHttpResponse doGet(String uri) {
        return doGet(uri, null);
    }

    public WaHttpResponse doGet(String uri, List<Header> headers) {
        return doRequest(StandardMethods.GET.name(), uri, headers);
    }

    public WaHttpResponse doDelete(String uri) {
        return doDelete(uri, null);
    }

    public WaHttpResponse doDelete(String uri, List<Header> headers) {
        return doRequest(StandardMethods.DELETE.name(), uri, headers);
    }

    public WaHttpResponse doPost(String uri, PostBodyEntity body) {
        return doPost(uri, body, null);
    }

    public WaHttpResponse doPost(String uri, PostBodyEntity body, List<Header> headers) {
        return doRequest(StandardMethods.POST.name(), uri, body, headers);
    }

    public WaHttpResponse doPut(String uri, PostBodyEntity body) {
        return doPut(uri, body, null);
    }

    public WaHttpResponse doPut(String uri, PostBodyEntity body, List<Header> headers) {
        return doRequest(StandardMethods.PUT.name(), uri, body, headers);
    }

    public WaHttpResponse doPatch(String uri, PostBodyEntity body) {
        return doPatch(uri, body, null);
    }

    public WaHttpResponse doPatch(String uri, PostBodyEntity body, List<Header> headers) {
        return doRequest(StandardMethods.PATCH.name(), uri, body, headers);
    }

    public WaHttpResponse doRequest(String verb, String uri, List<Header> headers) {
        return doRequest(verb, uri, (HttpEntity) null, headers);
    }

    public WaHttpResponse doRequest(String verb, String uri, PostBodyEntity body, List<Header> headers) {
        return doRequest(verb, uri, body != null ? body.getHttpEntity() : null, headers);
    }

    public WaHttpResponse doRequest(String verb, String uri, HttpEntity body, List<Header> headers) {
        HttpUriRequestBase request = new HttpUriRequestBase(verb, URI.create(uri));

        if (body != null) {
            request.setEntity(body);
        }

        if (headers != null) {
            for (var requestHeader : headers) {
                request.addHeader(requestHeader);
            }
        }

        return doRequest(request);
    }

    public WaHttpResponse doRequest(ClassicHttpRequest request) {
        return doRequest(request, HttpClientContext.create());
    }

    public WaHttpResponse doRequest(ClassicHttpRequest request, HttpClientContext context) {
        WaHttpResponse response = new WaHttpResponse();

        reInitializeClient();
        initializeRequest(request, context);
        context.setAttribute(WaHttpContexts.WEBAXE_PROXY, proxy);

        try {
            response.executionTimerStart();
            response.setContext(context);
            try(CloseableHttpResponse httpResponse = client.execute(request, context)) {
                response.setHttpResponse(httpResponse);
                final byte[] content = consumeResponse(httpResponse);
                response.setContent(content);
            }
        } catch (IOException ex) {
            response.setException(ex);
        } finally {
            response.executionTimerStop();
        }

        return response;
    }

    protected void reInitializeClient() {
        connectionManager.setSocketConfig(SocketConfig.custom().setSoTimeout(Timeout.ofMillis(config.timeoutMilli)).build());
        secureSocketFactory.setTrustAllSsl(config.isTrustAllSsl());
        if(!proxy.equals(previousProxy)) {
            closeConnection();
        }
    }

    protected void initializeRequest(HttpRequest request, HttpClientContext context) {

        if (request.getFirstHeader(HttpHeaders.USER_AGENT) == null) {
            request.setHeader(HttpHeaders.USER_AGENT, config.getUserAgent());
        }

        for (Header requestHeader : config.defaultHeaders().list()) {
            request.addHeader(requestHeader);
        }

        RequestConfig.Builder configBuilder = RequestConfig.copy(context.getRequestConfig());
        configBuilder.setConnectionTimeout(Timeout.ofMillis(config.timeoutMilli));
        configBuilder.setConnectionRequestTimeout(Timeout.ofMillis(config.timeoutMilli));
        configBuilder.setMaxRedirects(config.maxRedirect);
        configBuilder.setRedirectsEnabled(config.maxRedirect > 0);
        RequestConfig config = configBuilder.build();

        context.setAttribute(HttpClientContext.REQUEST_CONFIG, config);
    }

    protected byte[] consumeResponse(CloseableHttpResponse response) throws IOException {
        try(HttpEntity entity = response.getEntity()) {
            long contentLength = entity.getContentLength();

            if (contentLength > config.getMaxResponseLength()) {
                throw new IOException("content length (" + contentLength + ") is greater than max length (" +
                    config.getMaxResponseLength() + ")");
            }

            byte[] buffer = new byte[config.getMaxResponseLength() + 1];
            InputStream stream = entity.getContent();
            int totalRead = 0;
            int read;

            while (totalRead <= config.getMaxResponseLength()
                && (read = stream.read(buffer, totalRead, config.getMaxResponseLength() + 1 - totalRead)) != -1) {
                totalRead += read;
            }

            if (totalRead > config.getMaxResponseLength()) {
                throw new IOException("response is too big, already read " + totalRead + " bytes");
            }

            return Arrays.copyOfRange(buffer, 0, totalRead);
        }
    }

    public void closeConnection() {
        connectionManager.closeIdle(0, TimeUnit.NANOSECONDS);
    }

    public WaProxy getProxy() {
        return proxy;
    }

    public void setProxy(WaProxy proxy) {
        if (proxy == null) {
            proxy = DirectNoProxy.INSTANCE;
        }
        this.previousProxy = this.proxy;
        this.proxy = proxy;

        // TODO: store credentials
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

}
