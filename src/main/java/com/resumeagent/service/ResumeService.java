package com.resumeagent.service;

import com.resumeagent.agent.ResumeModifierAgent;
import com.resumeagent.model.ClientRequirement;
import com.resumeagent.util.PDFUtil;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class ResumeService {
    
    private final FileStorageService fileStorage;
    private final ResumeModifierAgent modifierAgent;
    private final PDFUtil pdfUtil;
    
    public ResumeService(FileStorageService fileStorage, 
                        ResumeModifierAgent modifierAgent,
                        PDFUtil pdfUtil) {
        this.fileStorage = fileStorage;
        this.modifierAgent = modifierAgent;
        this.pdfUtil = pdfUtil;
    }
    
    public String optimizeResume(String candidateId, String clientId) throws IOException {
        // Load client requirement
        ClientRequirement requirement = fileStorage.loadJson("clients/" + clientId + ".json", ClientRequirement.class);
        
        // Load original resume
        String originalPath = fileStorage.getFullPath("candidates/resumes/original/" + candidateId + ".pdf");
        String originalText = pdfUtil.extractText(new File(originalPath));
        
        // Generate optimized resume text
        String optimizedText = modifierAgent.optimizeResume(originalText, requirement);
        
        // Save as new PDF
        String modifiedPath = fileStorage.getFullPath("candidates/resumes/modified/" + candidateId + "_" + clientId + ".pdf");
        pdfUtil.generatePDF(optimizedText, new File(modifiedPath));
        
        return modifiedPath;
    }
    
    public File getOriginalResume(String candidateId) {
        String path = fileStorage.getFullPath("candidates/resumes/original/" + candidateId + ".pdf");
        return new File(path);
    }
    
    public File getModifiedResume(String candidateId, String clientId) {
        String path = fileStorage.getFullPath("candidates/resumes/modified/" + candidateId + "_" + clientId + ".pdf");
        return new File(path);
    }
}
