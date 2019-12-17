package io.extremum.dynamic.resources.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.dynamic.resources.ExternalResourceLoader;
import io.extremum.dynamic.resources.UnirestExternalResourceLoader;
import io.extremum.dynamic.resources.exceptions.ResourceLoadingException;
import kong.unirest.Config;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Slf4j
public class GithubResourceLoader implements ExternalResourceLoader {
    private final URI baseUri = URI.create("https://api.github.com/");
    private final ObjectMapper mapper = new ObjectMapper();

    private final ExternalResourceLoader loader;

    public GithubResourceLoader(GithubAccessOptions config) {
        Config unirestConfig = new Config();
        unirestConfig.addDefaultHeader(HttpHeaders.AUTHORIZATION, "token " + config.getAuthToken());
        loader = new UnirestExternalResourceLoader(unirestConfig);
    }

    public String loadContent(String resourceName, GithubResourceConfiguration options) throws ResourceLoadingException {
        URI uri = makeUri(resourceName, options);

        InputStream is = loadAsInputStream(uri);

        String content = extractContent(is, uri);

        byte[] decodedContent = decodeContent(content);

        return new String(decodedContent);
    }

    private String extractContent(InputStream response, URI uri) throws ResourceLoadingException {
        try {
            JsonNode node = mapper.readValue(response, JsonNode.class);
            JsonNode contentNode = node.get("content");

            return contentNode.textValue().replaceAll("\n", "");
        } catch (IOException e) {
            String msg = String.format("Unable to extract content from response %s", response);

            log.error(msg, e);

            throw new ResourceLoadingException(msg, uri, e);
        }
    }

    @NotNull
    protected URI makeUri(String resourceName, GithubResourceConfiguration opts) {
        Path path = Paths.get("repos",
                opts.getOwner(),
                opts.getRepo()
        );

        if (opts.getSchemaPath() != null) {
            path = Paths.get(path.toString(), "contents", opts.getSchemaPath());
        }

        path = Paths.get(path.toString(), resourceName);

        if (opts.getRef() != null) {
            return baseUri.resolve(path.toString() + "?ref=" + opts.getRef());
        } else {
            return baseUri.resolve(path.toString());
        }
    }

    @Override
    public InputStream loadAsInputStream(URI uri) throws ResourceLoadingException {
        InputStream is = loader.loadAsInputStream(uri);

        String content = extractContent(is, uri);

        return new ByteArrayInputStream(decodeContent(content));
    }

    private byte[] decodeContent(String encoded) {
        return Base64.getDecoder().decode(encoded);
    }
}
