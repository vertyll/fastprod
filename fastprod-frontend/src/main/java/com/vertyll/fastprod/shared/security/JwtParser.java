package com.vertyll.fastprod.shared.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.nio.charset.StandardCharsets;

@Slf4j
public class JwtParser {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static List<String> extractRoles(String token) {
        JsonNode jsonNode = parsePayload(token);
        if (jsonNode == null) {
            return Collections.emptyList();
        }

        try {
            JsonNode rolesNode = jsonNode.get("roles");
            if (rolesNode == null) rolesNode = jsonNode.get("authorities");
            if (rolesNode == null) rolesNode = jsonNode.get("role");

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
            log.error("Failed to extract roles from JWT", e);
            return Collections.emptyList();
        }
    }

    public static String extractEmail(String token) {
        JsonNode jsonNode = parsePayload(token);
        if (jsonNode == null) {
            return null;
        }

        JsonNode emailNode = jsonNode.get("sub");
        if (emailNode == null) {
            emailNode = jsonNode.get("email");
        }
        return emailNode != null ? emailNode.asText() : null;
    }

    private static JsonNode parsePayload(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            String[] parts = token.split("\\.", -1);

            if (parts.length < 2) {
                log.warn("Invalid JWT token format");
                return null;
            }

            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            return objectMapper.readTree(payload);
        } catch (Exception e) {
            log.error("Failed to parse JWT payload", e);
            return null;
        }
    }
}
