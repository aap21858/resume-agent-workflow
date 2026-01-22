package com.resumeagent.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.File;

@Configuration
public class FileStorageConfig {
    
    @Value("${app.storage.base-path}")
    private String basePath;
    
    @PostConstruct
    public void init() {
        // Create directory structure at startup
        createDirectory(basePath + "/candidates");
        createDirectory(basePath + "/candidates/resumes/original");
        createDirectory(basePath + "/candidates/resumes/modified");
        createDirectory(basePath + "/clients");
        createDirectory(basePath + "/analyses");
        createDirectory(basePath + "/interview-prep");
    }
    
    private void createDirectory(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    public String getBasePath() {
        return basePath;
    }
}
