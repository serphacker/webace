package com.serphacker.webace.routes;

import com.serphacker.webace.WaHttpContexts;
import com.serphacker.webace.proxy.*;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.RouteInfo;
import org.apache.hc.client5.http.routing.HttpRoutePlanner;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class WaRoutePlanner implements HttpRoutePlanner {

    WaRoutes routes;

    public WaRoutePlanner(WaRoutes routes) {
        this.routes = routes;
    }

    @Override
    public HttpRoute determineRoute(HttpHost target, HttpContext context) throws HttpException {
        String routeHostname = routes.get(target.getHostName(), target.getHostName());
        String routeScheme = target.getSchemeName();
        int routePort = target.getPort();
        if(routePort == -1) {
            switch(routeScheme) {
                case "http":
                    routePort = 80;
                    break;
                case "https":
                    routePort = 443;
                    break;
                default:
                    routePort = 80;
            }
        }
        HttpHost routeHost = new HttpHost(routeHostname, routePort, routeScheme);

        WaProxy proxy = null;
        if(context != null) {
            proxy = (WaProxy)context.getAttribute(WaHttpContexts.WEBAXE_PROXY);
        }

        if(proxy == null || proxy == DirectNoProxy.INSTANCE || proxy instanceof SocksProxy) {
            return new HttpRoute(routeHost);
        }

        boolean ssl = "https".equalsIgnoreCase(target.getSchemeName());;

        if (proxy instanceof BindProxy) {
            BindProxy bindProxy = (BindProxy) proxy;
            try {
                return new HttpRoute(target, InetAddress.getByName(bindProxy.ip), ssl);
            } catch (UnknownHostException cause) {
                throw new HttpException("invalid bind ip", cause);
            }
        }

        if (proxy instanceof HttpProxy) {
            HttpProxy httpProxy = (HttpProxy) proxy;

            return new HttpRoute(
                target,
                null,
                new HttpHost(httpProxy.getIp(), httpProxy.getPort()),
                ssl,
                ssl ? RouteInfo.TunnelType.TUNNELLED : RouteInfo.TunnelType.PLAIN,
                ssl ? RouteInfo.LayerType.LAYERED : RouteInfo.LayerType.PLAIN
            );
        }

        throw new UnsupportedOperationException("unsupported proxy type : " + proxy);
    }

}
