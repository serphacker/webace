package com.serphacker.webaxe.routes;

import org.apache.hc.core5.http.HttpHost;

import java.util.HashMap;
import java.util.Map;

public class WaRoutes {

    Map<String, String> routes = new HashMap<>();

    public String get(String destination, String defaultRoute) {
        return routes.getOrDefault(destination, defaultRoute);
    }

    public void set(String destination, String via) {
        routes.put(destination, via);
    }

    public void remove(String destination) {
        routes.remove(destination);
    }

    public void clear() {
        routes.clear();
    }
}
