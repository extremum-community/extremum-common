package io.extremum.dynamic.schema.provider.networknt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
@RequiredArgsConstructor
public class GithubConfiguration {
    @Getter
    private final String owner;
    @Getter
    private final String repo;
    @Getter
    private final String schemaBasePath;
    private String authToken;

    public Optional<String> getAuthToken() {
        return Optional.ofNullable(authToken);
    }
}
