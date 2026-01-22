package com.resumeagent.service;

import com.resumeagent.config.FileStorageConfig;
import com.resumeagent.util.JsonUtil;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class FileStorageService {
    
    private final FileStorageConfig config;
    private final JsonUtil jsonUtil;
    
    public FileStorageService(FileStorageConfig config, JsonUtil jsonUtil) {
        this.config = config;
        this.jsonUtil = jsonUtil;
    }
    
    public <T> void saveJson(T object, String relativePath) throws IOException {
        String fullPath = config.getBasePath() + "/" + relativePath;
        jsonUtil.writeToFile(object, fullPath);
    }
    
    public <T> T loadJson(String relativePath, Class<T> clazz) throws IOException {
        String fullPath = config.getBasePath() + "/" + relativePath;
        return jsonUtil.readFromFile(fullPath, clazz);
    }
    
    public List<String> listFiles(String relativePath) throws IOException {
        String fullPath = config.getBasePath() + "/" + relativePath;
        File directory = new File(fullPath);
        
        if (!directory.exists() || !directory.isDirectory()) {
            return new ArrayList<>();
        }
        
        List<String> files = new ArrayList<>();
        try (Stream<Path> paths = Files.list(Paths.get(fullPath))) {
            paths.filter(Files::isRegularFile)
                 .forEach(path -> files.add(path.getFileName().toString()));
        }
        
        return files;
    }
    
    public boolean fileExists(String relativePath) {
        String fullPath = config.getBasePath() + "/" + relativePath;
        return new File(fullPath).exists();
    }
    
    public String getFullPath(String relativePath) {
        return config.getBasePath() + "/" + relativePath;
    }
}
