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
import java.util.List;

@Log4j2
@Getter
@Component
public class ClasspathScanningProcessor {
    

    private final CrudAdminProperties properties;

    private final List<String> processedEntities = new ArrayList<>();

    /**
     * Scans specified base packages for classes annotated with {@link Entity} or {@link MappedSuperclass}.
     *
     * @param properties The CRUD admin properties containing the base packages to scan.
     */
    public ClasspathScanningProcessor(CrudAdminProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void loader(){
        String[] basePackages = properties.getBasePackages().split(",");
        for (String basePackage : basePackages){
            scanAndRegisterEntities(basePackage);
        }
    }


    public void scanAndRegisterEntities(String basePackage) {


        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);

        provider.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
        provider.addIncludeFilter(new AnnotationTypeFilter(MappedSuperclass.class));

        for (BeanDefinition definition : provider.findCandidateComponents(basePackage)) {

            log.debug(String.format("Scanned entity %s", definition.getBeanClassName()));

            if (definition.getBeanClassName() != null) {
                processedEntities.add(definition.getBeanClassName());
            }
        }

    }
}
