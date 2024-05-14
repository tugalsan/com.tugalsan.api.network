module com.tugalsan.api.network {
    requires javax.servlet.api;
    requires com.tugalsan.api.os;
    requires com.tugalsan.api.unsafe;
    requires com.tugalsan.api.string;
    requires com.tugalsan.api.union;
    requires com.tugalsan.api.log;
    requires com.tugalsan.api.random;
    requires com.tugalsan.api.list;
    requires com.tugalsan.api.stream;
    requires com.tugalsan.api.thread;
    requires com.tugalsan.api.runnable;
    requires com.tugalsan.api.charset;
    requires com.tugalsan.api.callable;
    exports com.tugalsan.api.network.client;
    exports com.tugalsan.api.network.server;
}
