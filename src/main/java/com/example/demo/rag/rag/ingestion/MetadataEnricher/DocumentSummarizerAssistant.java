package com.example.demo.rag.rag.ingestion.MetadataEnricher;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import java.util.Map;

public interface DocumentSummarizerAssistant {
    @SystemMessage("""
    You extract effective retrieval metadata from documents.
    You must return structured JSON matching the schema exactly.
    Summaries must be concise and retrieval-optimized in a single sentence.
    """)
    @UserMessage("""
    Extract metadata from the following document:
    {{document}}
    
    Incorporate this existing context where applicable:
    {{basicMetadata}}
    """)
    DocumentMetadata extract(
            @V("document") String document,
            @V("basicMetadata") Map<String, Object> basicMetadata
    );
}
