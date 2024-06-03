package com.tugalsan.api.network.server;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record TS_NetworkIPs(Optional<String> ip_loopback,
        Optional<String> ip_castBroad,
        List<String> ip_castMulti,
        List<String> ip_hostLocal,
        List<String> ip_hostPublic) {

    public boolean ip_hostPublic_contains(String ipTarget) {
        return ip_hostPublic.stream().filter(ipSource -> Objects.equals(ipSource, ipTarget)).findAny().isPresent();
    }

    public boolean ip_hostLocal_contains(String ipTarget) {
        return ip_hostLocal.stream().filter(ipSource -> Objects.equals(ipSource, ipTarget)).findAny().isPresent();
    }

    public boolean ip_castMulti_contains(String ipTarget) {
        return ip_castMulti.stream().filter(ipSource -> Objects.equals(ipSource, ipTarget)).findAny().isPresent();
    }

    public boolean ip_castBroad_contains(String ipTarget) {
        if (ip_castBroad.isEmpty()) {
            return false;
        }
        return Objects.equals(ip_castBroad.orElseThrow(), ipTarget);
    }

    public boolean ip_loopback_contains(String ipTarget) {
        if (ip_loopback.isEmpty()) {
            return false;
        }
        return Objects.equals(ip_loopback.orElseThrow(), ipTarget);
    }
}
