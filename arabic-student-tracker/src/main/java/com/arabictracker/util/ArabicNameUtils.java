package com.arabictracker.util;

import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class ArabicNameUtils {
    
    /**
     * Extract parent name from student full name
     * Student: [FirstName] [Father] [Grandfather] [2ndGrandfather]
     * Parent: [Father] [Grandfather] [2ndGrandfather]
     */
    public String extractParentName(String studentFullName) {
        String[] nameParts = studentFullName.trim().split("\\s+");
        
        if (nameParts.length < 2) {
            throw new IllegalArgumentException(
                "Student name must include at least first name and father name"
            );
        }
        
        // Skip first part (student's first name), take the rest
        return String.join(" ", Arrays.copyOfRange(nameParts, 1, nameParts.length));
    }
    
    /**
     * Extract first name (first word)
     */
    public String extractFirstName(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        return parts.length > 0 ? parts[0] : fullName;
    }
    
    /**
     * Extract father name (second word)
     */
    public String extractFatherName(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        return parts.length > 1 ? parts[1] : "";
    }
    
    /**
     * Normalize Arabic text for search:
     * - Remove diacritics (تشكيل)
     * - Normalize alef variations (أ إ آ → ا)
     * - Normalize ة → ه and ى → ي
     */
    public String normalizeForSearch(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        return text
            // Remove Arabic diacritics
            .replaceAll("[\\u064B-\\u065F\\u0670]", "")
            // Normalize alef variations
            .replaceAll("[أإآ]", "ا")
            // Normalize taa marbuta and alef maksura
            .replaceAll("ة", "ه")
            .replaceAll("ى", "ي")
            .trim()
            .toLowerCase();
    }
    
    /**
     * Clean phone number (remove spaces, dashes, parentheses)
     */
    public String cleanPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            return null;
        }
        return phone.replaceAll("[\\s\\-()]+", "");
    }
}