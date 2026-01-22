package com.resumeagent.controller;

import com.resumeagent.model.AnalysisResult;
import com.resumeagent.model.InterviewPrep;
import com.resumeagent.orchestrator.WorkflowOrchestrator;
import com.resumeagent.service.AnalysisService;
import com.resumeagent.service.InterviewPrepService;
import com.resumeagent.service.ResumeService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AnalysisController {
    
    private final AnalysisService analysisService;
    private final ResumeService resumeService;
    private final InterviewPrepService interviewPrepService;
    private final WorkflowOrchestrator orchestrator;
    
    public AnalysisController(AnalysisService analysisService,
                             ResumeService resumeService,
                             InterviewPrepService interviewPrepService,
                             WorkflowOrchestrator orchestrator) {
        this.analysisService = analysisService;
        this.resumeService = resumeService;
        this.interviewPrepService = interviewPrepService;
        this.orchestrator = orchestrator;
    }
    
    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResult> analyzeCandidate(@RequestBody Map<String, String> request) {
        try {
            String candidateId = request.get("candidateId");
            String clientId = request.get("clientId");
            AnalysisResult result = analysisService.analyzeCandidate(candidateId, clientId);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/analysis/{candidateId}/{clientId}")
    public ResponseEntity<AnalysisResult> getAnalysis(
            @PathVariable String candidateId,
            @PathVariable String clientId) {
        try {
            AnalysisResult result = analysisService.getAnalysis(candidateId, clientId);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/analyses")
    public ResponseEntity<List<AnalysisResult>> getAllAnalyses() {
        List<AnalysisResult> analyses = analysisService.getAllAnalyses();
        return ResponseEntity.ok(analyses);
    }
    
    @PostMapping("/optimize-resume")
    public ResponseEntity<Map<String, String>> optimizeResume(@RequestBody Map<String, String> request) {
        try {
            String candidateId = request.get("candidateId");
            String clientId = request.get("clientId");
            String path = resumeService.optimizeResume(candidateId, clientId);
            return ResponseEntity.ok(Map.of("path", path, "message", "Resume optimized successfully"));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/resume/original/{candidateId}")
    public ResponseEntity<Resource> getOriginalResume(@PathVariable String candidateId) {
        try {
            File file = resumeService.getOriginalResume(candidateId);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/resume/modified/{candidateId}/{clientId}")
    public ResponseEntity<Resource> getModifiedResume(
            @PathVariable String candidateId,
            @PathVariable String clientId) {
        try {
            File file = resumeService.getModifiedResume(candidateId, clientId);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/interview-prep")
    public ResponseEntity<InterviewPrep> generateInterviewPrep(@RequestBody Map<String, String> request) {
        try {
            String candidateId = request.get("candidateId");
            String clientId = request.get("clientId");
            InterviewPrep prep = interviewPrepService.generateInterviewPrep(candidateId, clientId);
            return ResponseEntity.ok(prep);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/interview-prep/{candidateId}/{clientId}")
    public ResponseEntity<InterviewPrep> getInterviewPrep(
            @PathVariable String candidateId,
            @PathVariable String clientId) {
        try {
            InterviewPrep prep = interviewPrepService.getInterviewPrep(candidateId, clientId);
            return ResponseEntity.ok(prep);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/workflow/process")
    public ResponseEntity<WorkflowOrchestrator.WorkflowResult> processWorkflow(@RequestBody Map<String, String> request) {
        try {
            String candidateId = request.get("candidateId");
            String clientId = request.get("clientId");
            WorkflowOrchestrator.WorkflowResult result = orchestrator.processCandidate(candidateId, clientId);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
