package com.banking.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Hashes PINs / passwords with SHA-256 instead of storing them as plain text.
 */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    public static String hash(String plainText) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(plainText.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }

    public static boolean matches(String plainText, String hashedText) {
        return hash(plainText).equals(hashedText);
    }
}
