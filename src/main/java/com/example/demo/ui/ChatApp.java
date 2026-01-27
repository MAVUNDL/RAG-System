package com.example.demo.ui;

import com.example.demo.rag.configuration.Assistant;
import com.example.demo.rag.rag.ingestion.pipeline.DocumentRegistry;
import com.example.demo.rag.rag.ingestion.pipeline.IngestionPipeline;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.service.Result;
import io.javelit.components.layout.ColumnsComponent;
import io.javelit.core.Jt;
import io.javelit.core.JtContainer;
import io.javelit.core.Server;
import org.intellij.lang.annotations.Language;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class ChatApp {
    private final IngestionPipeline pipeline;
    private final DocumentRegistry documentRegistry;
    private final Assistant assistant;

    public ChatApp(IngestionPipeline pipeline, DocumentRegistry documentRegistry, Assistant assistant) {
        this.pipeline = pipeline;
        this.documentRegistry = documentRegistry;
        this.assistant = assistant;
        startServer();
    }

    private void app() {
        // Initialize state to prevent NullPointerExceptions
        Jt.sessionState().putIfAbsent("chat_history", new ArrayList<String>());
        Jt.sessionState().putIfAbsent("is_ingesting", false);
        Jt.sessionState().putIfAbsent("is_thinking", false);

        var currentPage = Jt.navigation(
                // Set "/" to dashboard so it's not blank on load
                Jt.page("/dashboard", this::dashboardPage).title("Dashboard").icon("📊"),
                Jt.page("/chat", this::chatPage).title("AI Assistant").icon("💬")
        ).use();

        // This triggers the rendering of the matched page
        currentPage.run();
    }

    private void dashboardPage() {
        Jt.title("System Dashboard").use();

        Jt.title("RAG Controls").use(Jt.SIDEBAR);
        boolean isProcessing = Jt.sessionState().getBoolean("is_ingesting", false);

        if (Jt.button("🚀 Start Initial Batch").disabled(isProcessing).use(Jt.SIDEBAR)) {
            Jt.sessionState().put("is_ingesting", true);
            pipeline.runInitialLoad();
            Jt.sessionState().put("is_ingesting", false);
        }

        Jt.markdown("---").use(Jt.SIDEBAR);
        Jt.info("Watching static resources...").use(Jt.SIDEBAR);

        List<Document> documents = documentRegistry.getAllDocuments();
        renderMetrics(documents, isProcessing);

        Jt.markdown("---").use();

        var mainTabs = Jt.tabs(List.of("Registry Table", "Detailed View", "System Logs")).use();
        renderTableTab(documents, mainTabs.tab("Registry Table"));
        renderDetailsTab(documents, mainTabs.tab("Detailed View"));
        renderLogsTab(mainTabs.tab("System Logs"));
    }

    private void chatPage() {
        Jt.title("AI Assistant").use();
        Jt.markdown("Query methodology disclosures via Advanced RAG (Query Transformation + Chroma).").use();

        List<String> history = (List<String>) Jt.sessionState().get("chat_history");
        boolean isThinking = Jt.sessionState().getBoolean("is_thinking", false);

        // Chat Container
        var chatContainer = Jt.container().height(450).border(true).use();
        for (@Language("markdown") String msg : history) {
            Jt.markdown(msg).use(chatContainer);
        }

        if (isThinking) {
            Jt.info("⏳ AI is searching documents and formulating a response...").use(chatContainer);
        }

        Jt.markdown("---").use();

        // Input Form
        var form = Jt.form().use();
        String query = Jt.textInput("question")
                .placeholder("Ask about ECL methodology...")
                .disabled(isThinking) // Prevent typing while waiting
                .use(form);

        if (Jt.formSubmitButton("Ask").disabled(isThinking).use(form)) {
            if (query != null && !query.isBlank()) {
                history.add("**User:** " + query);
                Jt.sessionState().put("is_thinking", true);

                try {
                    // 1. Get the Result object instead of just a string
                    Result<String> result = assistant.chat(query);
                    String aiAnswer = result.content();

                    // 2. Extract unique file names from the retrieved segments
                    String sources = result.sources().stream()
                            .map(source -> source.textSegment().metadata().getString("file_name"))
                            .filter(Objects::nonNull)
                            .distinct()
                            .collect(Collectors.joining(", "));

                    // 3. Add to UI with a clear citation line
                    String fullResponse = "**Assistant:**\n" + aiAnswer +
                            "\n\n---\n**Sources used:** " + (sources.isEmpty() ? "Internal Knowledge" : sources);

                    history.add(fullResponse);

                } catch (Exception e) {
                    history.add("**Assistant:** ❌ Error: " + e.getMessage());
                } finally {
                    Jt.sessionState().put("is_thinking", false);
                }

                Jt.sessionState().put("chat_history", history);
            }
        }

        if (Jt.button("🗑️ Clear Conversation").use(Jt.SIDEBAR)) {
            Jt.sessionState().put("chat_history", new ArrayList<String>());
            // If you are using MessageWindowChatMemory, you'd clear it here too
            Jt.success("Memory cleared!").use(Jt.SIDEBAR);
        }
    }

    private void renderMetrics(List<Document> documents, boolean isProcessing) {
        var cols = Jt.columns(3).gap(ColumnsComponent.Gap.SMALL).use();

        var c1 = Jt.container().border(true).key("m1").use(cols.col(0));
        Jt.markdown("#### Total Docs").use(c1);
        Jt.title(String.valueOf(documents.size())).use(c1);

        var c2 = Jt.container().border(true).key("m2").use(cols.col(1));
        Jt.markdown("#### Status").use(c2);
        Jt.text(documentRegistry.getLastActionStatus()).use(c2);

        var c3 = Jt.container().border(true).key("m3").use(cols.col(2));
        Jt.markdown("#### Mode").use(c3);
        Jt.text(isProcessing ? "⏳ Processing" : "🟢 Online").use(c3);
    }

    private void renderTableTab(List<Document> docs, JtContainer parent) {
        if (docs.isEmpty()) { Jt.info("No documents found.").use(parent); return; }
        Jt.table(docs.stream().map(d -> Map.of(
                "File", Objects.toString(d.metadata().getString("file_name"), "Unknown"),
                "ID", Objects.toString(d.metadata().getString("documentId"), "N/A")
        )).toList()).use(parent);
    }

    private void renderDetailsTab(List<Document> docs, JtContainer parent) {
        for (Document doc : docs) {
            String id = doc.metadata().getString("documentId");
            var exp = Jt.expander("📄 " + doc.metadata().getString("file_name")).key("exp_"+id).use(parent);
            Jt.markdown("**AI Summary:**").use(exp);
            Jt.text(Objects.toString(doc.metadata().getString("summary"), "Not available")).use(exp);
        }
    }

    private void renderLogsTab(JtContainer parent) {
        Jt.markdown("### Background Event Stream").use(parent);
        Jt.code(documentRegistry.getLastActionStatus()).use(parent);
    }

    private void startServer() {
        Server.builder(this::app, 8888).build().start();
    }
}