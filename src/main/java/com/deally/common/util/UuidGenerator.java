package com.deally.common.util;

import java.util.UUID;

public class UuidGenerator {

    private UuidGenerator() {
    }

    public static String generate() {
        return UUID.randomUUID().toString();
    }

    public static boolean isValid(String uuid) {
        if (uuid == null || uuid.isEmpty()) {
            return false;
        }

        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}