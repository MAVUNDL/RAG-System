package com.example.demo.ai.model;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiServiceConfig {

    @Bean
    public Assistant assistant() {
        ChatModel model = OpenAiChatModel.builder()
                .apiKey(System.getenv("OPEN_API_KEY"))
                .modelName(OpenAiChatModelName.GPT_4_O_MINI)
                .build();

        return AiServices.create(Assistant.class, model);
    }

    
}
