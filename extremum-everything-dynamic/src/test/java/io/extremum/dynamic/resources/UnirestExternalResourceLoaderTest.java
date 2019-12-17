package io.extremum.dynamic.resources;

import io.extremum.dynamic.resources.exceptions.AccessForbiddenResourceLoadingException;
import io.extremum.dynamic.resources.exceptions.ResourceLoadingException;
import io.extremum.dynamic.resources.exceptions.ResourceLoadingTimeoutException;
import io.extremum.dynamic.resources.exceptions.ResourceNotFoundException;
import kong.unirest.Config;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import static io.extremum.dynamic.TestUtils.convertInputStreamToString;
import static io.extremum.dynamic.TestUtils.loadResourceAsInputStream;
import static java.lang.String.format;

@Testcontainers
class UnirestExternalResourceLoaderTest {
    @Container
    MockServerContainer mockServer = new MockServerContainer();

    MockServerClient msClient;

    @BeforeEach
    void beforeAll() {
        msClient = new MockServerClient(
                mockServer.getContainerIpAddress(),
                mockServer.getServerPort()
        );
    }

    @Test
    void loadResource() throws ResourceLoadingException {
        String path = "/path/to/resource";
        InputStream is = loadResourceAsInputStream(this.getClass().getClassLoader(), "test.file.txt");
        String resource = convertInputStreamToString(is);

        msClient.when(
                HttpRequest.request()
                        .withMethod(HttpMethod.GET.name())
                        .withPath(path)
        ).respond(
                HttpResponse.response()
                        .withBody(resource)
        );

        ResourceLoader loader = new UnirestExternalResourceLoader();
        String url = createUriToMockServer(path);

        InputStream responseIS = loader.loadAsInputStream(URI.create(url));

        String response = convertInputStreamToString(responseIS);

        Assertions.assertEquals(resource, response);
    }

    @Test
    void tryingToLoadUnknownResource_throwResourceNotFoundException() {
        String path = "/path/to/resource";

        msClient.when(
                HttpRequest.request()
                        .withMethod(HttpMethod.GET.name())
                        .withPath(path)
        ).respond(
                HttpResponse.response()
                        .withStatusCode(HttpStatus.NOT_FOUND.value())
        );

        String uriToMockServer = createUriToMockServer(path);
        ResourceLoader loader = new UnirestExternalResourceLoader();
        Assertions.assertThrows(ResourceNotFoundException.class, () -> loader.loadAsInputStream(URI.create(uriToMockServer)));
    }

    @Test
    void serverReceiveBasicAuth() {
        String path = "/path/to/resource";

        msClient.when(
                HttpRequest.request()
                        .withMethod(HttpMethod.GET.name())
                        .withPath(path)
                        .withHeader(HttpHeaders.AUTHORIZATION, "Basic YWFhOmJiYg==")
        ).respond(
                HttpResponse.response()
                        .withStatusCode(HttpStatus.OK.value())
        );

        String uriToMockServer = createUriToMockServer(path);

        Config config = new Config();
        config.setDefaultBasicAuth("aaa", "bbb");
        ResourceLoader loader = new UnirestExternalResourceLoader(config);
        Assertions.assertDoesNotThrow(() -> loader.loadAsInputStream(URI.create(uriToMockServer)));
    }

    @Test
    void serverReceiveBadBasicAuth_thrown_ForbiddenAccessResourceLoadingException() {
        String path = "/path/to/resource";

        msClient.when(
                HttpRequest.request()
                        .withMethod(HttpMethod.GET.name())
                        .withPath(path)
                        .withHeader(HttpHeaders.AUTHORIZATION)
        ).respond(
                HttpResponse.response()
                        .withStatusCode(HttpStatus.UNAUTHORIZED.value())
        );

        String uriToMockServer = createUriToMockServer(path);

        Config config = new Config();
        config.setDefaultBasicAuth("aaa", "bbb");
        ResourceLoader loader = new UnirestExternalResourceLoader(config);
        Assertions.assertThrows(AccessForbiddenResourceLoadingException.class,
                () -> loader.loadAsInputStream(URI.create(uriToMockServer)));
    }

    @Test
    void resourceLoadException_afterEndpointTimeout() {
        String path = "/path/to/resource";

        int socketTimeoutOverheadValue = 70;

        msClient
                .when(
                        HttpRequest.request()
                                .withPath(path)
                ).respond(
                HttpResponse.response()
                        .withStatusCode(HttpStatus.OK.value())
                        .withDelay(TimeUnit.SECONDS, socketTimeoutOverheadValue)
        );

        ResourceLoader loader = new UnirestExternalResourceLoader();

        URI uri = URI.create(format("http://%s:%d",
                mockServer.getContainerIpAddress(),
                mockServer.getServerPort()
        )).resolve(path);

        Assertions.assertThrows(ResourceLoadingTimeoutException.class, () -> loader.loadAsInputStream(uri),
                () -> format("Here is a logs of a MockServer %s", mockServer.getLogs()));
    }

    private String createUriToMockServer(String path) {
        return format("http://%s:%d/%s",
                mockServer.getContainerIpAddress(), mockServer.getServerPort(), path);
    }
}
