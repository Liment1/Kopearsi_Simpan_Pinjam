package com.example.project_map.data

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

object HashingUtils {

    // This SALT must be exactly the same as the one used to generate the hashes in UserDatabase.
    private const val SALT = "koperasi-app-secret-salt"

    /**
     * Hashes a password using SHA-256 with a static salt and UTF-8 encoding.
     * @param password The plain-text password to hash.
     * @return The hashed password as a lowercase hexadecimal string.
     */
    fun hashPassword(password: String): String {
        // Combine password and salt, then convert to bytes using a specific encoding (UTF-8) for consistency.
        val bytes = (password + SALT).toByteArray(StandardCharsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        // Convert the byte array into a hexadecimal string.
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}