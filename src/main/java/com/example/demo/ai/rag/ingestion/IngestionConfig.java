package com.example.demo.ai.rag.ingestion;

import com.example.demo.ai.rag.transformation.TextCleaningTransformer;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static dev.langchain4j.model.chat.Capability.RESPONSE_FORMAT_JSON_SCHEMA;

@Configuration
public class IngestionConfig {

    @Bean
    public DocumentSummarizerAssistant DocumentSummarizerAssistant() {
        ChatModel model = OpenAiChatModel.builder()
                .apiKey(System.getenv("OPEN_API_KEY"))
                .modelName(OpenAiChatModelName.GPT_4_O_MINI)
                .supportedCapabilities(RESPONSE_FORMAT_JSON_SCHEMA)
                .strictJsonSchema(true)
                .build();
        return AiServices.create(DocumentSummarizerAssistant.class, model);
    }

    @Bean
    public TextCleaningTransformer textCleaningTransformer() {
        return new TextCleaningTransformer();
    }

    @Bean
    public IngestionPipeline IngestionPipeline( DocumentSummarizerAssistant documentSummarizerAssistant, TextCleaningTransformer textCleaningTransformer) {
        return new IngestionPipeline(documentSummarizerAssistant, textCleaningTransformer);
    }

}
