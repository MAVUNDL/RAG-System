package com.example.demo.ai.rag.ingestion.pipeline;

import dev.langchain4j.data.document.Document;
import org.springframework.stereotype.Component;


import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class DocumentRegistry {
    // Thread-safe list of all documents (Batch + Real-time)
    private final List<Document> allDocuments = new CopyOnWriteArrayList<>();

    // This variable stores the "Status Message" for the UI
    private String lastActionStatus = "Idle - Monitoring Folder";

    public void addDocument(Document doc) {
        if (doc != null) {
            this.allDocuments.add(doc);

            // Extract the filename from the metadata we created earlier
            String fileName = doc.metadata().getString("file_name");

            // Update the status so the UI knows something happened
            this.lastActionStatus = "✅ Successfully processed: " + fileName;
        }
    }

    public List<Document> getAllDocuments() {
        return allDocuments;
    }

    // THIS IS THE METHOD THE UI CALLS
    public String getLastActionStatus() {
        return lastActionStatus;
    }
}