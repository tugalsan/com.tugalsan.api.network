package com.tugalsan.api.network.server;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record TS_NetworkIPs(
        Optional<String> ip_localHost_loopBack,
        Optional<String> ip_broadCast,
        List<String> ip_multiCast,
        List<String> ip_localNetwork,
        List<String> ip_other) {

    public boolean ip_other_contains(String ipTarget) {
        return ip_other.stream().filter(ipSource -> Objects.equals(ipSource, ipTarget)).findAny().isPresent();
    }

    public boolean ip_localNetwork_contains(String ipTarget) {
        return ip_localNetwork.stream().filter(ipSource -> Objects.equals(ipSource, ipTarget)).findAny().isPresent();
    }

    public boolean ip_multiCast_contains(String ipTarget) {
        return ip_multiCast.stream().filter(ipSource -> Objects.equals(ipSource, ipTarget)).findAny().isPresent();
    }

    public boolean ip_broadCast_contains(String ipTarget) {
        if (ip_broadCast.isEmpty()) {
            return false;
        }
        return Objects.equals(ip_broadCast.orElseThrow(), ipTarget);
    }

    public boolean ip_localHost_loopBack_contains(String ipTarget) {
        if (ip_localHost_loopBack.isEmpty()) {
            return false;
        }
        return Objects.equals(ip_localHost_loopBack.orElseThrow(), ipTarget);
    }
}
