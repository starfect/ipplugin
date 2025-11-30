package me.starfect.ipplugin.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import me.starfect.ipplugin.data.IpInfo;

public final class IpApiClient implements ApiClient {
    private static final String FREE_ENDPOINT = "http://ip-api.com/json/";
    private static final String PRO_ENDPOINT = "https://pro.ip-api.com/json/";
    private static final String QUERY_FIELDS = "?fields=status,message,country,countryCode,regionName,city,org,isp,lat,lon,timezone,query";
    private final HttpClient httpClient;
    private final Gson gson;
    private final String apiKey;

    public IpApiClient(String apiKey) {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
    }

    @Override
    public CompletableFuture<IpInfo> lookup(String ipAddress) {
        String base = apiKey.isEmpty() ? FREE_ENDPOINT : PRO_ENDPOINT;
        String url = base + encode(ipAddress) + QUERY_FIELDS;
        if (!apiKey.isEmpty()) {
            url = url + "&key=" + encode(apiKey);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .thenApply(response -> gson.fromJson(response.body(), JsonObject.class))
                .thenApply(this::parse);
    }

    @Override
    public String getName() {
        return "ip-api.com";
    }

    private IpInfo parse(JsonObject json) {
        String status = json.has("status") ? json.get("status").getAsString() : "fail";
        if (!"success".equalsIgnoreCase(status)) {
            String message = json.has("message") ? json.get("message").getAsString() : "unknown";
            throw new IllegalStateException("API error: " + message);
        }

        return IpInfo.builder()
                .ip(getString(json, "query"))
                .country(getString(json, "country"))
                .countryCode(getString(json, "countryCode"))
                .region(getString(json, "regionName"))
                .city(getString(json, "city"))
                .org(getString(json, "org"))
                .isp(getString(json, "isp"))
                .timezone(getString(json, "timezone"))
                .latitude(getDouble(json, "lat"))
                .longitude(getDouble(json, "lon"))
                .provider(getName())
                .build();
    }

    private String getString(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : "";
    }

    private Double getDouble(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsDouble() : null;
    }

    private String encode(String input) {
        return input.replace(" ", "%20");
    }
}
