package com.utopiarealized.bugbounty.proxy;

import javax.net.ssl.*;
import java.io.*;
import java.net.IDN;
import java.net.Socket;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.lang.reflect.Field;
import java.util.Map;

import sun.security.ssl.SSLSocketImpl;


public class HttpsProxy implements Runnable {
    private static final String[] protocols = new String[]{"TLSv1.3"};
    private static final String[] cipher_suites = new String[]{"TLS_AES_128_GCM_SHA256"};


    private boolean running = true;

    private Thread ourThread;

    private SSLServerSocket serverSocket;

    private final HttpForwarder httpForwarder;

    private final HttpSpy httpSpy;


    public HttpsProxy(int port, String keystoreLocation, String keystorePass,
                      HttpForwarder forwarder,
                      HttpSpy httpSpy) throws IOException, GeneralSecurityException {
        this.httpSpy = httpSpy;

        this.httpForwarder = forwarder;

        //TODO: Refactor this.
        KeyStore ks = null;
        try {
            ks = KeyStore.getInstance("JKS");
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        FileInputStream fis = new FileInputStream(keystoreLocation);
        ks.load(fis, "mypass".toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, keystorePass.toCharArray());

        KeyManager[] kms = kmf.getKeyManagers();
        for (int i = 0; i < kms.length; i++) {
            if (kms[i] instanceof X509KeyManager) {
                kms[i] = new MyKeyManager((X509KeyManager) kms[i]); // Your custom KeyManager for extra functionality
            }
        }
        SSLContext sc = SSLContext.getInstance("TLSv1.3");
        sc.init(kms, tmf.getTrustManagers(), new SecureRandom());
        SSLServerSocketFactory factory = sc.getServerSocketFactory();

        serverSocket = (SSLServerSocket) factory.createServerSocket(port);
        final SSLParameters sslParameters = serverSocket.getSSLParameters();
        final List<SNIMatcher> hostMatchers = Arrays.asList(new FalseSNIMatcher(), new TrueSNIMatcher());


        sslParameters.setServerNames(Arrays.asList(new SNIHostName("example.com")));
        sslParameters.setSNIMatchers(hostMatchers);
        serverSocket.setSSLParameters(sslParameters);

        ourThread = new Thread(this);
        ourThread.setDaemon(true);
        ourThread.start();
    }

    public void run() {
        while (running) {
            try {

                final SSLSocket sslSocket = (SSLSocket) serverSocket.accept();
                final ProxyRequest proxyRequest = new ProxyRequest(sslSocket, httpForwarder, httpSpy);

            } catch (Exception ioe) {
                // TODO: What do I do with this?
                ioe.printStackTrace();
            }
        }
    }


    class FalseSNIMatcher extends SNIMatcher {

        FalseSNIMatcher() {
            super(0);
        }

        public boolean matches(SNIServerName serverName) {

            return true;
        }
    }

    class TrueSNIMatcher extends SNIMatcher {

        TrueSNIMatcher() {
            super(1);
        }

        public boolean matches(SNIServerName serverName) {
            return true;
        }
    }

    class MyKeyManager extends X509ExtendedKeyManager {

        X509KeyManager delegate;

        MyKeyManager(X509KeyManager delegagte) {
            this.delegate = delegagte;
        }

        public String chooseEngineClientAlias(String[] keyType, Principal[] issuers, SSLEngine engine) {
            return null;
        }

        public String chooseEngineServerAlias(String keyType, Principal[] issuers, SSLEngine engine) {

            return null;
        }

        public String[] getClientAliases(String var1, Principal[] var2) {
            return delegate.getClientAliases(var1, var2);
        }

        public String chooseClientAlias(String[] var1, Principal[] var2, Socket var3) {
            return delegate.chooseClientAlias(var1, var2, var3);
        }

        public String[] getServerAliases(String var1, Principal[] var2) {
            return delegate.getServerAliases(var1, var2);
        }

        public String chooseServerAlias(String var1, Principal[] var2, Socket var3) {
            try {
                SSLSocketImpl realSocket = (SSLSocketImpl) var3;
                //    SSLSessionImpl sslSession = (SSLSessionImpl) realSocket.getHandshakeSession();
                // sun.security.ssl.SSLSessionImpl sslSession = (SSLSessionImpl) realSocket.getHandshakeSession();

                Field field = SSLSocketImpl.class.getDeclaredField("conContext");
                field.setAccessible(true);
                Object conContext = field.get(realSocket);

                field = conContext.getClass().getDeclaredField("handshakeContext");
                field.setAccessible(true);
                Object handshakeContext = field.get(conContext);

                field = handshakeContext.getClass().getSuperclass().getDeclaredField("negotiatedServerName");
                field.setAccessible(true);

                SNIServerName sniServerName = (SNIServerName) field.get(handshakeContext);

                final String alias = new String(sniServerName.getEncoded());
                return alias;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "example.com";
            //     return delegate.chooseServerAlias(var1, var2, var3);
        }

        public X509Certificate[] getCertificateChain(String var1) {
            return delegate.getCertificateChain(var1);
        }

        public PrivateKey getPrivateKey(String var1) {
            return delegate.getPrivateKey(var1);
        }

    }
}
