package com.vertyll.fastprod.shared.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Base64;
import java.util.Collections;
import java.util.List;

@Slf4j
public class JwtParser {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static List<String> extractRoles(String token) {
        if (token == null || token.isBlank()) {
            return Collections.emptyList();
        }

        try {
            // JWT format: header.payload.signature
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                log.warn("Invalid JWT token format");
                return Collections.emptyList();
            }

            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            JsonNode jsonNode = objectMapper.readTree(payload);

            JsonNode rolesNode = jsonNode.get("roles");
            if (rolesNode == null) {
                rolesNode = jsonNode.get("authorities");
            }
            if (rolesNode == null) {
                rolesNode = jsonNode.get("role");
            }

            if (rolesNode != null) {
                if (rolesNode.isArray()) {
                    return objectMapper.convertValue(rolesNode, 
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
                } else if (rolesNode.isTextual()) {
                    return List.of(rolesNode.asText());
                }
            }

            return Collections.emptyList();

        } catch (Exception e) {
            log.error("Failed to parse JWT token", e);
            return Collections.emptyList();
        }
    }

    public static String extractEmail(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return null;
            }

            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            JsonNode jsonNode = objectMapper.readTree(payload);

            JsonNode emailNode = jsonNode.get("sub");
            if (emailNode == null) {
                emailNode = jsonNode.get("email");
            }

            return emailNode != null ? emailNode.asText() : null;

        } catch (Exception e) {
            log.error("Failed to extract email from JWT token", e);
            return null;
        }
    }
}
