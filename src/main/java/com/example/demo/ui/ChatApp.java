package com.example.demo.ui;

import com.example.demo.ai.model.Assistant;
import com.example.demo.ai.rag.ingestion.IngestionPipeline;
import dev.langchain4j.data.document.Document;
import io.javelit.core.Jt;
import io.javelit.core.Server;
import io.javelit.core.JtComponent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChatApp {

    private final IngestionPipeline pipeline;

    public ChatApp(IngestionPipeline pipeline) {
        this.pipeline = pipeline;
        startServer(); // automatically start the Javelit server on Spring startup
    }

    private void startServer() {
        Server.builder(this::app, 8888)
                .build()
                .start();
    }

    private void app() {
        Jt.title("AI Chat App").use();
        Jt.markdown("**About:** " + "Testing ingestion pipeline").use();
        if(Jt.button("Start pipeline").use()){
            List<Document> documents = pipeline.ETLPipeline();
            Jt.markdown("**Documents:** " + documents.size()).use();
            Jt.markdown("**Document Metadata:** " + documents.getFirst().metadata()).use();
        }


    }
}
