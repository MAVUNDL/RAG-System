package com.example.demo.ai.rag;

import dev.langchain4j.store.embedding.chroma.ChromaApiVersion;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VectorDB {

    @Bean
    public ChromaEmbeddingStore chromaDB(){
        return ChromaEmbeddingStore.builder()
                .apiVersion(ChromaApiVersion.V2)
                .baseUrl("http://localhost:8001")
                .tenantName("default")
                .databaseName("default")
                .collectionName("my-rag-collection")
                .build();
    }

}
