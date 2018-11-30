package com.hunter.wallet.utils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

public class JsonUtils {

    // 定义jackson对象
    private static ObjectMapper MAPPER;

    private static ObjectMapper getMAPPER() {
        if (MAPPER == null) {
            MAPPER = new ObjectMapper();
            MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
        return MAPPER;
    }

    public static String objectToJson(Object data) {
        try {
            String string = getMAPPER().writeValueAsString(data);
            return string;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T jsonToPojo(String jsonData, Class<T> beanType) {
        try {
            T t = getMAPPER().readValue(jsonData, beanType);
            return t;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> List<T> jsonToList(String jsonData, Class<T> beanType) {
        JavaType javaType = getMAPPER().getTypeFactory().constructParametricType(List.class, beanType);
        try {
            List<T> list = getMAPPER().readValue(jsonData, javaType);
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static <T> T readValue(String json, TypeReference<T> typeReference) {
        try {
            if (StringUtils.hasText(json)) {
                return getMAPPER().readValue(json, typeReference);
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
