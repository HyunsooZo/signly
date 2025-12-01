package com.deally.common.util;

import de.huxhorn.sulky.ulid.ULID;

public class UlidGenerator {

    private static final ULID ulid = new ULID();

    private UlidGenerator() {
    }

    public static String generate() {
        return ulid.nextULID();
    }

    public static boolean isValid(String id) {
        if (id == null || id.isEmpty()) {
            return false;
        }

        // ULID는 26자의 Crockford's Base32 문자열
        if (id.length() != 26) {
            return false;
        }

        // ULID는 0-9, A-Z (I, L, O, U 제외) 문자만 사용
        String validChars = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";
        for (char c : id.toUpperCase().toCharArray()) {
            if (validChars.indexOf(c) == -1) {
                return false;
            }
        }

        return true;
    }
}
