package com.tugalsan.api.network.server;

import com.tugalsan.api.function.client.TGS_Func;
import com.tugalsan.api.stream.client.TGS_StreamUtils;
import com.tugalsan.api.time.client.TGS_Time;
import com.tugalsan.api.union.client.TGS_UnionExcuse;
import com.tugalsan.api.unsafe.client.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.net.ssl.*;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.util.*;

public class TS_NetworkSSLUtils {

    public static TGS_UnionExcuse<KeyStore> sniffFromP12ToKeyStore(Path p12, CharSequence pass) {
        return TGS_UnSafe.call(() -> {
            var store = KeyStore.getInstance("PKCS12");
            store.load(Files.newInputStream(p12), pass.toString().toCharArray());
            return TGS_UnionExcuse.of(store);
        }, e -> TGS_UnionExcuse.ofExcuse(e));
    }

    public static TGS_UnionExcuse<PrivateKey> sniffFromP12ToPrivateKey(Path p12, CharSequence pass) {
        return TGS_UnSafe.call(() -> {
            var u_store = sniffFromP12ToKeyStore(p12, pass);
            if (u_store.isExcuse()) {
                TGS_UnSafe.thrw(u_store.excuse());
            }
            u_store.value().load(Files.newInputStream(p12), pass.toString().toCharArray());
            var privateKey = (PrivateKey) u_store.value().getKey("example", pass.toString().toCharArray());
            if (privateKey == null) {
                TGS_UnSafe.thrw(new NullPointerException("no private key exists"));
            }
            return TGS_UnionExcuse.of(privateKey);
        }, e -> TGS_UnionExcuse.ofExcuse(e));
    }

    public static TGS_UnionExcuse<List<Certificate>> sniffFromToCertificatesP12(Path p12, CharSequence pass, boolean withChain) {
        return TGS_UnSafe.call(() -> {
            List<Certificate> certificates = new ArrayList();
            var u_store = sniffFromP12ToKeyStore(p12, pass);
            if (u_store.isExcuse()) {
                TGS_UnSafe.thrw(u_store.excuse());
            }
            var aliases = TGS_StreamUtils.of(u_store.value().aliases()).toList();
            for (var alias : aliases) {
                var cert_main = u_store.value().getCertificate(alias);
                certificates.add(cert_main);
                certificates.addAll(Arrays.asList(u_store.value().getCertificateChain(alias)));
            }
            return TGS_UnionExcuse.of(certificates);
        }, e -> TGS_UnionExcuse.ofExcuse(e));
    }

    public static TGS_UnionExcuse<TGS_Time> expirationOfPem(Path pemPath) {
        try {
            var bytes = Files.readAllBytes(pemPath);
            var cert = (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(new ByteArrayInputStream(bytes));
            var date = cert.getNotAfter();
            var time = TGS_Time.of(date);
            return TGS_UnionExcuse.of(time);
        } catch (CertificateException | IOException e) {
            return TGS_UnionExcuse.ofExcuse(e);
        }
    }

    //https://mkyong.com/java/java-https-client-httpsurlconnection-example/
    public static TGS_UnionExcuse<StringBuffer> info(HttpsURLConnection con) {
        return TGS_UnSafe.call(() -> {
            var sb = new StringBuffer();
            sb.append("\nResponse Code : ").append(con.getResponseCode());
            sb.append("\nCipher Suite : ").append(con.getCipherSuite());
            sb.append("\n\n");
            Arrays.stream(con.getServerCertificates()).forEach(cert -> {
                sb.append("\nCert Type : ").append(cert.getType());
                sb.append("\nCert Hash Code : ").append(cert.hashCode());
                sb.append("\nCert Public Key Algorithm : ").append(cert.getPublicKey().getAlgorithm());
                sb.append("\nCert Public Key Format : ").append(cert.getPublicKey().getFormat());
                sb.append("\n\n");
            });
            return TGS_UnionExcuse.of(sb);
        }, e -> {
            return TGS_UnionExcuse.ofExcuse(e);
        });
    }

    public static void disableCertificateValidation() {
        TGS_UnSafe.run(() -> {
            var sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
            }, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        }, e -> TGS_Func.empty.run());
    }
}
