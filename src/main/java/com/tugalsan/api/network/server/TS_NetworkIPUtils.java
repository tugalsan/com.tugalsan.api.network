package com.tugalsan.api.network.server;

import com.tugalsan.api.charset.client.TGS_CharSetCast;
import com.tugalsan.api.list.client.*;
import com.tugalsan.api.log.server.*;
import com.tugalsan.api.os.server.TS_OsProcess;
import com.tugalsan.api.string.client.*;
import com.tugalsan.api.unsafe.client.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;
import javax.servlet.http.*;

public class TS_NetworkIPUtils {

    final private static TS_Log d = TS_Log.of(TS_NetworkIPUtils.class);

    public static int MIN_IP() {
        return 0;
    }

    public static int MAX_IP() {
        return 255;
    }

    public static int MAX_THREAD_COUNT() {
        return 30;
    }

    public static int MAX_TIMEOUT_SEC() {
        return 10;
    }

    public static void logIPServerAndIpRouter() {
        TGS_UnSafe.run(() -> {
            d.cr("logIPServerAndIpRouter", "getIPServer()", TS_NetworkIPUtils.getIPServer_ifConnectedToInternet());
            d.cr("logIPServerAndIpRouter", "getIPRouter()", TS_NetworkIPUtils.getIPRouter());
        }, e -> d.ce("logIPServerAndIpRouter", "ERROR: Possibly no internet connection!", e.getMessage()));
    }

    private static class TaskIsReacable implements Callable<String> {

        private final String ipAddress;
        private final int watchDogSeconds;

        public TaskIsReacable(String ipAddress, int watchDogSeconds) {
            this.ipAddress = ipAddress;
            this.watchDogSeconds = watchDogSeconds;
        }

        @Override
        public String call() {
            var result = isReacable(ipAddress, watchDogSeconds) ? ipAddress : null;
            if (ipAddress.endsWith("5")) {
                System.out.println("end.ipAddress:" + ipAddress);
            }
            return result;
        }
    }

    public static List<String> getReachables(CharSequence ipClassC) {
        return TGS_UnSafe.call(() -> {
            List<TaskIsReacable> taskList = TGS_ListUtils.of();
            IntStream.range(MIN_IP(), MAX_IP()).forEachOrdered(ipPartD -> {
                var ipNext = TGS_StringUtils.concat(ipClassC, ".", String.valueOf(ipPartD));
                taskList.add(new TaskIsReacable(ipNext, MAX_TIMEOUT_SEC()));
            });
            var executor = (ExecutorService) Executors.newFixedThreadPool(MAX_THREAD_COUNT());
            var futures = executor.invokeAll(taskList);
            executor.shutdown();
            List<String> results = TGS_ListUtils.of();
            futures.stream().forEachOrdered(f -> {
                TGS_UnSafe.run(() -> {
                    if (f.get() == null) {
                        return;
                    }
                    results.add(f.get());
                });
            });
            return results;
        });
    }

    public static boolean isReacable(CharSequence ipAddress) {
        return isReacable(ipAddress, 5);
    }

    public static boolean isReacable(CharSequence ipAddress, int watchDogSeconds) {
        return TGS_UnSafe.call(() -> getByName(ipAddress).isReachable(watchDogSeconds * 1000));
    }

    public static InetAddress getByName(CharSequence ipAddress) {
        return TGS_UnSafe.call(() -> InetAddress.getByName(ipAddress.toString()));
    }

    public static String get_IP_CONFIG_ALL() {//cmd /c netstat
        var osName = TGS_CharSetCast.toLocaleLowerCase(System.getProperty("os.name"));
        if (osName.startsWith("windows")) {
            return TS_OsProcess.of("ipconfig /all").output;
        }
        if (osName.startsWith("linux")) {
            return TS_OsProcess.of("ifconfig").output;
        }
        return TGS_UnSafe.thrwReturns(d.className, "get_IP_CONFIG_ALL", "UnknownOs: " + System.getProperty("os.name"));
    }

    public static String getIPRouter() {
        return TGS_UnSafe.call(() -> {
            var ip = InetAddress.getLocalHost();
            return ip.getHostAddress();
        });
    }

    public static Optional<String> getIPServer_ifConnectedToInternet() {
        return TGS_UnSafe.call(() -> {
            try (var socket = new Socket()) {
                socket.connect(new InetSocketAddress("google.com", 80));
                var ip = socket.getLocalAddress().toString();
                if (ip != null && ip.startsWith("/")) {
                    ip = ip.substring(1);
                }
                return Optional.of(ip);
            }
        }, e -> Optional.empty());
    }

    public static String getIPClient(HttpServletRequest request) {
        return TGS_UnSafe.call(() -> {
            var r = request.getRemoteAddr();
            if (r != null && (r.equals("0:0:0:0:0:0:0:1") || r.equals("127.0.0.1") || r.equals("localhost"))) {
                r = InetAddress.getLocalHost().getHostAddress();
            }
            return r;
        });
    }
}
