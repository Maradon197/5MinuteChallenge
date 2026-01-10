package com.example.a5minutechallenge.util.fileutil;

public class fileutil {

    /**
     * Sanitizes a filename by removing path separators and other dangerous characters
     * @param fileName The filename to sanitize
     * @return The sanitized filename
     */
    public static String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "";
        }

        // Remove path separators and other dangerous characters
        String sanitized = fileName.replaceAll("[/\\\\:*?\"<>|]", "_");

        // Remove leading/trailing dots and spaces
        sanitized = sanitized.replaceAll("^\\.+", "").replaceAll("\\.+$", "");
        sanitized = sanitized.trim();

        // Ensure filename is not empty after sanitization
        if (sanitized.isEmpty()) {
            return "";
        }

        return sanitized;
    }

}
