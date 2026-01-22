package com.resumeagent.service;

import com.resumeagent.agent.ResumeAnalyzerAgent;
import com.resumeagent.model.AnalysisResult;
import com.resumeagent.model.ClientRequirement;
import com.resumeagent.util.PDFUtil;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnalysisService {
    
    private final FileStorageService fileStorage;
    private final ResumeAnalyzerAgent analyzerAgent;
    private final PDFUtil pdfUtil;
    
    public AnalysisService(FileStorageService fileStorage, 
                          ResumeAnalyzerAgent analyzerAgent,
                          PDFUtil pdfUtil) {
        this.fileStorage = fileStorage;
        this.analyzerAgent = analyzerAgent;
        this.pdfUtil = pdfUtil;
    }
    
    public AnalysisResult analyzeCandidate(String candidateId, String clientId) throws IOException {
        // Load client requirement
        ClientRequirement requirement = fileStorage.loadJson("clients/" + clientId + ".json", ClientRequirement.class);
        
        // Load and extract resume text
        String resumePath = fileStorage.getFullPath("candidates/resumes/original/" + candidateId + ".pdf");
        String resumeText = pdfUtil.extractText(new File(resumePath));
        
        // Analyze fit
        AnalysisResult result = analyzerAgent.analyze(candidateId, resumeText, requirement);
        
        // Save analysis result
        fileStorage.saveJson(result, "analyses/" + candidateId + "_" + clientId + ".json");
        
        return result;
    }
    
    public AnalysisResult getAnalysis(String candidateId, String clientId) throws IOException {
        return fileStorage.loadJson("analyses/" + candidateId + "_" + clientId + ".json", AnalysisResult.class);
    }
    
    public List<AnalysisResult> getAllAnalyses() {
        List<AnalysisResult> analyses = new ArrayList<>();
        try {
            List<String> files = fileStorage.listFiles("analyses");
            for (String file : files) {
                if (file.endsWith(".json")) {
                    AnalysisResult analysis = fileStorage.loadJson("analyses/" + file, AnalysisResult.class);
                    analyses.add(analysis);
                }
            }
        } catch (IOException e) {
            // Return empty list on error
        }
        return analyses;
    }
}
