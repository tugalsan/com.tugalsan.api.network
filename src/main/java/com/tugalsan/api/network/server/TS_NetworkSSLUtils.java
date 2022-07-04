package com.tugalsan.api.network.server;

import com.tugalsan.api.unsafe.client.*;
import javax.net.ssl.*;
import java.security.*;
import java.security.cert.*;

public class TS_NetworkSSLUtils {

    public static void disableCertificateValidation() {
        TGS_UnSafe.execute(() -> {
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
        }, e -> TGS_UnSafe.doNothing());
    }
}
