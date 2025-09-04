package com.bifrost.demo.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class JSONUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JSONUtil() {
    }

    public static String toJsonString(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to JSON string", e);
        }
    }

    public static Object parseIfJson(String input) {
        if (input == null || input.isBlank()) return null;

        try {
            return MAPPER.readTree(input);
        } catch (Exception e) {
            return input;
        }
    }
}
