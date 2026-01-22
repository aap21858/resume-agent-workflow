package com.resume.agent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.resume.agent.config.FileStorageConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Service for handling file storage operations (JSON and file management).
 */
@Service
@Slf4j
public class FileStorageService {
    
    private final FileStorageConfig config;
    private final ObjectMapper objectMapper;
    
    public FileStorageService(FileStorageConfig config) {
        this.config = config;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        initializeDirectories();
    }
    
    /**
     * Initialize required directory structure.
     */
    private void initializeDirectories() {
        try {
            String basePath = config.getBasePath();
            Files.createDirectories(Paths.get(basePath, "requirements"));
            Files.createDirectories(Paths.get(basePath, "candidates"));
            Files.createDirectories(Paths.get(basePath, "resumes", "original"));
            Files.createDirectories(Paths.get(basePath, "resumes", "modified"));
            Files.createDirectories(Paths.get(basePath, "analysis"));
            Files.createDirectories(Paths.get(basePath, "interview-prep"));
            log.info("Initialized storage directories at: {}", basePath);
        } catch (IOException e) {
            log.error("Failed to create storage directories", e);
            throw new RuntimeException("Failed to initialize storage directories", e);
        }
    }
    
    /**
     * Save an object as JSON file.
     */
    public <T> void saveAsJson(T object, String directory, String filename) throws IOException {
        Path dirPath = Paths.get(config.getBasePath(), directory);
        Files.createDirectories(dirPath);
        
        Path filePath = dirPath.resolve(filename);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), object);
        log.debug("Saved JSON file: {}", filePath);
    }
    
    /**
     * Load an object from JSON file.
     */
    public <T> T loadFromJson(String directory, String filename, Class<T> clazz) throws IOException {
        Path filePath = Paths.get(config.getBasePath(), directory, filename);
        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + filePath);
        }
        T object = objectMapper.readValue(filePath.toFile(), clazz);
        log.debug("Loaded JSON file: {}", filePath);
        return object;
    }
    
    /**
     * Get the full path for a file in a specific directory.
     */
    public String getFilePath(String directory, String filename) {
        return Paths.get(config.getBasePath(), directory, filename).toString();
    }
    
    /**
     * Copy a file to the storage directory.
     */
    public String copyFile(Path source, String directory, String filename) throws IOException {
        Path dirPath = Paths.get(config.getBasePath(), directory);
        Files.createDirectories(dirPath);
        
        Path destination = dirPath.resolve(filename);
        Files.copy(source, destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        log.debug("Copied file from {} to {}", source, destination);
        return destination.toString();
    }
    
    /**
     * Check if a file exists.
     */
    public boolean fileExists(String directory, String filename) {
        Path filePath = Paths.get(config.getBasePath(), directory, filename);
        return Files.exists(filePath);
    }
    
    /**
     * Get base path.
     */
    public String getBasePath() {
        return config.getBasePath();
    }
}
