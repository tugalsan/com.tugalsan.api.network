package com.tugalsan.api.network.server;

import com.tugalsan.api.callable.client.TGS_CallableType1;
import com.tugalsan.api.charset.client.TGS_CharSetCast;
import com.tugalsan.api.log.server.*;
import com.tugalsan.api.network.client.TGS_NetworkIPUtils;
import com.tugalsan.api.os.server.TS_OsProcess;
import com.tugalsan.api.stream.client.TGS_StreamUtils;
import com.tugalsan.api.string.client.*;
import com.tugalsan.api.thread.server.async.TS_ThreadAsyncAwait;
import com.tugalsan.api.thread.server.sync.TS_ThreadSyncTrigger;
import com.tugalsan.api.unsafe.client.*;
import java.net.*;
import java.time.Duration;
import java.util.*;
import java.util.stream.*;
import javax.servlet.http.*;

public class TS_NetworkIPUtils {

    final private static TS_Log d = TS_Log.of(true, TS_NetworkIPUtils.class);

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

    private static class TaskIsReacable implements TGS_CallableType1<Optional<String>, TS_ThreadSyncTrigger> {

        private final String ipAddress;
        private final int watchDogSeconds;

        public TaskIsReacable(String ipAddress, int watchDogSeconds) {
            this.ipAddress = ipAddress;
            this.watchDogSeconds = watchDogSeconds;
        }

        @Override
        public Optional<String> call(TS_ThreadSyncTrigger threadKiller) {
            if (ipAddress.endsWith("5")) {
                d.ci("end.ipAddress:" + ipAddress);
            }
            return isReacable(ipAddress, watchDogSeconds) ? Optional.of(ipAddress) : Optional.empty();
        }
    }

    public static List<String> getReachables(CharSequence ipClassC, TS_ThreadSyncTrigger threadKiller) {
        return TGS_UnSafe.call(() -> {
            var threadUntil = Duration.ofSeconds(2 * (long) MAX_TIMEOUT_SEC() * (MAX_IP() - MIN_IP()));
            List<TGS_CallableType1<Optional<String>, TS_ThreadSyncTrigger>> taskList = TGS_StreamUtils.toLst(
                    IntStream.range(MIN_IP(), MAX_IP())
                            .mapToObj(ipPartD -> TGS_StringUtils.concat(ipClassC, ".", String.valueOf(ipPartD)))
                            .map(ipNext -> new TaskIsReacable(ipNext, MAX_TIMEOUT_SEC()))
            );
            var await = TS_ThreadAsyncAwait.callParallelRateLimited(threadKiller, MAX_THREAD_COUNT(), threadUntil, taskList);
            return TGS_StreamUtils.toLst(
                    await.resultsForSuccessfulOnes.stream()
                            .filter(r -> !r.isEmpty())
                            .map(r -> r.get())
            );
        });
    }

    //https://stackoverflow.com/questions/77937704/in-java-how-to-migrate-from-executors-newfixedthreadpoolmax-thread-count-to?noredirect=1#comment137420375_77937704
//        private static class TaskIsReacable implements Callable<String> {
//
//            private final String ipAddress;
//            private final int watchDogSeconds;
//
//            public TaskIsReacable(String ipAddress, int watchDogSeconds) {
//                this.ipAddress = ipAddress;
//                this.watchDogSeconds = watchDogSeconds;
//            }
//
//            @Override
//            public String call() {
//                var result = isReacable(ipAddress, watchDogSeconds) ? ipAddress : null;
//                if (ipAddress.endsWith("5")) {
//                    System.out.println("end.ipAddress:" + ipAddress);
//                }
//                return result;
//            }
//        }
//    public static List<String> getReachables(CharSequence ipClassC, boolean useVirtualThread) {
//        return TGS_UnSafe.call(() -> {
//            List<TaskIsReacable> taskList = TGS_ListUtils.of();
//            IntStream.range(MIN_IP(), MAX_IP()).forEachOrdered(ipPartD -> {
//                var ipNext = TGS_StringUtils.concat(ipClassC, ".", String.valueOf(ipPartD));
//                taskList.add(new TaskIsReacable(ipNext, MAX_TIMEOUT_SEC()));
//            });
//            var executor = useVirtualThread
//                    ? Executors.newVirtualThreadPerTaskExecutor()
//                    : Executors.newFixedThreadPool(MAX_THREAD_COUNT());
//            var futures = executor.invokeAll(taskList);
//            executor.shutdown();
//            List<String> results = TGS_ListUtils.of();
//            futures.stream().forEachOrdered(f -> {
//                TGS_UnSafe.run(() -> {
//                    if (f.get() == null) {
//                        return;
//                    }
//                    results.add(f.get());
//                });
//            });
//            return results;
//        });
//    }
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
            if (TGS_NetworkIPUtils.isLocalHost(r)) {
                r = InetAddress.getLocalHost().getHostAddress();
            }
            return r;
        });
    }
}
