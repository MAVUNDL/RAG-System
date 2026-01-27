package com.example.demo.rag.configuration;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.router.LanguageModelQueryRouter;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.rag.query.transformer.CompressingQueryTransformer;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;

@Configuration
public class RAGConfiguration {


    // 1. THE SHARED MODEL: Define this once.
    @Bean
    public ChatModel chatModelModel() {
        return OpenAiChatModel.builder()
                .apiKey(System.getenv("OPEN_API_KEY"))
                .modelName(OpenAiChatModelName.GPT_4_O_MINI)
                .timeout(Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    // 2. THE AUGMENTOR: Injects the shared model bean.
    @Bean
    public RetrievalAugmentor retrievalAugmentor(
            ChatModel chatModel, // Injected by Spring
            ChromaEmbeddingStore store,
            EmbeddingModel embeddingModel) {

        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .maxResults(5)
                .build();

        // We provide a description of the data so the LLM knows when to route there
        Map<ContentRetriever, String> retrieverToDescription = Map.of(
                contentRetriever, "Use this for questions about Expected Credit Loss (ECL), IFRS9, risk management, or specific financial disclosure documents."
        );

        QueryRouter queryRouter = new LanguageModelQueryRouter(chatModel, retrieverToDescription);

        return DefaultRetrievalAugmentor.builder()
                .queryTransformer(new CompressingQueryTransformer(chatModel))
                .queryRouter(queryRouter)
                .build();
    }

    // 3. THE ASSISTANT: Injects the shared model AND the augmentor.
    @Bean
    public Assistant assistant(
            ChatModel chatModel, // Injected by Spring
            RetrievalAugmentor augmentor) {

        return AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .retrievalAugmentor(augmentor)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .systemMessageProvider(chatId ->
                        "You are a professional financial assistant for ECL disclosures. " +
                        "Always cite the 'file_name' from the metadata. " +
                        "If the information is not in the context, say you don't know."
                )
                .build();
    }
}
