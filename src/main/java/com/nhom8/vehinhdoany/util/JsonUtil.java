package com.nhom8.vehinhdoany.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp tiện ích xử lý chuyển đổi JSON cho giao tiếp WebSocket.
 * Sử dụng thư viện Gson (giữ tương thích với React Client).
 */
public class JsonUtil {

    private static final Gson gson = new GsonBuilder().serializeNulls().create();

    public static String toJson(Object obj) { return gson.toJson(obj); }

    public static <T> T fromJson(String json, Class<T> clazz) { return gson.fromJson(json, clazz); }

    public static <T> T convert(Object obj, Class<T> clazz) {
        if (obj == null) return null;
        if (clazz.isInstance(obj)) return clazz.cast(obj);
        String json = gson.toJson(obj);
        return gson.fromJson(json, clazz);
    }

    public static <T> List<T> convertList(Object obj, Class<T> elementClass) {
        if (obj == null) return new ArrayList<>();
        String json = gson.toJson(obj);
        Type listType = TypeToken.getParameterized(List.class, elementClass).getType();
        List<T> result = gson.fromJson(json, listType);
        return result != null ? result : new ArrayList<>();
    }

    public static Gson getGson() { return gson; }
}
