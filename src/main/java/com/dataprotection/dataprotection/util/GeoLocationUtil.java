package com.dataprotection.dataprotection.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Component
public class GeoLocationUtil {
    private final RestClient restClient;
    private final String token;

    public GeoLocationUtil(
            @Value("${app.location.ipinfo-base-url:https://api.ipinfo.io/lookup}") String baseUrl,
            @Value("${app.location.ipinfo-token:}") String token) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.token = token;
    }

    public String getLocationFromIp(String ipAddress) {
        String normalizedIp = normalizeIp(ipAddress);
        if (!StringUtils.hasText(normalizedIp)) {
            return "Unknown IP";
        }

        if (isLocalAddress(normalizedIp)) {
            return "Local Development Environment";
        }

        if (!StringUtils.hasText(token)) {
            return "Location unavailable";
        }

        try {
            IpInfoLookupResponse response = restClient.get()
                    .uri("/{ip}?token={token}", normalizedIp, token)
                    .retrieve()
                    .body(IpInfoLookupResponse.class);

            if (response == null || response.geo() == null) {
                return "Location unavailable";
            }

            String city = trimToNull(response.geo().city());
            String region = trimToNull(response.geo().region());
            String country = trimToNull(response.geo().country());
            return formatLocation(city, region, country);
        } catch (Exception e) {
            return "Location unavailable";
        }
    }

    private String normalizeIp(String ipAddress) {
        if (!StringUtils.hasText(ipAddress)) {
            return null;
        }
        String trimmed = ipAddress.trim();
        return "0:0:0:0:0:0:0:1".equals(trimmed) ? "::1" : trimmed;
    }

    private boolean isLocalAddress(String ipAddress) {
        return "127.0.0.1".equals(ipAddress)
                || "::1".equals(ipAddress)
                || ipAddress.startsWith("192.168.")
                || ipAddress.startsWith("10.")
                || ipAddress.startsWith("172.16.")
                || ipAddress.startsWith("172.17.")
                || ipAddress.startsWith("172.18.")
                || ipAddress.startsWith("172.19.")
                || ipAddress.startsWith("172.2")
                || ipAddress.startsWith("172.30.")
                || ipAddress.startsWith("172.31.");
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String formatLocation(String city, String region, String country) {
        StringBuilder builder = new StringBuilder();
        appendPart(builder, city);
        appendPart(builder, region);
        appendPart(builder, country);
        return builder.length() == 0 ? "Location unavailable" : builder.toString();
    }

    private void appendPart(StringBuilder builder, String value) {
        if (!StringUtils.hasText(value)) {
            return;
        }
        if (builder.length() > 0) {
            builder.append(", ");
        }
        builder.append(value);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record IpInfoLookupResponse(
            GeoInfo geo) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GeoInfo(
            String city,
            String region,
            String country) {
    }
}
