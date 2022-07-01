package com.tugalsan.api.network.server;

import com.tugalsan.api.list.client.*;
import com.tugalsan.api.unsafe.client.*;
import java.net.*;
import java.util.*;

public class TS_NetworkMACUtils {

    public static List<String> getMAC_FromNetworkInterface() {
        return TGS_UnSafe.compile(() -> {
            List<String> macs = TGS_ListUtils.of();
            var networks = NetworkInterface.getNetworkInterfaces();
            while (networks.hasMoreElements()) {
                var network = networks.nextElement();
                var mac = network.getHardwareAddress();
                if (mac != null) {
                    var sb = new StringBuilder();
                    for (var i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                    }
                    macs.add(sb.toString());
                }
            }
            return macs;
        });
    }

    public static List<String> getMAC_from_IP_CONFIG_ALL(CharSequence fullInfo) {
        List<String> macs = TGS_ListUtils.of();
        String s;
        int v1, v2, v3, v4, v5, v6;
        var t = ':';
        {
            var st = new StringTokenizer(fullInfo.toString());
            while (st.hasMoreTokens()) {
                s = st.nextToken().trim();
                v1 = s.indexOf(t);
                if (v1 == -1) {
                    continue;
                }
                v2 = s.indexOf(t, v1 + 1);
                if (v2 == -1 || v2 - v1 != 3) {
                    continue;
                }
                v3 = s.indexOf(t, v2 + 1);
                if (v3 == -1 || v3 - v2 != 3) {
                    continue;
                }
                v4 = s.indexOf(t, v3 + 1);
                if (v4 == -1 || v4 - v3 != 3) {
                    continue;
                }
                v5 = s.indexOf(t, v4 + 1);
                if (v5 == -1 || v5 - v4 != 3) {
                    continue;
                }
                v6 = s.indexOf(t, v5 + 1);
                if (v6 != -1) {
                    continue;
                }
                macs.add(s);
            }
        }
        t = '-';
        {
            var st = new StringTokenizer(fullInfo.toString());
            while (st.hasMoreTokens()) {
                s = st.nextToken().trim();
                v1 = s.indexOf(t);
                if (v1 == -1) {
                    continue;
                }
                v2 = s.indexOf(t, v1 + 1);
                if (v2 == -1 || v2 - v1 != 3) {
                    continue;
                }
                v3 = s.indexOf(t, v2 + 1);
                if (v3 == -1 || v3 - v2 != 3) {
                    continue;
                }
                v4 = s.indexOf(t, v3 + 1);
                if (v4 == -1 || v4 - v3 != 3) {
                    continue;
                }
                v5 = s.indexOf(t, v4 + 1);
                if (v5 == -1 || v5 - v4 != 3) {
                    continue;
                }
                v6 = s.indexOf(t, v5 + 1);
                if (v6 != -1) {
                    continue;
                }
                macs.add(s);
            }
        }
        return macs;
    }
}
