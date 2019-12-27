package io.extremum.dynamic;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Validated
@Getter
@Setter
@ConfigurationProperties("github.schema")
public class GithubSchemaProperties {
    @NotNull
    private int webhookListenerPort;
    @NotNull
    private String webhookListenerServerContext;
    @NotNull
    private String owner;
    @NotNull
    private String repo;
    @NotNull
    private String schemaPath;
    private String schemaName;
    @NotNull
    private String ref;
    @NotNull
    private String token;
}
