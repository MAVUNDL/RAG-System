package com.example.demo.ai.rag.ingestion.automation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.devtools.filewatch.FileSystemWatcher;

import java.io.File;
import java.time.Duration;

@Configuration
public class DirectoryWatcherConfig {

    @Bean
    public FileSystemWatcher fileSystemWatcher() {
        FileSystemWatcher watcher = new FileSystemWatcher(true, Duration.ofMillis(5000), Duration.ofMillis(3000));
        watcher.addSourceDirectory(new File("src/main/resources/static/ECL-Methodology-Disclosures"));
        return watcher;
    }
}