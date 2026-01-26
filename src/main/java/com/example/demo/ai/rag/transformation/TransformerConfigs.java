package com.example.demo.ai.rag.transformation;

import dev.langchain4j.data.document.DocumentTransformer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TransformerConfigs {
    @Bean
    public DocumentTransformer textTransformer(){
        return new TextCleaningTransformer();
    }
}
