package com.example.demo.ui;

import com.example.demo.ai.rag.ingestion.pipeline.DocumentRegistry;
import com.example.demo.ai.rag.ingestion.pipeline.IngestionPipeline;
import dev.langchain4j.data.document.Document;
import io.javelit.components.layout.ColumnsComponent;
import io.javelit.core.Jt;
import io.javelit.core.JtContainer;
import io.javelit.core.Server;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class ChatApp {
    private final IngestionPipeline pipeline;
    private final DocumentRegistry documentRegistry;

    public ChatApp(IngestionPipeline pipeline, DocumentRegistry documentRegistry) {
        this.pipeline = pipeline;
        this.documentRegistry = documentRegistry;
        startServer();
    }

    private void app() {
        // --- Sidebar: Main Controls ---
        Jt.title("RAG Pipeline Control").use(Jt.SIDEBAR);

        boolean isProcessing = Jt.sessionState().getBoolean("is_ingesting", false);

        if (Jt.button("🚀 Start Initial Batch").disabled(isProcessing).use(Jt.SIDEBAR)) {
            Jt.sessionState().put("is_ingesting", true);
            pipeline.runInitialLoad();
            Jt.sessionState().put("is_ingesting", false);
        }

        Jt.markdown("---").use(Jt.SIDEBAR);
        Jt.markdown("**Folder Monitor:**").use(Jt.SIDEBAR);
        Jt.code("static/ECL-Methodology-Disclosures").use(Jt.SIDEBAR);

        // --- Main Dashboard Layout ---
        List<Document> documents = documentRegistry.getAllDocuments();

        var metricsCols = Jt.columns(3).gap(ColumnsComponent.Gap.SMALL).use();

        // FIX: Added .key() to metrics containers to prevent ID clashes
        var container1 = Jt.container().border(true).key("metric_total").use(metricsCols.col(0));
        Jt.markdown("#### Total Documents").use(container1);
        Jt.title(String.valueOf(documents.size())).use(container1);

        var container2 = Jt.container().border(true).key("metric_status").use(metricsCols.col(1));
        Jt.markdown("#### System Status").use(container2);
        Jt.text(documentRegistry.getLastActionStatus()).use(container2);

        var container3 = Jt.container().border(true).key("metric_mode").use(metricsCols.col(2));
        Jt.markdown("#### Mode").use(container3);
        if (isProcessing) {
            Jt.text("⏳ AI Extracting...").use(container3);
        } else {
            Jt.text("🟢 Real-time Active").use(container3);
        }

        Jt.markdown("---").use();

        var mainTabs = Jt.tabs(List.of("Registry Table", "Detailed View", "System Logs")).use();

        renderTableTab(documents, mainTabs.tab("Registry Table"));
        renderDetailsTab(documents, mainTabs.tab("Detailed View"));
        renderLogsTab(mainTabs.tab("System Logs"));
    }

    private void renderTableTab(List<Document> documents, JtContainer parent) {
        if (documents.isEmpty()) {
            Jt.info("No documents found. Start the pipeline or add a file to the folder.").use(parent);
            return;
        }

        Jt.table(documents.stream().map(d -> Map.of(
                "File", Objects.toString(d.metadata().getString("file_name"), "Unknown"),
                "Language", Objects.toString(d.metadata().getString("language"), "en"),
                "ID", Objects.toString(d.metadata().getString("documentId"), "N/A")
        )).toList()).use(parent);
    }

    private void renderDetailsTab(List<Document> documents, JtContainer parent) {
        if (documents.isEmpty()) {
            Jt.text("Nothing to display yet.").use(parent);
            return;
        }

        for (Document doc : documents) {
            String fileName = Objects.toString(doc.metadata().getString("file_name"), "Unknown File");
            String docId = Objects.toString(doc.metadata().getString("documentId"), "no-id");

            // FIX: Added .key() using docId to ensure unique expanders in the loop
            var expander = Jt.expander("Details for: " + fileName)
                    .key("expander_" + docId)
                    .expanded(false)
                    .use(parent);

            Jt.markdown("**AI-Generated Summary:**").use(expander);
            Jt.text(Objects.toString(doc.metadata().getString("summary"), "No summary available")).use(expander);

            Jt.markdown("**Topics Identified:**").use(expander);
            Jt.code(Objects.toString(doc.metadata().getString("topics"), "[]")).use(expander);

            Jt.markdown("**Raw Metadata Path:**").use(expander);
            Jt.text(Objects.toString(doc.metadata().getString("path"), "N/A")).use(expander);
        }
    }

    private void renderLogsTab(JtContainer parent) {
        Jt.markdown("### Background Event Stream").use(parent);
        Jt.code(documentRegistry.getLastActionStatus()).use(parent);
        Jt.text("The FileSystemWatcher is currently polling every 5 seconds.").use(parent);
    }

    private void startServer() {
        Server.builder(this::app, 8888).build().start();
    }
}