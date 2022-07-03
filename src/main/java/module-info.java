module com.tugalsan.api.network {
    requires javax.servlet.api;
    requires com.tugalsan.api.os;
    requires com.tugalsan.api.unsafe;
    requires com.tugalsan.api.string;
    requires com.tugalsan.api.log;
    requires com.tugalsan.api.list;
    requires com.tugalsan.api.compiler;
    exports com.tugalsan.api.network.client;
    exports com.tugalsan.api.network.server;
}
