package com.tugalsan.api.network.server;

import com.tugalsan.api.function.client.maythrowexceptions.checked.TGS_FuncMTCUtils;
import com.tugalsan.api.function.client.maythrowexceptions.unchecked.TGS_FuncMTU_OutTyped_In1;
import com.tugalsan.api.log.server.TS_Log;
import com.tugalsan.api.stream.client.TGS_StreamUtils;
import com.tugalsan.api.thread.server.async.await.TS_ThreadAsyncAwait;
import com.tugalsan.api.thread.server.sync.TS_ThreadSyncTrigger;
import com.tugalsan.api.union.client.TGS_UnionExcuse;
import com.tugalsan.api.union.client.TGS_UnionExcuseVoid;

import java.net.*;
import java.time.Duration;
import java.util.*;
import java.util.stream.*;

public class TS_NetworkPortUtils {

    final private static TS_Log d = TS_Log.of(TS_NetworkPortUtils.class);

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

    private static class TaskIsReacable implements TGS_FuncMTU_OutTyped_In1<TGS_UnionExcuse<Integer>, TS_ThreadSyncTrigger> {

        private final String ipAddress;
        private final int port;
        private final float watchDogSeconds;

        public TaskIsReacable(CharSequence ipAddress, int port, float watchDogSeconds) {
            this.ipAddress = ipAddress.toString();
            this.port = port;
            this.watchDogSeconds = watchDogSeconds;
        }

        @Override
        public TGS_UnionExcuse<Integer> call(TS_ThreadSyncTrigger threadKiller) {
            if (port % 1000 == 0) {
                d.ci("end.ipAddress:" + ipAddress + ", port:" + port);
            }
            var u = isReacable(ipAddress, port, watchDogSeconds);
            if (u.isExcuse()) {
                return u.toExcuse();
            }
            return TGS_UnionExcuse.of(port);
        }
    }

    public static List<Integer> getReachables(CharSequence ip, TS_ThreadSyncTrigger threadKiller) {
        var threadUntil = Duration.ofSeconds(2 * (long) MAX_TIMEOUT_SEC() * (MAX_PORT() - MIN_PORT()));
        List<TGS_FuncMTU_OutTyped_In1<TGS_UnionExcuse<Integer>, TS_ThreadSyncTrigger>> taskList = TGS_StreamUtils.toLst(
                IntStream.range(MIN_PORT(), MAX_PORT())
                        .mapToObj(port -> new TaskIsReacable(ip, port, MAX_TIMEOUT_SEC()))
        );
        var await = TS_ThreadAsyncAwait.callParallelRateLimited(threadKiller.newChild(d.className), MAX_THREAD_COUNT(), threadUntil, taskList);
        return TGS_StreamUtils.toLst(
                await.resultsSuccessful().stream()
                        .filter(r -> r.isPresent())
                        .map(r -> r.value())
        );
    }

    //https://stackoverflow.com/questions/77937704/in-java-how-to-migrate-from-executors-newfixedthreadpoolmax-thread-count-to?noredirect=1#comment137420375_77937704
//        private static class TaskIsReacable implements Callable<Integer> {
//
//            private final String ipAddress;
//            private final int port;
//            private final float watchDogSeconds;
//
//            public TaskIsReacable(CharSequence ipAddress, int port, float watchDogSeconds) {
//                this.ipAddress = ipAddress.toString();
//                this.port = port;
//                this.watchDogSeconds = watchDogSeconds;
//            }
//
//            @Override
//            public Integer call() {
//                var result = isReacable(ipAddress, port, watchDogSeconds) ? port : null;
//                if (port % 1000 == 0) {
//                    System.out.println("end.ipAddress:" + ipAddress + ", port:" + port);
//                }
//                return result;
//            }
//        }
//    public static List<Integer> getReachables(CharSequence ip, boolean useVirtualThread) {
//        return TGS_FuncMTCUtils.call(() -> {
//            List<TaskIsReacable> taskList = TGS_ListUtils.of();
//            IntStream.range(MIN_PORT(), MAX_PORT()).forEachOrdered(port -> taskList.add(new TaskIsReacable(ip, port, MAX_TIMEOUT_SEC())));
//            var executor = useVirtualThread
//                    ? Executors.newVirtualThreadPerTaskExecutor()
//                    : Executors.newFixedThreadPool(MAX_THREAD_COUNT());
//            var futures = executor.invokeAll(taskList);
//            executor.shutdown();
//            List<Integer> results = TGS_ListUtils.of();
//            futures.stream().forEachOrdered(f -> {
//                TGS_FuncMTCUtils.run(() -> {
//                    var port = f.get();
//                    if (port != null) {
//                        results.add(port);
//                    }
//                });
//            });
//            return results;
//        });
//    }
    public static TGS_UnionExcuseVoid isReacable(CharSequence ip, int port, float watchDogSeconds) {
        return TGS_FuncMTCUtils.call(() -> {
            try (var socket = new Socket();) {
                socket.connect(new InetSocketAddress(ip.toString(), port), Math.round(watchDogSeconds * 1000));
                return TGS_UnionExcuseVoid.ofVoid();
            }
        }, e -> {
            return TGS_UnionExcuseVoid.ofExcuse(e);
        });
    }

}
