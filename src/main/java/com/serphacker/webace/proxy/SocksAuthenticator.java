/*
 * WebAce - Java Http Client for webscraping https://gitlab.com/serphacker/webace
 *
 * Copyright (c) 2018 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

package com.serphacker.webace.proxy;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SocksAuthenticator extends Authenticator {

    public final static SocksAuthenticator INSTANCE = new SocksAuthenticator();

    static {
        Authenticator.setDefault(INSTANCE);
    }

    Map<String, PasswordAuthentication> credentials = new ConcurrentHashMap<>();

    private SocksAuthenticator() {
    }

    public void addSocksWithCredentials(SocksProxy proxy) {
        if (!proxy.hasCredentials()) {
            return;
        }

        credentials.put(
            proxy.getIp() + ":" + proxy.getPort(),
            new PasswordAuthentication(proxy.getUsername(), proxy.getPassword().toCharArray())
        );
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {

        String protocol = getRequestingProtocol();
        if (protocol == null || !protocol.toLowerCase().startsWith("socks")) {
            return super.getPasswordAuthentication();
        }

        String ip = getRequestingHost();
        int port = getRequestingPort();
        if (ip == null || ip.isEmpty()) {
            return super.getPasswordAuthentication();
        }

        PasswordAuthentication authInfo = credentials.get(ip + ":" + port);
        if (authInfo != null) {
            return authInfo;
        }

        return super.getPasswordAuthentication();
    }
}
