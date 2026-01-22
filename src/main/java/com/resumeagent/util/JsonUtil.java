package com.resumeagent.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class JsonUtil {
    
    private final ObjectMapper objectMapper;
    
    public JsonUtil() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
    
    public <T> void writeToFile(T object, String filePath) throws IOException {
        objectMapper.writeValue(new File(filePath), object);
    }
    
    public <T> T readFromFile(String filePath, Class<T> clazz) throws IOException {
        return objectMapper.readValue(new File(filePath), clazz);
    }
    
    public String toJsonString(Object object) throws IOException {
        return objectMapper.writeValueAsString(object);
    }
    
    public <T> T fromJsonString(String json, Class<T> clazz) throws IOException {
        return objectMapper.readValue(json, clazz);
    }
}
