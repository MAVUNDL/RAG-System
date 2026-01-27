package com.example.demo.rag.rag.storage.configuration;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15.BgeSmallEnV15EmbeddingModel;
import dev.langchain4j.store.embedding.chroma.ChromaApiVersion;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Bean
    public EmbeddingModel embeddingModel() {
        return new BgeSmallEnV15EmbeddingModel();
    }

    @Bean
    public ChromaEmbeddingStore chromaDB(){
        String chromaUrl = System.getenv().getOrDefault("CHROMA_URL", "http://localhost:8001");
        return ChromaEmbeddingStore.builder()
                .apiVersion(ChromaApiVersion.V2)
                .baseUrl(chromaUrl)
                .tenantName("default")
                .databaseName("default")
                .collectionName("risk_disclosures")
                .build();
    }

}
