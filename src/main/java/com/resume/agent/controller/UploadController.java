package com.resume.agent.controller;

import com.resume.agent.config.WorkflowConfig;
import com.resume.agent.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for file upload and download operations.
 */
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Slf4j
public class UploadController {
    
    private final FileStorageService fileStorageService;
    private final WorkflowConfig workflowConfig;
    
    /**
     * Upload a candidate resume PDF.
     */
    @PostMapping("/resume")
    public ResponseEntity<Map<String, String>> uploadResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "candidateId", required = false) String candidateId) {
        
        log.info("Received resume upload request: {}", file.getOriginalFilename());
        
        // Validation
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "File is empty")
            );
        }
        
        if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Only PDF files are allowed")
            );
        }
        
        if (file.getSize() > workflowConfig.getPdf().getMaxFileSize()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "File size exceeds maximum allowed size")
            );
        }
        
        try {
            // Generate candidate ID if not provided
            if (candidateId == null || candidateId.isBlank()) {
                candidateId = UUID.randomUUID().toString();
            }
            
            // Save file
            String filename = candidateId + ".pdf";
            Path tempPath = Files.createTempFile("resume-", ".pdf");
            file.transferTo(tempPath.toFile());
            
            String savedPath = fileStorageService.copyFile(tempPath, "resumes/original", filename);
            Files.deleteIfExists(tempPath);
            
            log.info("Resume uploaded successfully: {}", savedPath);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Resume uploaded successfully");
            response.put("candidateId", candidateId);
            response.put("filePath", savedPath);
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("Failed to upload resume", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "Failed to upload resume: " + e.getMessage())
            );
        }
    }
    
    /**
     * Download a modified resume.
     */
    @GetMapping("/download/resume/{candidateId}/{clientId}")
    public ResponseEntity<Resource> downloadModifiedResume(
            @PathVariable String candidateId,
            @PathVariable String clientId) {
        
        try {
            String filename = candidateId + "-" + clientId + ".pdf";
            String filePath = fileStorageService.getFilePath("resumes/modified", filename);
            
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(path);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + filename + "\"")
                    .body(resource);
            
        } catch (Exception e) {
            log.error("Failed to download resume", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
