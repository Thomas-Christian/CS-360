package com.zybooks.weightlogger.Utilities;

import java.nio.charset.StandardCharsets;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import java.util.Base64;

/**
 * Utility class for securely hashing and verifying passwords.
 * Implements salted SHA-256 hashing for increased security.
 * Provides methods for creating password hashes and verifying passwords against stored hashes.
 */
public class PasswordHash {
    // Number of bytes in the salt
    private static final int SALT_LENGTH = 16;

    /**
     * Generates a cryptographically secure random salt for password hashing.
     *
     * @return A random salt as a byte array
     */
    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

    /**
     * Hashes a password with a randomly generated salt.
     * Uses SHA-256 algorithm to create a secure hash.
     *
     * @param password The password to hash
     * @return A string in the format "salt:hash" where both salt and hash are Base64 encoded,
     *         or null if an error occurs during hashing
     */
    public static String hashPassword(String password) {
        try {
            // Generate a new salt
            byte[] salt = generateSalt();

            // Hash the password with the salt
            byte[] hash = hashWithSalt(password, salt);

            // Encode salt and hash to Base64 for storage
            String saltString = Base64.getEncoder().encodeToString(salt);
            String hashString = Base64.getEncoder().encodeToString(hash);

            // Return the combined string
            return saltString + ":" + hashString;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Hashes a password with a provided salt using the SHA-256 algorithm.
     *
     * @param password The password to hash
     * @param salt The salt to use in the hashing process
     * @return The hashed password as a byte array
     * @throws NoSuchAlgorithmException If the SHA-256 algorithm is not available
     */
    private static byte[] hashWithSalt(String password, byte[] salt) throws NoSuchAlgorithmException {
        // Create a SHA-256 MessageDigest instance
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        // Add the salt to the digest
        digest.update(salt);

        // Add the password bytes
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);

        // Get the hash
        return digest.digest(passwordBytes);
    }

    /**
     * Verifies that a plain text password matches a stored salted hash.
     * Uses a constant-time comparison to prevent timing attacks.
     *
     * @param plainTextPassword The plain text password to verify
     * @param storedHash The stored hash in the format "salt:hash"
     * @return True if the passwords match, false otherwise
     */
    public static boolean verifyPassword(String plainTextPassword, String storedHash) {
        try {
            // Split the stored hash into salt and hash
            String[] parts = storedHash.split(":");

            if (parts.length != 2) {
                return false; // Invalid format
            }

            // Decode the salt and hash from Base64
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] hash = Base64.getDecoder().decode(parts[1]);

            // Hash the plain text password with the same salt
            byte[] testHash = hashWithSalt(plainTextPassword, salt);

            // Compare the hashes using constant-time comparison
            if (hash.length != testHash.length) {
                return false;
            }

            int result = 0;
            for (int i = 0; i < hash.length; i++) {
                result |= hash[i] ^ testHash[i]; // XOR will be 0 if bytes are the same
            }

            return result == 0; // If all bytes matched, result will be 0
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}