package com.tugalsan.api.network.server;

import module com.tugalsan.api.log;
import module com.tugalsan.api.stream;
import module com.tugalsan.api.time;
import module com.tugalsan.api.union;
import module com.tugalsan.api.function;
import com.tugalsan.api.network.server.core.TS_NetworkCoreDirectoryUtils;
import com.tugalsan.api.network.server.core.TS_NetworkCorePemImporterUtils;
import java.io.*;
import java.nio.file.*;
import javax.net.ssl.*;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.util.*;

public class TS_NetworkSSLUtils {

    final static private TS_Log d = TS_Log.of(TS_NetworkSSLUtils.class);

    public static List<Certificate> toCertificatesFromDirectory(Path dirSSL, CharSequence p12Pass) {
        List<Certificate> trustedLocalCertificates = new ArrayList();
        var listCer = TS_NetworkCoreDirectoryUtils.subFiles(dirSSL, "*.cer", true, true);
        listCer.forEach(p12 -> {
            var u_certs = TS_NetworkSSLUtils.toCertificatesFromCer(p12);
            if (u_certs.isExcuse()) {
                d.ce("run", "skip cer certificate", p12, u_certs.excuse().getMessage());
                return;
            }
            trustedLocalCertificates.addAll(u_certs.value());
        });
        var listCrt = TS_NetworkCoreDirectoryUtils.subFiles(dirSSL, "*.crt", true, true);
        listCrt.forEach(p12 -> {
            var u_certs = TS_NetworkSSLUtils.toCertificatesFromCrt(p12);
            if (u_certs.isExcuse()) {
                d.ce("run", "skip crt certificate", p12, u_certs.excuse().getMessage());
                return;
            }
            trustedLocalCertificates.addAll(u_certs.value());
        });
        var listDer = TS_NetworkCoreDirectoryUtils.subFiles(dirSSL, "*.der", true, true);
        listDer.forEach(p12 -> {
            var u_certs = TS_NetworkSSLUtils.toCertificatesFromDer(p12);
            if (u_certs.isExcuse()) {
                d.ce("run", "skip der certificate", p12, u_certs.excuse().getMessage());
                return;
            }
            trustedLocalCertificates.addAll(u_certs.value());
        });
        var listPem = TS_NetworkCoreDirectoryUtils.subFiles(dirSSL, "*.pem", true, true);
        listPem.forEach(p12 -> {
            var u_certs = TS_NetworkSSLUtils.toCertificatesFromPem(p12);
            if (u_certs.isExcuse()) {
                d.ce("run", "skip pem certificate", p12, u_certs.excuse().getMessage());
                return;
            }
            trustedLocalCertificates.addAll(u_certs.value());
        });
        var listP12 = TS_NetworkCoreDirectoryUtils.subFiles(dirSSL, "*.p12", true, true);
        listP12.forEach(p12 -> {
            var u_certs = TS_NetworkSSLUtils.toCertificatesFromP12(p12, p12Pass, true);
            if (u_certs.isExcuse()) {
                d.ce("run", "skip p12 certificate", p12, u_certs.excuse().getMessage());
                return;
            }
            trustedLocalCertificates.addAll(u_certs.value());
        });
        return trustedLocalCertificates;
    }

    public static TGS_UnionExcuse<List<Certificate>> toCertificatesFromCrt(Path crtCert) {
        return toCertificatesFromCer(crtCert);
    }

    public static TGS_UnionExcuse<List<Certificate>> toCertificatesFromCer(Path cerCert) {
        var u_der = toCertificatesFromDer(cerCert);
        if (!u_der.isExcuse()) {
            return u_der;
        }
        return toCertificatesFromPem(cerCert);
    }

    public static TGS_UnionExcuse<List<Certificate>> toCertificatesFromDer(Path derCert) {
        return TGS_FuncMTCUtils.call(() -> {
            List<Certificate> certificates = new ArrayList();
            try (var is = new BufferedInputStream(Files.newInputStream(derCert))) {
                var cf = CertificateFactory.getInstance("X.509");
                while (is.available() > 0) {
                    var cert = cf.generateCertificate(is);
                    certificates.add(cert);
                }
            }
            return TGS_UnionExcuse.of(certificates);
        }, e -> TGS_UnionExcuse.ofExcuse(e));
    }

