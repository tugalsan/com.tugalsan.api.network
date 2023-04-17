package com.tugalsan.api.network.server;

import com.tugalsan.api.unsafe.client.*;
import javax.net.ssl.*;
import java.security.*;
import java.security.cert.*;
import java.util.*;

public class TS_NetworkSSLUtils {

    //https://mkyong.com/java/java-https-client-httpsurlconnection-example/
    public static StringBuffer info(HttpsURLConnection con) {
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
            return sb;
        }, e -> {
            e.printStackTrace();
            return null;
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
        }, e -> TGS_UnSafe.runNothing());
    }
}
