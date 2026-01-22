package com.resume.agent.controller;

import com.resume.agent.dto.WorkflowRequest;
import com.resume.agent.dto.WorkflowResult;
import com.resume.agent.orchestrator.WorkflowOrchestrator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for workflow operations.
 */
@RestController
@RequestMapping("/api/workflow")
@RequiredArgsConstructor
@Slf4j
public class WorkflowController {
    
    private final WorkflowOrchestrator workflowOrchestrator;
    
    /**
     * Execute the complete workflow.
     */
    @PostMapping("/execute")
    public ResponseEntity<WorkflowResult> executeWorkflow(@Valid @RequestBody WorkflowRequest request) {
        log.info("Received workflow execution request");
        
        try {
            WorkflowResult result = workflowOrchestrator.execute(request);
            
            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            log.error("Workflow execution failed", e);
            
            WorkflowResult errorResult = WorkflowResult.builder()
                    .success(false)
                    .message("Workflow execution failed: " + e.getMessage())
                    .status("ERROR")
                    .build();
            
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }
    
    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Workflow service is running");
    }
}
