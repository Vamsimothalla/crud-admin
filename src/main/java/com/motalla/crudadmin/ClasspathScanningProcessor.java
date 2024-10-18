package com.motalla.crudadmin;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.Entity;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The ClasspathScanningProcessor is responsible for scanning specified base packages for classes
 * annotated with {@link Entity} or {@link MappedSuperclass}. Detected entity classes are registered
 * for further processing.
 */
@Log4j2
@Getter
@Component
public class ClasspathScanningProcessor {

    private final CrudAdminProperties properties;
    private final List<String> processedEntities = Collections.synchronizedList(new ArrayList<>());

    public ClasspathScanningProcessor(CrudAdminProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void loader() {
        if (properties.getBasePackages() == null) {
            log.warn("Base packages are not configured.");
            return;
        }

        String[] basePackages = properties.getBasePackages().split(",");

        for (String basePackage : basePackages) {
            scanPackageForEntities(basePackage);
        }
    }

    /**
     * Scans the specified base package for classes annotated with {@link Entity} or {@link MappedSuperclass}.
     * Detected entity classes are logged and registered for further processing.
     *
     * @param basePackage the base package to scan for entity classes
     */
    public void scanPackageForEntities(String basePackage) {
        try {
            ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
            provider.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
            provider.addIncludeFilter(new AnnotationTypeFilter(MappedSuperclass.class));

            for (BeanDefinition definition : provider.findCandidateComponents(basePackage)) {
                log.debug("Scanned entity {}", definition.getBeanClassName());
                if (definition.getBeanClassName() != null) {
                    processedEntities.add(definition.getBeanClassName());
                }
            }
        } catch (Exception e) {
            log.error("Error while scanning for entities in base package: {}", basePackage, e);
        }
    }
}