package com.rmowlana.aimemorychat.configuration;

import com.rmowlana.aimemorychat.service.ReferenceContentService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;

@Configuration
@Slf4j
public class ReferenceContentConfig {
    private final ReferenceContentService referenceContentService;
    private final ResourceLoader resourceLoader;

    @Value("${reference.content.path}")
    private String referenceContentPath;

    public ReferenceContentConfig(ReferenceContentService referenceContentService, ResourceLoader resourceLoader) {
        this.referenceContentService = referenceContentService;
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void loadReferenceContent() throws IOException {
        Resource resource = resourceLoader.getResource(referenceContentPath);
        referenceContentService.loadReferenceContent(resource.getFile().getAbsolutePath());
        log.info("Reference content loaded from: {} " , referenceContentPath);
    }
}
