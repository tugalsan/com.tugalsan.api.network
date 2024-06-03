package com.tugalsan.api.network.server;

import java.util.List;
import java.util.Optional;

public record TS_NetworkIPs(Optional<String> ip_loopback,
        Optional<String> ip_castBroad,
        List<String> ip_castMulti,
        List<String> ip_hostLocal,
        List<String> ip_hostPublic) {
}
