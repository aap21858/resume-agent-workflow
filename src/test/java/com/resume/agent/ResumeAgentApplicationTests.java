package com.resume.agent;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.ai.openai.api-key=test-key"
})
class ResumeAgentApplicationTests {

    @Test
    void contextLoads() {
        // Test that Spring context loads successfully
    }
}
