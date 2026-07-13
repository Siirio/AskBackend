package kz.ask.shared.util;

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TwoGisLinkParser {

    private static final Pattern COORDS_IN_PATH = Pattern.compile(
            "2gis\\.\\w+/(?:\\w+/)?(?:geo|firm)/[^/]*?/([\\d.]+)%2C([\\d.]+)");
    private static final Pattern COORDS_IN_QUERY = Pattern.compile(
            "[?&]m=([\\d.]+)%2C([\\d.]+)");
    private static final Pattern DIRECT_COORDS = Pattern.compile(
            "^\\s*(-?\\d+\\.?\\d*)\\s*[,;\\s]\\s*(-?\\d+\\.?\\d*)\\s*$");
    private static final Pattern SHORT_LINK = Pattern.compile(
            "go\\.2gis\\.com/\\w+");

    private TwoGisLinkParser() {}

    public static ParsedCoordinates parse(String input) {
        if (input == null || input.isBlank()) return null;

        String trimmed = input.trim();

        Matcher direct = DIRECT_COORDS.matcher(trimmed);
        if (direct.matches()) {
            double lat = Double.parseDouble(direct.group(1));
            double lng = Double.parseDouble(direct.group(2));
            if (isValid(lat, lng)) return new ParsedCoordinates(lat, lng);
        }

        String url = trimmed;
        if (SHORT_LINK.matcher(trimmed).find()) {
            url = resolveRedirect(trimmed);
            if (url == null) return null;
        }

        Matcher pathMatch = COORDS_IN_PATH.matcher(url);
        if (pathMatch.find()) {
            double lng = Double.parseDouble(pathMatch.group(1));
            double lat = Double.parseDouble(pathMatch.group(2));
            if (isValid(lat, lng)) return new ParsedCoordinates(lat, lng);
        }

        Matcher queryMatch = COORDS_IN_QUERY.matcher(url);
        if (queryMatch.find()) {
            double lng = Double.parseDouble(queryMatch.group(1));
            double lat = Double.parseDouble(queryMatch.group(2));
            if (isValid(lat, lng)) return new ParsedCoordinates(lat, lng);
        }

        return null;
    }

    private static String resolveRedirect(String shortUrl) {
        try {
            HttpURLConnection conn = (HttpURLConnection) URI.create(
                    shortUrl.startsWith("http") ? shortUrl : "https://" + shortUrl).toURL().openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.connect();
            int status = conn.getResponseCode();
            if (status >= 300 && status < 400) {
                String location = conn.getHeaderField("Location");
                conn.disconnect();
                return location;
            }
            conn.disconnect();
        } catch (Exception ignored) {
        }
        return null;
    }

    private static boolean isValid(double lat, double lng) {
        return lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180;
    }

    public record ParsedCoordinates(double latitude, double longitude) {}
}
