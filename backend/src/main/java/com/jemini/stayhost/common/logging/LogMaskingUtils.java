package com.jemini.stayhost.common.logging;

public class LogMaskingUtils {

    private LogMaskingUtils() {
    }

    public static String maskName(final String name) {
        if (name == null || name.length() <= 1) {
            return name;
        }
        return name.charAt(0) + "*".repeat(name.length() - 1);
    }

    public static String maskPhone(final String phone) {
        if (phone == null || phone.length() < 8) {
            return phone;
        }
        return phone.substring(0, 3) + "-****-" + phone.substring(phone.length() - 4);
    }

    public static String maskEmail(final String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        final String[] parts = email.split("@");
        return parts[0].charAt(0) + "***@" + parts[1];
    }

    public static String mask(final String value) {
        if (value == null) {
            return null;
        }
        if (value.contains("@")) {
            return maskEmail(value);
        }
        if (value.matches("^\\d[\\d-]+\\d$")) {
            return maskPhone(value);
        }
        return maskName(value);
    }
}
