package com.java_template.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

public class JsonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String mapToJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            throw new RuntimeException("Error converting Map to JSON", e);
        }
    }

    public static Map<String, Object> jsonToMap(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Error converting JSON to Map", e);
        }
    }

    public static String toJson(Object data) {
        try {
            if (data instanceof String) {
                return (String) data;
            }
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            throw new RuntimeException("Error converting to JSON", e);
        }
    }

    public static String getJsonString(Object responseJson) {
        if (responseJson instanceof String) {
            return (String) responseJson;
        } else {
            return toJson(responseJson);
        }
    }

    public static JsonNode getJsonNode(Object object) {
        return objectMapper.valueToTree(object);
    }
}

