package com.example.demo.ai.rag.ingestion.automation;

import com.example.demo.ai.rag.ingestion.pipeline.IngestionPipeline;
import org.springframework.boot.devtools.filewatch.ChangedFile;
import org.springframework.boot.devtools.filewatch.ChangedFiles;
import org.springframework.boot.devtools.filewatch.FileChangeListener;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class FileEventTrigger implements FileChangeListener {
    private final IngestionPipeline pipeline;

    public FileEventTrigger(IngestionPipeline pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public void onChange(Set<ChangedFiles> changeSet) {
        for(ChangedFiles changes: changeSet){
            for(ChangedFile file: changes.getFiles()) {
                if(file.getType() == ChangedFile.Type.ADD){
                    pipeline.processAndRegister(file.getFile());
                }
            }
        }

    }
}
