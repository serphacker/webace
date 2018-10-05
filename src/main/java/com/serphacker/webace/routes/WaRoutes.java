/*
 * WebAce - Java Http Client for webscraping https://gitlab.com/serphacker/webace
 *
 * Copyright (c) 2018 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

package com.serphacker.webace.routes;

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
