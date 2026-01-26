package com.example.demo.ai.rag.ingestion;

import com.example.demo.ai.rag.transformation.TextCleaningTransformer;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;


import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class IngestionPipeline {
    private final DocumentParser documentParser;
    private final Path directoryPath;
    private final DocumentSummarizerAssistant documentSummarizerAssistant;
    private final TextCleaningTransformer textCleaningTransformer;

    public IngestionPipeline(DocumentSummarizerAssistant documentSummarizerAssistant, TextCleaningTransformer textCleaningTransformer) {
        this.documentSummarizerAssistant = documentSummarizerAssistant;
        this.textCleaningTransformer = textCleaningTransformer;
        this.documentParser = new ApacheTikaDocumentParser();
        this.directoryPath = Paths.get("src/main/resources/static/ECL-Methodology-Disclosures");
    }

    public List<Document> ETLPipeline() {
        // create instances
        List<Document> documents = new ArrayList<>();
        File directory = directoryPath.toFile();
        File[] files = directory.listFiles();

        if (files == null) {
            throw new IllegalStateException("Directory not found or empty: " + directoryPath);
        }

        for (File file : files) {
            if (!file.isFile()) continue;

             /*
                ______________________________________________________________________________-
                                    Load Document
                ___________________________________________________________________________
             */

            Document document = FileSystemDocumentLoader.loadDocument(file.getPath(), documentParser);

             /*
                ______________________________________________________________________________-
                                    Document Transformation
                ___________________________________________________________________________
             */

            document = textCleaningTransformer.transform(document);


            /*
                ______________________________________________________________________________-
                                    Retrieval Enrichment
                ___________________________________________________________________________
             */

            String documentId = UUID.randomUUID().toString();
            Map<String, Object> basicMetadata = new HashMap<>();
            basicMetadata.put("documentId", documentId);
            basicMetadata.put("source_type", "filesystem");
            basicMetadata.put("path", file.getAbsolutePath());


            //  Get the LLM enrichment result
            DocumentMetadata enriched = documentSummarizerAssistant.extract(document.text(), basicMetadata);

            Map<String, Object> finalMetadataMap = new HashMap<>();

            finalMetadataMap.put("file_name", file.getName());
            finalMetadataMap.put("absolute_directory_path", directoryPath.toAbsolutePath().toString());

            finalMetadataMap.put("title", Objects.toString(enriched.getTitle(), "No Title"));
            finalMetadataMap.put("summary", Objects.toString(enriched.getSummary(), "No Summary"));
            finalMetadataMap.put("language", Objects.toString(enriched.getLanguage(), "en"));

            if (enriched.getTopics() != null) {
                finalMetadataMap.put("topics",  enriched.getTopics().toString());
            }

            finalMetadataMap.put("documentId", documentId);
            finalMetadataMap.put("source_type", "filesystem");
            finalMetadataMap.put("path", file.getAbsolutePath());

            document = Document.from(document.text(), Metadata.from(finalMetadataMap));

            documents.add(document);
        }

        return documents;
    }
}
