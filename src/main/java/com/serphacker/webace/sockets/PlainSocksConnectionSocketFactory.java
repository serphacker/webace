/*
 * WebAce - Java Http Client for webscraping https://gitlab.com/serphacker/webace
 *
 * Copyright (c) 2018 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

package com.serphacker.webace.sockets;

import com.serphacker.webace.WaHttpContexts;
import com.serphacker.webace.proxy.SocksProxy;
import org.apache.hc.client5.http.ConnectTimeoutException;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class PlainSocksConnectionSocketFactory implements ConnectionSocketFactory {

    @Override
    public Socket createSocket(final HttpContext context) {
        final Object proxy = context.getAttribute(WaHttpContexts.WEBAXE_PROXY);

        if (proxy instanceof SocksProxy) {
            final var socksProxy = (SocksProxy) proxy;
            final var address = InetSocketAddress.createUnresolved(socksProxy.getIp(), socksProxy.getPort());
            return new Socket(new Proxy(Proxy.Type.SOCKS, address));
        } else {
            return new Socket();
        }
    }

    @Override
    public Socket connectSocket(
        TimeValue connectTimeout,
        Socket socket,
        HttpHost host,
        InetSocketAddress remoteAddress,
        InetSocketAddress localAddress,
        HttpContext context) throws IOException {

        Socket sock;
        if (socket != null) {
            sock = socket;
        } else {
            sock = createSocket(context);
        }
        if (localAddress != null) {
            sock.bind(localAddress);
        }
        try {
            sock.connect(remoteAddress, connectTimeout.toMillisIntBound());
        } catch (SocketTimeoutException ex) {
            throw new ConnectTimeoutException(ex, host, remoteAddress.getAddress());
        }
        return sock;
    }

}
