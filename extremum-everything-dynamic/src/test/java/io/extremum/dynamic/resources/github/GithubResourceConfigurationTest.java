package io.extremum.dynamic.resources.github;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GithubResourceConfigurationTest {
    @Test
    void getPropertiesTest() {
        String owner = "owner";
        String repo = "repo";
        String path = "/path/to/schema/file.schema.json";
        String ref = "master";

        GithubResourceConfiguration opts = new GithubResourceConfiguration(
                owner,
                repo,
                path,
                ref
        );

        Assertions.assertEquals(owner, opts.getOwner());
        Assertions.assertEquals(repo, opts.getRepo());
        Assertions.assertEquals(path, opts.getSchemaPath());
        Assertions.assertEquals(ref, opts.getRef());
    }
}