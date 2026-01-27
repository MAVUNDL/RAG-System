package com.example.demo.ai.rag.ingestion.automation;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.boot.devtools.filewatch.FileSystemWatcher;

@Component
public class WatcherInitializer implements ApplicationRunner {
    private final FileSystemWatcher watcher;
    private final FileEventTrigger trigger;

    public WatcherInitializer(FileSystemWatcher watcher, FileEventTrigger trigger) {
        this.watcher = watcher;
        this.trigger = trigger;
    }

    @Override
    public void run(@NotNull ApplicationArguments args) {
        watcher.addListener(trigger);
        watcher.start();
        System.out.println("Watcher started automatically on Spring Boot startup.");
    }
}