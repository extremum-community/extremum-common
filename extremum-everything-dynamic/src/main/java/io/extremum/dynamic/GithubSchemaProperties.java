package io.extremum.dynamic;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ConfigurationProperties("github")
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
    @NotNull
    private String ref;
    @NotNull
    private String token;
}
