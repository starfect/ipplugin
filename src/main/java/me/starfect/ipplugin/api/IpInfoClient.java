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

public final class IpInfoClient implements ApiClient {
    private static final String ENDPOINT = "https://ipinfo.io/";
    private final HttpClient httpClient;
    private final Gson gson;
    private final String apiToken;

    public IpInfoClient(String apiToken) {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
        this.apiToken = apiToken == null ? "" : apiToken.trim();
    }

    @Override
    public CompletableFuture<IpInfo> lookup(String ipAddress) {
        String url = ENDPOINT + encode(ipAddress) + "/json";
        if (!apiToken.isEmpty()) {
            url = url + "?token=" + encode(apiToken);
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
        return "ipinfo.io";
    }

    private IpInfo parse(JsonObject json) {
        if (json.has("error")) {
            String message = json.getAsJsonObject("error").get("message").getAsString();
            throw new IllegalStateException("API error: " + message);
        }

        String loc = json.has("loc") && !json.get("loc").isJsonNull() ? json.get("loc").getAsString() : "";
        Double lat = null;
        Double lon = null;
        if (loc.contains(",")) {
            String[] parts = loc.split(",");
            try {
                lat = Double.parseDouble(parts[0]);
                lon = Double.parseDouble(parts[1]);
            } catch (NumberFormatException ignored) {
                lat = null;
                lon = null;
            }
        }

        return IpInfo.builder()
                .ip(getString(json, "ip"))
                .country(getString(json, "country"))
                .countryCode(getString(json, "country"))
                .region(getString(json, "region"))
                .city(getString(json, "city"))
                .org(getString(json, "org"))
                .isp(getString(json, "org"))
                .timezone(getString(json, "timezone"))
                .latitude(lat)
                .longitude(lon)
                .provider(getName())
                .build();
    }

    private String getString(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : "";
    }

    private String encode(String input) {
        return input.replace(" ", "%20");
    }
}
