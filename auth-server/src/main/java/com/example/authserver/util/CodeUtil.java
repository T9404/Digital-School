package com.example.authserver.util;

import org.apache.commons.text.RandomStringGenerator;

import java.util.UUID;

public class CodeUtil {
    private static final int CODE_LENGTH = 6;

    private CodeUtil() {
        throw new UnsupportedOperationException("api.custom.exception.utils.instantiation");
    }

    public static String generateRandomUuid() {
        return UUID.randomUUID().toString();
    }

    public static String generateRandomCode() {
        RandomStringGenerator generator = new RandomStringGenerator.Builder()
                .withinRange('0', 'z')
                .build();
        return generator.generate(CODE_LENGTH);
    }
}