    public static TGS_UnionExcuse<List<Certificate>> toCertificatesFromPem(Path pemCert) {
        return TGS_FuncMTCUtils.call(() -> {
            var certififactes = TS_NetworkCorePemImporterUtils.createCertificates(pemCert.toFile());
            return TGS_UnionExcuse.of(Arrays.asList(certififactes));
        }, e -> TGS_UnionExcuse.ofExcuse(e));
    }

    public static TGS_UnionExcuse<List<Certificate>> toCertificatesFromP12(Path p12, CharSequence pass, boolean withChain) {
        return TGS_FuncMTCUtils.call(() -> {
            var u_store = toKeyStoreFromP12(p12, pass);
            if (u_store.isExcuse()) {
                TGS_FuncMTUUtils.thrw(u_store.excuse());
            }
            return toCertificatesFromKeyStore(u_store.value(), withChain);
        }, e -> TGS_UnionExcuse.ofExcuse(e));
    }

    public static TGS_UnionExcuse<SSLServerSocketFactory> toSSLServerSocketFactoryFromPEM(Path pemPrivateKey, Path pemCert, CharSequence pass) {
        return TGS_FuncMTCUtils.call(() -> {
            var store = TS_NetworkCorePemImporterUtils.createSSLFactory(pemPrivateKey.toFile(), pemCert.toFile(), pass.toString());
            return TGS_UnionExcuse.of(store);
        }, e -> TGS_UnionExcuse.ofExcuse(e));
    }

    public static TGS_UnionExcuse<KeyStore> toKeyStoreFromPEM(Path pemPrivateKey, Path pemCert, CharSequence pass) {
        return TGS_FuncMTCUtils.call(() -> {
            var store = TS_NetworkCorePemImporterUtils.createKeyStore(pemPrivateKey.toFile(), pemCert.toFile(), pass.toString());
            return TGS_UnionExcuse.of(store);
        }, e -> TGS_UnionExcuse.ofExcuse(e));
    }

    public static TGS_UnionExcuse<KeyStore> toKeyStoreFromP12(Path p12, CharSequence pass) {
        return TGS_FuncMTCUtils.call(() -> {
            var store = KeyStore.getInstance("PKCS12");
            try (var is = Files.newInputStream(p12)) {
                store.load(is, pass.toString().toCharArray());
            }
            return TGS_UnionExcuse.of(store);
        }, e -> TGS_UnionExcuse.ofExcuse(e));
    }

    public static TGS_UnionExcuse<PrivateKey> toPrivateKey(KeyStore store, CharSequence pass) {
        return TGS_FuncMTCUtils.call(() -> {
            var privateKey = (PrivateKey) store.getKey("example", pass.toString().toCharArray());
            if (privateKey == null) {
                TGS_FuncMTUUtils.thrw(new NullPointerException("no private key exists"));
            }
            return TGS_UnionExcuse.of(privateKey);
        }, e -> TGS_UnionExcuse.ofExcuse(e));
    }

    public static TGS_UnionExcuse<List<Certificate>> toCertificatesFromKeyStore(KeyStore store, boolean withChain) {
        return TGS_FuncMTCUtils.call(() -> {
            List<Certificate> certificates = new ArrayList();
            var aliases = TGS_StreamUtils.of(store.aliases()).toList();
            for (var alias : aliases) {
                var cert_main = store.getCertificate(alias);
                certificates.add(cert_main);
                certificates.addAll(Arrays.asList(store.getCertificateChain(alias)));
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
    public static TGS_UnionExcuse<StringBuilder> info(HttpsURLConnection con) {
        return TGS_FuncMTCUtils.call(() -> {
            var sb = new StringBuilder();
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
        TGS_FuncMTCUtils.run(() -> {
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
        }, e -> TGS_FuncMTU.empty.run());
    }
}
