package com.vertyll.fastprod.auth.util;

public final class UserAgentUtils {

    private static final String CHROME = "Chrome";
    private static final String FIREFOX = "Firefox";
    private static final String SAFARI = "Safari";
    private static final String EDGE = "Edge";
    private static final String OPERA = "Opera";
    private static final String UNKNOWN = "unknown";
    private static final String WINDOWS = "Windows";
    private static final String MAC = "Mac";
    private static final String LINUX = "Linux";
    private static final String ANDROID = "Android";
    private static final String I_OS = "iOS";
    private static final String I_PHONE = "iPhone";
    private static final String I_PAD = "iPad";

    private UserAgentUtils() {}

    public static String parseBrowser(String userAgent) {
        String ua = userAgent != null ? userAgent : UNKNOWN;
        if (ua.contains(CHROME)) return CHROME;
        if (ua.contains(FIREFOX)) return FIREFOX;
        if (ua.contains(SAFARI)) return SAFARI;
        if (ua.contains(EDGE)) return EDGE;
        if (ua.contains(OPERA) || ua.contains("OPR")) return OPERA;
        return UNKNOWN;
    }

    public static String parseOs(String userAgent) {
        String ua = userAgent != null ? userAgent : UNKNOWN;
        if (ua.contains(WINDOWS)) return WINDOWS;
        if (ua.contains(MAC)) return "macOS";
        if (ua.contains(LINUX)) return LINUX;
        if (ua.contains(ANDROID)) return ANDROID;
        if (ua.contains(I_OS) || ua.contains(I_PHONE) || ua.contains(I_PAD)) return I_OS;
        return UNKNOWN;
    }
}
