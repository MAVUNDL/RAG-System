package com.example.demo.rag.rag.transformation;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentTransformer;


public class TextCleaningTransformer implements DocumentTransformer {
    @Override
    public Document transform(Document document) {
        String text = document.text();
        if (text == null) return document;

        text = text.replaceAll("\\r\\n|\\r", "\n");
        text = text.replaceAll("\\n{3,}", "\n\n");
        text = text.replaceAll("\\f", "");
        text = text.replaceAll("(?<!\\.)\\s*\\n\\s*", " ");
        text = text.replaceAll("\\s+", " ").trim();

        return Document.from(text, document.metadata());
    }
}
