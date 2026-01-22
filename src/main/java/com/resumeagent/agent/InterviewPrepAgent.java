package com.resumeagent.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeagent.model.ClientRequirement;
import com.resumeagent.model.InterviewPrep;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class InterviewPrepAgent implements Agent {
    
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    
    public InterviewPrepAgent(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public String getAgentName() {
        return "Interview Prep Agent";
    }
    
    public InterviewPrep generateQuestions(String candidateId, ClientRequirement requirement) {
        String promptText = """
            Generate interview preparation materials for a candidate.
            Return ONLY a valid JSON object (no markdown, no code blocks) with:
            {
              "technical_questions": ["question1", "question2"],
              "behavioral_questions": ["question1", "question2"],
              "talking_points": ["point1", "point2"],
              "tips": ["tip1", "tip2"]
            }
            
            Client Requirements:
            Role: {role}
            Level: {level}
            Required Skills: {requiredSkills}
            Years of Experience: {years}
            """;
        
        PromptTemplate promptTemplate = new PromptTemplate(promptText);
        Map<String, Object> params = new HashMap<>();
        params.put("role", requirement.getRole());
        params.put("level", requirement.getLevel());
        params.put("requiredSkills", String.join(", ", requirement.getRequiredSkills()));
        params.put("years", requirement.getYearsOfExperience());
        
        Prompt prompt = promptTemplate.create(params);
        String response = chatClient.prompt(prompt).call().content();
        
        return parseInterviewPrepResponse(response, candidateId, requirement.getId());
    }
    
    private InterviewPrep parseInterviewPrepResponse(String jsonResponse, String candidateId, String clientId) {
        try {
            // Clean up the response
            String cleanedJson = jsonResponse.trim();
            if (cleanedJson.startsWith("```json")) {
                cleanedJson = cleanedJson.substring(7);
            }
            if (cleanedJson.startsWith("```")) {
                cleanedJson = cleanedJson.substring(3);
            }
            if (cleanedJson.endsWith("```")) {
                cleanedJson = cleanedJson.substring(0, cleanedJson.length() - 3);
            }
            cleanedJson = cleanedJson.trim();
            
            JsonNode jsonNode = objectMapper.readTree(cleanedJson);
            
            List<String> technicalQuestions = new ArrayList<>();
            if (jsonNode.has("technical_questions")) {
                jsonNode.get("technical_questions").forEach(node -> technicalQuestions.add(node.asText()));
            }
            
            List<String> behavioralQuestions = new ArrayList<>();
            if (jsonNode.has("behavioral_questions")) {
                jsonNode.get("behavioral_questions").forEach(node -> behavioralQuestions.add(node.asText()));
            }
            
            List<String> talkingPoints = new ArrayList<>();
            if (jsonNode.has("talking_points")) {
                jsonNode.get("talking_points").forEach(node -> talkingPoints.add(node.asText()));
            }
            
            List<String> tips = new ArrayList<>();
            if (jsonNode.has("tips")) {
                jsonNode.get("tips").forEach(node -> tips.add(node.asText()));
            }
            
            return InterviewPrep.builder()
                    .id(UUID.randomUUID().toString())
                    .candidateId(candidateId)
                    .clientId(clientId)
                    .technicalQuestions(technicalQuestions)
                    .behavioralQuestions(behavioralQuestions)
                    .talkingPoints(talkingPoints)
                    .tips(tips)
                    .generatedAt(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            // Fallback
            return InterviewPrep.builder()
                    .id(UUID.randomUUID().toString())
                    .candidateId(candidateId)
                    .clientId(clientId)
                    .technicalQuestions(Arrays.asList("Unable to generate questions"))
                    .behavioralQuestions(new ArrayList<>())
                    .talkingPoints(new ArrayList<>())
                    .tips(new ArrayList<>())
                    .generatedAt(LocalDateTime.now())
                    .build();
        }
    }
}
