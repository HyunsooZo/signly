package com.deally.common.util;

public final class EmailUtils {

    private EmailUtils() {
        throw new AssertionError("Utility class cannot be instantiated");
    }

    public static String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
