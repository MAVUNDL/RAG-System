package com.example.demo.rag.rag.ingestion.pipeline;

import com.example.demo.rag.rag.ingestion.MetadataEnricher.DocumentMetadata;
import com.example.demo.rag.rag.ingestion.MetadataEnricher.DocumentSummarizerAssistant;
import com.example.demo.rag.rag.storage.service.DocumentStorageService;
import com.example.demo.rag.rag.transformation.TextCleaningTransformer;
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
    private final DocumentRegistry documentRegistry;
    private final DocumentStorageService documentStorageService;

    public IngestionPipeline(DocumentSummarizerAssistant documentSummarizerAssistant, TextCleaningTransformer textCleaningTransformer, DocumentRegistry documentRegistry, DocumentStorageService documentStorageService) {
        this.documentSummarizerAssistant = documentSummarizerAssistant;
        this.textCleaningTransformer = textCleaningTransformer;
        this.documentRegistry = documentRegistry;
        this.documentStorageService = documentStorageService;
        this.documentParser = new ApacheTikaDocumentParser();
        String folderPath = System.getenv().getOrDefault(
                "WATCHER_PATH",
                "src/main/resources/static/ECL-Methodology-Disclosures"
        );
        this.directoryPath = Paths.get(folderPath);
    }

    public Document processDocument(File file) {
        if (!file.isFile()) return null;
        // load file to document object
        Document document = FileSystemDocumentLoader.loadDocument(file.toPath(),  documentParser);

        // Transform document
        document = textCleaningTransformer.transform(document);

        // Enrich Document
        String documentId = UUID.randomUUID().toString();
        Map<String, Object> basicMetadata = new HashMap<>();
        //              Basic Metadata
        basicMetadata.put("documentId", documentId);
        basicMetadata.put("source_type", "filesystem");
        basicMetadata.put("path", file.getAbsolutePath());

        //              Send Doc to Model for Enrichment
        DocumentMetadata enriched = documentSummarizerAssistant.extract(document.text(), basicMetadata);

        //              (mapping enriched to finalMetadataMap)
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
        return document;
    }

    public void processAndRegister(File file) {
        Document document = processDocument(file);
        if (document != null) {
            documentStorageService.ingestDocument(document); // persist
            this.documentRegistry.addDocument(document); // for UI registry
        }
    }

    public void runInitialLoad(){
        File[] files = directoryPath.toFile().listFiles();
        if (files != null && files.length > 0) {
            Arrays.stream(files)
                    .filter(File::isFile)
                    .forEach(this::processAndRegister);
        }
    }
}
