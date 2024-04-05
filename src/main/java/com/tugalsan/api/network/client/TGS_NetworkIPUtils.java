package com.tugalsan.api.network.client;

import com.tugalsan.api.cast.client.TGS_CastUtils;
import java.util.*;

public class TGS_NetworkIPUtils {

    public static boolean isValidInet4Address(CharSequence ip) {
        if (ip == null) {
            return false;
        }
        var groups = ip.toString().split("\\.");
        if (groups.length != 4) {
            return false;
        }
        return Arrays.stream(groups)
                .filter(s -> s.length() > 1 && s.startsWith("0"))
                .map(s -> TGS_CastUtils.toInteger(ip))
                .filter(s -> s != null)
                .filter(i -> (i >= 0 && i <= 255))
                .count() == 4;
    }

    public static boolean isLocalHost(CharSequence domainOrIp) {
        return Objects.equals(domainOrIp, "localhost")
                || Objects.equals(domainOrIp, "127.0.0.1")
                || Objects.equals(domainOrIp, "0:0:0:0:0:0:0:1")
                || Objects.equals(domainOrIp, "::1");
    }

    public static boolean isLocalClientA(CharSequence clientIp) {
        return clientIp.toString().startsWith("10.");
    }

    public static boolean isLocalClientB(CharSequence clientIp) {
        return clientIp.toString().startsWith("172.16.");
    }

    public static boolean isLocalClientC(CharSequence clientIp) {
        return clientIp.toString().startsWith("192.168.");
    }

    public static boolean isLocalClientC(CharSequence serverIp, CharSequence clientIp) {
        var serverIpStr = serverIp.toString();
        var prefixLen = serverIpStr.lastIndexOf(".") + 1;
        var prefix = serverIpStr.substring(0, prefixLen);
        return clientIp.toString().startsWith(prefix);
    }

    public static boolean isLocalClient(CharSequence serverIp, CharSequence clientIp) {
        return isLocalClientA(clientIp) || isLocalClientB(clientIp) || isLocalClientC(clientIp) || isLocalClientC(serverIp, clientIp);
    }
}
