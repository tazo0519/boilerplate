package com.example.boilerplate.common.mask;

public final class Mask {

    private Mask() {
    }

    public static String rrn(String value) {
        if (value == null) {
            return null;
        }
        String digits = value.replace("-", "");
        if (digits.length() < 7) {
            return value;
        }
        return digits.substring(0, 6) + "-" + digits.charAt(6) + "******";
    }

    public static String phone(String value) {
        if (value == null) {
            return null;
        }
        String digits = value.replaceAll("[^0-9]", "");
        if (digits.length() < 8) {
            return value;
        }
        String prefix = digits.substring(0, 3);
        String suffix = digits.substring(digits.length() - 4);
        return prefix + "-****-" + suffix;
    }

    public static String email(String value) {
        if (value == null) {
            return null;
        }
        int at = value.indexOf('@');
        if (at <= 1) {
            return value;
        }
        return value.charAt(0) + "***" + value.substring(at);
    }
}
