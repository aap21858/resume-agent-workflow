package com.resumeagent.service;

import com.resumeagent.model.Candidate;
import com.resumeagent.util.PDFUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CandidateService {
    
    private final FileStorageService fileStorage;
    private final PDFUtil pdfUtil;
    
    public CandidateService(FileStorageService fileStorage, PDFUtil pdfUtil) {
        this.fileStorage = fileStorage;
        this.pdfUtil = pdfUtil;
    }
    
    public Candidate uploadResume(MultipartFile file, String name, String email) throws IOException {
        String candidateId = UUID.randomUUID().toString();
        
        // Save the PDF file
        String pdfPath = fileStorage.getFullPath("candidates/resumes/original/" + candidateId + ".pdf");
        file.transferTo(new File(pdfPath));
        
        // Extract text to identify skills (basic extraction)
        String resumeText = pdfUtil.extractText(new File(pdfPath));
        List<String> skills = extractSkillsFromText(resumeText);
        
        // Create candidate profile
        Candidate candidate = Candidate.builder()
                .id(candidateId)
                .name(name)
                .email(email)
                .skills(skills)
                .yearsOfExperience(0) // Could be extracted from resume
                .originalResumeFilename(file.getOriginalFilename())
                .uploadedAt(LocalDateTime.now())
                .build();
        
        fileStorage.saveJson(candidate, "candidates/" + candidateId + ".json");
        
        return candidate;
    }
    
    public Candidate getCandidate(String candidateId) throws IOException {
        return fileStorage.loadJson("candidates/" + candidateId + ".json", Candidate.class);
    }
    
    public List<Candidate> getAllCandidates() {
        List<Candidate> candidates = new ArrayList<>();
        try {
            List<String> files = fileStorage.listFiles("candidates");
            for (String file : files) {
                if (file.endsWith(".json")) {
                    Candidate candidate = fileStorage.loadJson("candidates/" + file, Candidate.class);
                    candidates.add(candidate);
                }
            }
        } catch (IOException e) {
            // Return empty list on error
        }
        return candidates;
    }
    
    private List<String> extractSkillsFromText(String text) {
        // Simple keyword extraction - in production, this would be more sophisticated
        List<String> skills = new ArrayList<>();
        String lowerText = text.toLowerCase();
        
        String[] commonSkills = {"java", "spring", "spring boot", "aws", "docker", "kubernetes", 
                                  "python", "javascript", "react", "angular", "sql", "mongodb"};
        
        for (String skill : commonSkills) {
            if (lowerText.contains(skill)) {
                skills.add(skill);
            }
        }
        
        return skills;
    }
}
