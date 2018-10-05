/*
 * WebAce - Java Http Client for webscraping https://gitlab.com/serphacker/webace
 *
 * Copyright (c) 2018 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

package com.serphacker.webace.proxy.utils;

import com.serphacker.webace.proxy.BindProxy;
import com.serphacker.webace.proxy.HttpProxy;
import com.serphacker.webace.proxy.SocksProxy;
import com.serphacker.webace.proxy.WaProxy;

import java.util.regex.Pattern;


public class ProxyParser {

    public final static Pattern PATTERN_IPV4 = Pattern.compile("([0-9])");

    static class Credentials {
        public final String login;
        public final String password;

        public Credentials(String login, String password) {
            this.login = login;
            this.password = password;
        }
    }

    static class ProxyScheme {
        public final Credentials credentials;
        public final String host;
        public final int port;

        public ProxyScheme(Credentials cred, String host, int port) {
            this.credentials = cred;
            this.host = host;
            this.port = port;
        }
    }

    public WaProxy parse(String line) {
        if (line != null && line.length() > 2) {

            if (line.charAt(line.length() - 1) == '/') {
                line = line.substring(0, line.length() - 1);
            }

            if (line.startsWith("http://")) {
                return parseHttpProxy(line);
            } else if (line.startsWith("socks://")) {
                return parseSocksProxy(line);
            } else if (line.startsWith("bind://")) {
                return parseBindProxy(line);
            }
        }
        return null;
    }

    HttpProxy parseHttpProxy(String line) {
        ProxyScheme scheme = parseProxyScheme(line.substring(7));
        HttpProxy proxy = new HttpProxy(scheme.host, scheme.port);
        if (scheme.credentials != null) {
            proxy.setUsername(scheme.credentials.login);
            proxy.setPassword(scheme.credentials.password);
        }
        return proxy;
    }

    SocksProxy parseSocksProxy(String line) {
        ProxyScheme scheme = parseProxyScheme(line.substring(8));
        SocksProxy proxy = new SocksProxy(scheme.host, scheme.port);
        if (scheme.credentials != null) {
            proxy.setUsername(scheme.credentials.login);
            proxy.setPassword(scheme.credentials.password);
        }
        return proxy;
    }

    BindProxy parseBindProxy(String line) {
        return new BindProxy(line.substring(7));
    }

    protected ProxyScheme parseProxyScheme(String line) {
        Credentials credentials = parseCredentials(line);
        int credSeparator = line.indexOf('@');
        if (credSeparator != -1) {
            line = line.substring(credSeparator + 1);
        }

        if (line.length() < 4) {
            return null;
        }

        int portSeparator = line.indexOf(":");
        if (portSeparator == -1 || portSeparator == line.length() - 1) {
            return null;
        }

        return new ProxyScheme(
            credentials,
            line.substring(0, portSeparator),
            Integer.parseInt(line.substring(portSeparator + 1))
        );
    }

    protected Credentials parseCredentials(String line) {
        int credSeparator = line.indexOf('@');
        if (credSeparator != -1) {
            int tupleDelimitor = line.indexOf(':');
            if (tupleDelimitor != -1 && tupleDelimitor < credSeparator) {
                return new Credentials(line.substring(0, tupleDelimitor), line.substring(tupleDelimitor + 1, credSeparator));
            }
        }
        return null;
    }

}
