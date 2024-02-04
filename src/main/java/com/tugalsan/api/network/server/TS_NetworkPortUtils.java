package com.tugalsan.api.network.server;

import com.tugalsan.api.list.client.*;
import com.tugalsan.api.unsafe.client.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

public class TS_NetworkPortUtils {

    public static int MIN_PORT() {
        return 1;
    }

    public static int MAX_PORT() {
        return 65535;
    }

    public static int MAX_THREAD_COUNT() {
        return 30;
    }

    public static float MAX_TIMEOUT_SEC() {
        return 0.4f;
    }

    private static class TaskIsReacable implements Callable<Integer> {

        private final String ipAddress;
        private final int port;
        private final float watchDogSeconds;

        public TaskIsReacable(CharSequence ipAddress, int port, float watchDogSeconds) {
            this.ipAddress = ipAddress.toString();
            this.port = port;
            this.watchDogSeconds = watchDogSeconds;
        }

        @Override
        public Integer call() {
            var result = isReacable(ipAddress, port, watchDogSeconds) ? port : null;
            if (port % 1000 == 0) {
                System.out.println("end.ipAddress:" + ipAddress + ", port:" + port);
            }
            return result;
        }
    }

    public static List<Integer> getReachables(CharSequence ip, boolean useVirtualThread) {
        return TGS_UnSafe.call(() -> {
            List<TaskIsReacable> taskList = TGS_ListUtils.of();
            IntStream.range(MIN_PORT(), MAX_PORT()).forEachOrdered(port -> taskList.add(new TaskIsReacable(ip, port, MAX_TIMEOUT_SEC())));
            var executor = useVirtualThread
                    ? Executors.newVirtualThreadPerTaskExecutor()
                    : Executors.newFixedThreadPool(MAX_THREAD_COUNT());
            var futures = executor.invokeAll(taskList);
            executor.shutdown();
            List<Integer> results = TGS_ListUtils.of();
            futures.stream().forEachOrdered(f -> {
                TGS_UnSafe.run(() -> {
                    var port = f.get();
                    if (port != null) {
                        results.add(port);
                    }
                });
            });
            return results;
        });
    }

    public static boolean isReacable(CharSequence ip, int port, float watchDogSeconds) {
        return TGS_UnSafe.call(() -> {
            try (var socket = new Socket();) {
                socket.connect(new InetSocketAddress(ip.toString(), port), Math.round(watchDogSeconds * 1000));
                return true;
            }
        }, e -> {
            return false;
        });
    }

}
