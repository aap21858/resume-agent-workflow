package com.resumeagent.agent;

import com.resumeagent.model.ClientRequirement;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ResumeModifierAgent implements Agent {
    
    private final ChatClient chatClient;
    
    public ResumeModifierAgent(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }
    
    @Override
    public String getAgentName() {
        return "Resume Modifier Agent";
    }
    
    public String optimizeResume(String originalResumeText, ClientRequirement requirement) {
        String promptText = """
            You are a professional resume writer. Tailor the following resume to highlight skills and experience
            that match the client requirements. Keep the same overall structure and information, but:
            1. Reorder sections to highlight matching experience first
            2. Emphasize skills that match required technologies
            3. Adjust terminology to match the client's language
            4. Add relevant keywords where appropriate
            5. Keep the resume professional and concise
            
            Client Requirements:
            Role: {role}
            Level: {level}
            Required Skills: {requiredSkills}
            Preferred Skills: {preferredSkills}
            
            Original Resume:
            {resumeText}
            
            Return the optimized resume text (not JSON, just the formatted resume text):
            """;
        
        PromptTemplate promptTemplate = new PromptTemplate(promptText);
        Map<String, Object> params = new HashMap<>();
        params.put("role", requirement.getRole());
        params.put("level", requirement.getLevel());
        params.put("requiredSkills", String.join(", ", requirement.getRequiredSkills()));
        params.put("preferredSkills", String.join(", ", requirement.getPreferredSkills()));
        params.put("resumeText", originalResumeText);
        
        Prompt prompt = promptTemplate.create(params);
        return chatClient.prompt(prompt).call().content();
    }
}
