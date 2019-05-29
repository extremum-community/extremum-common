package com.extremum.jpa.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties("jpa")
@Validated
public class JpaProperties {
    private String uri;
    private String username;
    private String password;
    private boolean generateDdl;
    private boolean showSql;
    @NotEmpty
    private List<String> entityPackages;
    @NotEmpty
    private List<String> repositoryPackages;

}