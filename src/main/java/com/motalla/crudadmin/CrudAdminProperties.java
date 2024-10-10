package com.motalla.crudadmin;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("crud.admin")

@Getter
@Setter
public class CrudAdminProperties {

    /**
     * Indicates whether the CRUD administration interface is enabled or disabled.
     */
    private boolean enabled = false;

    /**
     * The base URL of the CRUD administration interface.
     */
    private String baseUrl;

    private String basePackages;
}
