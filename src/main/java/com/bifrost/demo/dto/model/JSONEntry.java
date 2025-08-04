package com.bifrost.demo.dto.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JSONEntry {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final JsonNode node;

    private JSONEntry(JsonNode node) {
        this.node = node;
    }

    public static JSONEntry parse(String json) {
        try {
            JsonNode root = MAPPER.readTree(json);
            return new JSONEntry(root);
        } catch (Exception e) {
            return new JSONEntry(null);
        }
    }

    public JSONEntry get(String key) {
        if (node != null && node.has(key)) {
            return new JSONEntry(node.get(key));
        } else {
            return new JSONEntry(null);
        }
    }

    public JSONEntry get(int index) {
        if (node != null && node.isArray() && index < node.size()) {
            return new JSONEntry(node.get(index));
        } else {
            return new JSONEntry(null); // safe null
        }
    }

    public String asString() {
        return (node != null && node.isValueNode()) ? node.asText() : null;
    }

    public Integer asInt() {
        return (node != null && node.isInt()) ? node.asInt() : null;
    }

    public Boolean asBoolean() {
        return (node != null && node.isBoolean()) ? node.asBoolean() : null;
    }

    public boolean isObject() {
        return node != null && node.isObject();
    }

    public boolean isArray() {
        return node != null && node.isArray();
    }

    public boolean exists() {
        return node != null && !node.isNull();
    }

    public List<Object> asJavaList() {
        List<Object> list = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode element : node) {
                list.add(convertJsonNodeToJava(element));
            }
        }
        return list;
    }

    public Map<String, Object> asJavaMap() {
        if (node != null && node.isObject()) {
            Map<String, Object> map = new LinkedHashMap<>();
            node.fieldNames().forEachRemaining(field -> {
                map.put(field, convertJsonNodeToJava(node.get(field)));
            });
            return map;
        }
        return null;
    }

    public Object toJavaObject() {
        return convertJsonNodeToJava(this.node);
    }

    private Object convertJsonNodeToJava(JsonNode node) {
        if (node == null || node.isNull()) return null;
        if (node.isTextual()) return node.asText();
        if (node.isInt()) return node.asInt();
        if (node.isLong()) return node.asLong();
        if (node.isDouble() || node.isFloat()) return node.asDouble();
        if (node.isBoolean()) return node.asBoolean();
        if (node.isArray()) {
            List<Object> list = new ArrayList<>();
            for (JsonNode item : node) {
                list.add(convertJsonNodeToJava(item));
            }
            return list;
        }
        if (node.isObject()) {
            Map<String, Object> map = new LinkedHashMap<>();
            node.fieldNames().forEachRemaining(field -> {
                map.put(field, convertJsonNodeToJava(node.get(field)));
            });
            return map;
        }
        return node.toString();
    }

    @Override
    public String toString() {
        return node != null ? node.toString() : "null";
    }
}
