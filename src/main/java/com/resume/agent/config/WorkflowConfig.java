package com.resume.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the workflow system.
 */
@Configuration
@ConfigurationProperties(prefix = "resume-agent")
@Data
public class WorkflowConfig {
    
    private PdfConfig pdf = new PdfConfig();
    private WorkflowThresholdConfig workflow = new WorkflowThresholdConfig();
    
    @Data
    public static class PdfConfig {
        private long maxFileSize = 5242880; // 5MB default
    }
    
    @Data
    public static class WorkflowThresholdConfig {
        private int fitScoreThreshold = 60;
    }
}
