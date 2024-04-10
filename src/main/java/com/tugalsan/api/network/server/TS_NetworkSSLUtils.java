package com.tugalsan.api.network.server;

import com.tugalsan.api.log.server.TS_Log;
import com.tugalsan.api.union.client.TGS_UnionExcuse;
import java.io.IOException;
import javax.net.ssl.*;
import java.security.*;
import java.security.cert.*;
import java.util.*;

public class TS_NetworkSSLUtils {

    final private static TS_Log d = TS_Log.of(TS_NetworkSSLUtils.class);

    //https://mkyong.com/java/java-https-client-httpsurlconnection-example/
    public static TGS_UnionExcuse<StringBuffer> info(HttpsURLConnection con) {
        try {
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
        } catch (IOException ex) {
            return TGS_UnionExcuse.ofExcuse(ex);
        }
    }

    public static void disableCertificateValidation() {
        try {
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
        } catch (NoSuchAlgorithmException | KeyManagementException ex) {
            d.ce("disableCertificateValidation", "Unsuccessful", ex.getMessage());
        }
    }
}
