package com.vertyll.fastprod.auth.util;

public final class UserAgentUtils {

    private UserAgentUtils() {}

    public static String parseBrowser(String userAgent) {
        String ua = userAgent != null ? userAgent : "unknown";
        if (ua.contains("Chrome")) return "Chrome";
        if (ua.contains("Firefox")) return "Firefox";
        if (ua.contains("Safari") && !ua.contains("Chrome")) return "Safari";
        if (ua.contains("Edge")) return "Edge";
        if (ua.contains("Opera") || ua.contains("OPR")) return "Opera";
        return "Unknown";
    }

    public static String parseOs(String userAgent) {
        String ua = userAgent != null ? userAgent : "unknown";
        if (ua.contains("Windows")) return "Windows";
        if (ua.contains("Mac")) return "macOS";
        if (ua.contains("Linux")) return "Linux";
        if (ua.contains("Android")) return "Android";
        if (ua.contains("iOS") || ua.contains("iPhone") || ua.contains("iPad")) return "iOS";
        return "Unknown";
    }
}
