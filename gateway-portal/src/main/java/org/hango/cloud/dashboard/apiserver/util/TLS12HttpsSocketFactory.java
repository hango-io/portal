package org.hango.cloud.dashboard.apiserver.util;

import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class TLS12HttpsSocketFactory implements SecureProtocolSocketFactory {

    private final SecureProtocolSocketFactory base;

    public TLS12HttpsSocketFactory() {
        base = new EasySSLProtocolSocketFactory();
    }

    private Socket acceptOnlyTLS12(Socket socket) {
        if (!(socket instanceof SSLSocket)) return socket;
        SSLSocket sslSocket = (SSLSocket) socket;
        sslSocket.setEnabledProtocols(new String[]{"TLSv1.2"});
        return sslSocket;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return acceptOnlyTLS12(base.createSocket(host, port));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localAddress, int localPort) throws IOException {
        return acceptOnlyTLS12(base.createSocket(host, port, localAddress, localPort));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localAddress, int localPort, HttpConnectionParams params) throws IOException {
        return acceptOnlyTLS12(base.createSocket(host, port, localAddress, localPort, params));
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        return acceptOnlyTLS12(base.createSocket(socket, host, port, autoClose));
    }

}
