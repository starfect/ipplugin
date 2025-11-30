package me.starfect.ipplugin.api;

import java.util.concurrent.CompletableFuture;
import me.starfect.ipplugin.data.IpInfo;

public interface ApiClient {
    CompletableFuture<IpInfo> lookup(String ipAddress);

    String getName();
}
