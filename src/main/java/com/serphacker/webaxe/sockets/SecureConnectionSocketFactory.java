package com.serphacker.webaxe.sockets;

import org.apache.hc.client5.http.socket.LayeredConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TextUtils;
import org.apache.hc.core5.util.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class SecureConnectionSocketFactory implements LayeredConnectionSocketFactory {

    private static final Logger LOG = LoggerFactory.getLogger(SecureConnectionSocketFactory.class);
    static SSLSocketFactory DEFAULT_SSL_SOCKET_FACTORY = null;
    static SSLSocketFactory TRUST_ALL_SSL_SOCKET_FACTORY = null;

    static {
        try {
            TRUST_ALL_SSL_SOCKET_FACTORY = SSLContexts.custom()
                .loadTrustMaterial(null, (X509Certificate[] chain, String authType) -> true)
                .build()
                .getSocketFactory();
            DEFAULT_SSL_SOCKET_FACTORY = SSLContexts.createDefault().getSocketFactory();
        } catch (Exception ex) {
            LOG.error("ex in ssl socket initialization", ex);
        }
    }

    public static final String TLS = "TLS";
    public static final String SSL = "SSL";
    public static final String SSLV2 = "SSLv2";

    private static String[] split(final String s) {
        if (TextUtils.isBlank(s)) {
            return null;
        }
        return s.split(" *, *");
    }

    private final PlainSocksConnectionSocketFactory plainConnectionSocketFactory;
    private final javax.net.ssl.SSLSocketFactory defaultSSLSocketFactory;
    private final javax.net.ssl.SSLSocketFactory trustAllSSLSocketfactory;
    private final HostnameVerifier defaultHostnameVerifier;
    private final String[] supportedProtocols;
    private final String[] supportedCipherSuites;
    private boolean trustAllSsl;

    public SecureConnectionSocketFactory(PlainSocksConnectionSocketFactory plainConnectionSocketFactory) {
        this(plainConnectionSocketFactory, false);
    }

    public SecureConnectionSocketFactory(PlainSocksConnectionSocketFactory plainConnectionSocketFactory, boolean trustAllSsl) {
        this(
            plainConnectionSocketFactory,
            DEFAULT_SSL_SOCKET_FACTORY,
            TRUST_ALL_SSL_SOCKET_FACTORY,
            null, null, trustAllSsl
        );
    }

    public SecureConnectionSocketFactory(
        PlainSocksConnectionSocketFactory plainConnectionSocketFactory,
        javax.net.ssl.SSLSocketFactory defaultSSLSocketFactory,
        javax.net.ssl.SSLSocketFactory trustAllSSLSocketfactory,
        String[] supportedProtocols,
        String[] supportedCipherSuites,
        boolean trustAllSsl
    ) {
        this.plainConnectionSocketFactory = plainConnectionSocketFactory;
        this.defaultSSLSocketFactory = defaultSSLSocketFactory;
        this.trustAllSSLSocketfactory = trustAllSSLSocketfactory;
        this.defaultHostnameVerifier = new DefaultHostnameVerifier();
        this.supportedProtocols = supportedProtocols;
        this.supportedCipherSuites = supportedCipherSuites;
        this.trustAllSsl = trustAllSsl;
    }

    /**
     * Performs any custom initialization for a newly created SSLSocket (before the SSL handshake happens).
     *
     * The default implementation is a no-op, but could be overridden to, e.g., call
     * {@link javax.net.ssl.SSLSocket#setEnabledCipherSuites(String[])}.
     *
     * @throws IOException may be thrown if overridden
     */
    protected void prepareSocket(final SSLSocket socket) throws IOException {
    }

    @Override
    public Socket createSocket(final HttpContext context) throws IOException {
        return plainConnectionSocketFactory.createSocket(context);
    }

    @Override
    public Socket connectSocket(
        final TimeValue connectTimeout,
        final Socket socket,
        final HttpHost host,
        final InetSocketAddress remoteAddress,
        final InetSocketAddress localAddress,
        final HttpContext context) throws IOException {
        Args.notNull(host, "HTTP host");
        Args.notNull(remoteAddress, "Remote address");
        final Socket sock = socket != null ? socket : createSocket(context);
        if (localAddress != null) {
            sock.bind(localAddress);
        }
        try {
            if (connectTimeout.toMillis() > 0 && sock.getSoTimeout() == 0) {
                sock.setSoTimeout(connectTimeout.toMillisIntBound());
            }
            LOG.debug("Connecting socket to {} with timeout {}",  remoteAddress, connectTimeout);

            sock.connect(remoteAddress, connectTimeout.toMillisIntBound());
        } catch (final IOException ex) {
            try {
                sock.close();
            } catch (final IOException ignore) {
            }
            throw ex;
        }
        // Setup SSL layering if necessary
        if (sock instanceof SSLSocket) {
            final SSLSocket sslsock = (SSLSocket) sock;
            LOG.debug("Starting handshake");
            sslsock.startHandshake();
            verifyHostname(sslsock, host.getHostName());
            return sock;
        } else {
            return createLayeredSocket(sock, host.getHostName(), remoteAddress.getPort(), context);
        }
    }

    @Override
    public Socket createLayeredSocket(
        final Socket socket,
        final String target,
        final int port,
        final HttpContext context) throws IOException {

        SSLSocketFactory sslSocketFactory = trustAllSsl ? trustAllSSLSocketfactory : defaultSSLSocketFactory;

        final SSLSocket sslsock = (SSLSocket) sslSocketFactory.createSocket(
            socket,
            target,
            port,
            true);

        if (supportedProtocols != null) {
            sslsock.setEnabledProtocols(supportedProtocols);
        } else {
            // If supported protocols are not explicitly set, remove all SSL protocol versions
            final String[] allProtocols = sslsock.getEnabledProtocols();
            final List<String> enabledProtocols = new ArrayList<String>(allProtocols.length);
            for (String protocol : allProtocols) {
                if (!protocol.startsWith("SSL")) {
                    enabledProtocols.add(protocol);
                }
            }
            if (!enabledProtocols.isEmpty()) {
                sslsock.setEnabledProtocols(enabledProtocols.toArray(new String[enabledProtocols.size()]));
            }
        }
        if (supportedCipherSuites != null) {
            sslsock.setEnabledCipherSuites(supportedCipherSuites);
        }

        LOG.debug("Enabled protocols: " + Arrays.asList(sslsock.getEnabledProtocols()));
        LOG.debug("Enabled cipher suites:" + Arrays.asList(sslsock.getEnabledCipherSuites()));

        prepareSocket(sslsock);
        LOG.debug("Starting handshake");
        sslsock.startHandshake();
        verifyHostname(sslsock, target);
        return sslsock;
    }

    private void verifyHostname(final SSLSocket sslsock, final String hostname) throws IOException {
        try {
            SSLSession session = sslsock.getSession();
            if (session == null) {
                // In our experience this only happens under IBM 1.4.x when
                // spurious (unrelated) certificates show up in the server'
                // chain.  Hopefully this will unearth the real problem:
                final InputStream in = sslsock.getInputStream();
                in.available();
                // If ssl.getInputStream().available() didn't cause an
                // exception, maybe at least now the session is available?
                session = sslsock.getSession();
                if (session == null) {
                    // If it's still null, probably a startHandshake() will
                    // unearth the real problem.
                    sslsock.startHandshake();
                    session = sslsock.getSession();
                }
            }
            if (session == null) {
                throw new SSLHandshakeException("SSL session not available");
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Secure session established");
                LOG.debug(" negotiated protocol: " + session.getProtocol());
                LOG.debug(" negotiated cipher suite: " + session.getCipherSuite());

                try {

                    final Certificate[] certs = session.getPeerCertificates();
                    final X509Certificate x509 = (X509Certificate) certs[0];
                    final X500Principal peer = x509.getSubjectX500Principal();

                    LOG.debug(" peer principal: " + peer.toString());
                    final Collection<List<?>> altNames1 = x509.getSubjectAlternativeNames();
                    if (altNames1 != null) {
                        final List<String> altNames = new ArrayList<String>();
                        for (final List<?> aC : altNames1) {
                            if (!aC.isEmpty()) {
                                altNames.add((String) aC.get(1));
                            }
                        }
                        LOG.debug(" peer alternative names: " + altNames);
                    }

                    final X500Principal issuer = x509.getIssuerX500Principal();
                    LOG.debug(" issuer principal: " + issuer.toString());
                    final Collection<List<?>> altNames2 = x509.getIssuerAlternativeNames();
                    if (altNames2 != null) {
                        final List<String> altNames = new ArrayList<String>();
                        for (final List<?> aC : altNames2) {
                            if (!aC.isEmpty()) {
                                altNames.add((String) aC.get(1));
                            }
                        }
                        LOG.debug(" issuer alternative names: " + altNames);
                    }
                } catch (Exception ignore) {
                }
            }

            if (!trustAllSsl && !defaultHostnameVerifier.verify(hostname, session)) {
                final Certificate[] certs = session.getPeerCertificates();
                final X509Certificate x509 = (X509Certificate) certs[0];
                final X500Principal x500Principal = x509.getSubjectX500Principal();
                throw new SSLPeerUnverifiedException("Host name '" + hostname + "' does not match "
                    + "the certificate subject provided by the peer (" + x500Principal.toString() + ")");
            }
            // verifyHostName() didn't blowup - good!
        } catch (final IOException iox) {
            // close the socket before re-throwing the exception
            try {
                sslsock.close();
            } catch (final Exception x) {
                /*ignore*/ }
            throw iox;
        }
    }

    public boolean isTrustAllSsl() {
        return trustAllSsl;
    }

    public void setTrustAllSsl(boolean trustAllSsl) {
        this.trustAllSsl = trustAllSsl;
    }


}
