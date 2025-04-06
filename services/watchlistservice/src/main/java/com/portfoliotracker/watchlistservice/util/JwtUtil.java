package com.portfoliotracker.watchlistservice.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.Map;

/**
* Utility class for decoding and extracting claims from JWT
*/
public class JwtUtil {

    public static String getJwtSub(String token){
        return getAllJwtClaims(token).get("sub").toString();
    }

    /**
     * Decodes the JWT payload and extracts all claims as a Map.
     *
     * @param token the JWT string
     * @return a Map containing all claims from the JWT payload
     * @throws RuntimeException if decoding or parsing fails
     */
    public static Map<String, Object> getAllJwtClaims(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid JWT Token");
            }

            String claimsSection = new String(Base64.getUrlDecoder().decode(parts[1]));

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> claims = objectMapper.readValue(
                    claimsSection,
                    new TypeReference<Map<String, Object>>() {}
            );

            return claims;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JWT", e);
        }
    }
}