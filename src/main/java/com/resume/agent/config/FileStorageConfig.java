package com.resume.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for file storage.
 */
@Configuration
@ConfigurationProperties(prefix = "resume-agent.storage")
@Data
public class FileStorageConfig {
    
    /**
     * Base path for file storage.
     */
    private String basePath = "./data";
    
    /**
     * Get the full path for a specific storage type.
     */
    public String getPath(String type) {
        return basePath + "/" + type;
    }
}
