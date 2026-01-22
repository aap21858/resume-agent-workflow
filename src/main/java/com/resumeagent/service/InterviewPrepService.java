package com.resumeagent.service;

import com.resumeagent.agent.InterviewPrepAgent;
import com.resumeagent.model.ClientRequirement;
import com.resumeagent.model.InterviewPrep;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class InterviewPrepService {
    
    private final FileStorageService fileStorage;
    private final InterviewPrepAgent prepAgent;
    
    public InterviewPrepService(FileStorageService fileStorage, InterviewPrepAgent prepAgent) {
        this.fileStorage = fileStorage;
        this.prepAgent = prepAgent;
    }
    
    public InterviewPrep generateInterviewPrep(String candidateId, String clientId) throws IOException {
        // Load client requirement
        ClientRequirement requirement = fileStorage.loadJson("clients/" + clientId + ".json", ClientRequirement.class);
        
        // Generate interview prep
        InterviewPrep prep = prepAgent.generateQuestions(candidateId, requirement);
        
        // Save interview prep
        fileStorage.saveJson(prep, "interview-prep/" + candidateId + "_" + clientId + ".json");
        
        return prep;
    }
    
    public InterviewPrep getInterviewPrep(String candidateId, String clientId) throws IOException {
        return fileStorage.loadJson("interview-prep/" + candidateId + "_" + clientId + ".json", InterviewPrep.class);
    }
    
    public List<InterviewPrep> getAllInterviewPreps() {
        List<InterviewPrep> preps = new ArrayList<>();
        try {
            List<String> files = fileStorage.listFiles("interview-prep");
            for (String file : files) {
                if (file.endsWith(".json")) {
                    InterviewPrep prep = fileStorage.loadJson("interview-prep/" + file, InterviewPrep.class);
                    preps.add(prep);
                }
            }
        } catch (IOException e) {
            // Return empty list on error
        }
        return preps;
    }
}
