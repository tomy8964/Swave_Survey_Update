package com.example.surveyanalyze;

import com.example.surveyanalyze.survey.exception.InvalidPythonException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ResourceLocator {

    private final ResourceLoader resourceLoader;

    public String getResourceFolderLocation() {
        Resource resource = resourceLoader.getResource("classpath:");
        try {
            return resource.getURI().getPath();
        } catch (IOException e) {
            throw new InvalidPythonException(e);
        }
    }
}