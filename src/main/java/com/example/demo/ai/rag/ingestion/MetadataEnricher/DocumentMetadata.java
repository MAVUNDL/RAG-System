package com.example.demo.ai.rag.ingestion.MetadataEnricher;

import dev.langchain4j.model.output.structured.Description;
import java.util.List;

public class DocumentMetadata {

    @Description("Stable unique identifier for the document")
    private String documentId; // Changed from UUID to String

    @Description("Concise human-readable title of the document")
    private String title;

    @Description("2–3 sentence summary optimized for retrieval")
    private String summary;

    @Description("Key topics or domains covered by the document")
    private List<String> topics;

    @Description("Origin of the document, e.g. filesystem, web, database")
    private String sourceType;

    @Description("Original path or URL of the document")
    private String path;

    @Description("ISO 639-1 language code, e.g. en")
    private String language;

    // IMPORTANT: No-args constructor for Jackson deserialization
    public DocumentMetadata() {
    }

    // Getters with specific types instead of Object
    public String getDocumentId() {
        return documentId;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public List<String> getTopics() {
        return topics;
    }

    public String getSourceType() {
        return sourceType;
    }

    public String getPath() {
        return path;
    }

    public String getLanguage() {
        return language;
    }

    // Setters  recommended for Jackson
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public void setTitle(String title) { this.title = title; }
    public void setSummary(String summary) { this.summary = summary; }
    public void setTopics(List<String> topics) { this.topics = topics; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public void setPath(String path) { this.path = path; }
    public void setLanguage(String language) { this.language = language; }
}