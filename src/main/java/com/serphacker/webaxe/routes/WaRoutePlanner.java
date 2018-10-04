package com.serphacker.webaxe.routes;

import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.routing.HttpRoutePlanner;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.HttpContext;

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

        return new HttpRoute(routeHost);
    }

}
