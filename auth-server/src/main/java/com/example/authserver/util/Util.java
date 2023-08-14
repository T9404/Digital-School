package com.example.authserver.util;

import org.apache.commons.text.RandomStringGenerator;

import java.util.UUID;

public class Util {
    private static final int CODE_LENGTH = 6;

    private Util() {
        throw new UnsupportedOperationException("Cannot instantiate a Util class");
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
