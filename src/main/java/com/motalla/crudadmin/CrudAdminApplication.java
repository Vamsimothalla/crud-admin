package com.motalla.crudadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(CrudAdminProperties.class)
public class CrudAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrudAdminApplication.class, args);
    }

}
