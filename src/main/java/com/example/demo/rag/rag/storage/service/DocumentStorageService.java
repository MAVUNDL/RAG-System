package com.example.demo.rag.rag.storage.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class DocumentStorageService {

    private final EmbeddingStoreIngestor ingestor;

    public DocumentStorageService(ChromaEmbeddingStore chromaDB, EmbeddingModel embeddingModel) {
        chromaDB.removeAll(); // clear storage first

        // Build the ingestor once during startup
        this.ingestor = EmbeddingStoreIngestor.builder()
                .embeddingStore(chromaDB)
                .embeddingModel(embeddingModel)
                .documentSplitter(DocumentSplitters.recursive(500, 50))
                .build();
    }

    public void ingestDocument(Document document) {
        ingestor.ingest(document);
        String title = document.metadata().getString("title");
        Logger.getAnonymousLogger().info("✅ Ingested and Vectorized: " + (title != null ? title : "Unknown"));
    }
}