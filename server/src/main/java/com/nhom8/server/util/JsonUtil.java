package com.nhom8.server.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.util.ArrayList;
import java.util.List;

/**
 * Lớp tiện ích xử lý chuyển đổi JSON cho giao tiếp WebSocket.
 * Sử dụng thư viện Jackson theo tech.md.
 */
public class JsonUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T convert(Object obj, Class<T> clazz) {
        if (obj == null) return null;
        if (clazz.isInstance(obj)) return clazz.cast(obj);
        return mapper.convertValue(obj, clazz);
    }

    public static <T> List<T> convertList(Object obj, Class<T> elementClass) {
        if (obj == null) return new ArrayList<>();
        CollectionType listType = mapper.getTypeFactory().constructCollectionType(List.class, elementClass);
        try {
            // ObjectMapper.convertValue cannot be used directly with type reference sometimes, 
            // but we can serialize and deserialize
            String json = mapper.writeValueAsString(obj);
            return mapper.readValue(json, listType);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static ObjectMapper getMapper() { return mapper; }
}
