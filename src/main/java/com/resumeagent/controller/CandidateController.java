package com.resumeagent.controller;

import com.resumeagent.model.Candidate;
import com.resumeagent.service.CandidateService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/candidates")
public class CandidateController {
    
    private final CandidateService candidateService;
    
    public CandidateController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }
    
    @PostMapping("/upload")
    public ResponseEntity<Candidate> uploadResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam("email") String email) {
        try {
            Candidate candidate = candidateService.uploadResume(file, name, email);
            return ResponseEntity.ok(candidate);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Candidate>> getAllCandidates() {
        List<Candidate> candidates = candidateService.getAllCandidates();
        return ResponseEntity.ok(candidates);
    }
    
    @GetMapping("/{candidateId}")
    public ResponseEntity<Candidate> getCandidate(@PathVariable String candidateId) {
        try {
            Candidate candidate = candidateService.getCandidate(candidateId);
            return ResponseEntity.ok(candidate);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
