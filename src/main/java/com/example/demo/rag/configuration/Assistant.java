package com.example.demo.rag.configuration;


import dev.langchain4j.service.Result;

public interface Assistant {
    Result<String> chat(String userMessage);
}
