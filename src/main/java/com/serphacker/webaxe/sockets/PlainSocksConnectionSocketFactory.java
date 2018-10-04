package com.serphacker.webaxe.sockets;

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
    public Socket createSocket(final HttpContext context) throws IOException {
        InetSocketAddress socksaddr = (InetSocketAddress) context.getAttribute("proxy.socks");

        if (socksaddr != null) {
            return new Socket(new Proxy(Proxy.Type.SOCKS, socksaddr));
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
