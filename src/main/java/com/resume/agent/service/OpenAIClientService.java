package com.resume.agent.service;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Service wrapper for OpenAI API interactions.
 */
@Service
@Slf4j
public class OpenAIClientService {
    
    private final OpenAiService openAiService;
    private final String model;
    
    public OpenAIClientService(
            @Value("${spring.ai.openai.api-key:}") String apiKey,
            @Value("${spring.ai.openai.chat.options.model:gpt-4-turbo}") String model) {
        this.model = model;
        
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("OpenAI API key not configured. Service will not function properly.");
            this.openAiService = null;
        } else {
            this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(90));
        }
    }
    
    /**
     * Generate a response from OpenAI with the given prompt.
     */
    public String generateResponse(String systemPrompt, String userPrompt) {
        log.debug("Generating OpenAI response");
        
        if (openAiService == null) {
            throw new RuntimeException("OpenAI service not initialized. Please configure OPENAI_API_KEY.");
        }
        
        try {
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), systemPrompt));
            messages.add(new ChatMessage(ChatMessageRole.USER.value(), userPrompt));
            
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(messages)
                    .temperature(0.7)
                    .maxTokens(4000)
                    .build();
            
            ChatCompletionResult result = openAiService.createChatCompletion(request);
            String response = result.getChoices().get(0).getMessage().getContent();
            
            log.debug("Received response with {} characters", response.length());
            return response;
        } catch (Exception e) {
            log.error("Error calling OpenAI API", e);
            throw new RuntimeException("Failed to generate AI response", e);
        }
    }
    
    /**
     * Generate a response with retry logic.
     */
    public String generateResponseWithRetry(String systemPrompt, String userPrompt, int maxRetries) {
        int attempt = 0;
        Exception lastException = null;
        
        while (attempt < maxRetries) {
            try {
                return generateResponse(systemPrompt, userPrompt);
            } catch (Exception e) {
                lastException = e;
                attempt++;
                log.warn("Attempt {} failed, retrying...", attempt);
                
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(1000 * attempt); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        throw new RuntimeException("Failed after " + maxRetries + " attempts", lastException);
    }
}
