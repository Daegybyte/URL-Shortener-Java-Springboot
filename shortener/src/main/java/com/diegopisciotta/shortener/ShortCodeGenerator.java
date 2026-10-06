package com.diegopisciotta.shortener;

import java.security.SecureRandom;

public final class ShortCodeGenerator {

    private static final char[] ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
            .toCharArray();
    private static final int DEFAULT_LENGTH = 7;
    private static final int MAX_LENGTH = 16;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private ShortCodeGenerator() {
    }

    /**
     * Generates cryptographically secure random short code
     */
    public static String randomShortCode(int length) {
        // Fix quietly instead of failing
        if (length < 1) {
            length = DEFAULT_LENGTH;
        } else if (length > MAX_LENGTH) {
            length = MAX_LENGTH;
        }
        char[] out = new char[length];
        for (int i = 0; i < length; i++) {
            out[i] = ALPHABET[SECURE_RANDOM.nextInt(ALPHABET.length)];
        }
        return new String(out);
    }

}
