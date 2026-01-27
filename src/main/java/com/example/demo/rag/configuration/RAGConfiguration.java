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
                .minScore(0.6)
                .build();

        // We provide a description of the data so the LLM knows when to route there
        Map<ContentRetriever, String> retrieverToDescription = Map.of(
                contentRetriever, "ONLY use this for technical questions about Expected Credit Loss (ECL), IFRS 9 formulas, " +
                        "bank risk disclosures, or specific methodology documents. " +
                        "DO NOT use this for greetings, general conversation, or non-financial questions."
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
                        "You are a professional financial assistant specialized in Expected Credit Loss (ECL) disclosures. " +
                                "GUIDELINES: " +
                                "1. If the user asks a technical question about ECL, IFRS 9, or risk management, USE the provided context and cite the 'file_name'. " +
                                "2. If the user query is a general greeting (like 'hello'), or a general question unrelated to the financial documents, " +
                                "DO NOT use the context. Instead, respond as a polite, helpful AI assistant, and do not retrieve any document from the vector store. " +
                                "3. For technical financial questions NOT covered by the context, inform the user you don't have that specific data in your registry."
                )
                .build();
    }
}
