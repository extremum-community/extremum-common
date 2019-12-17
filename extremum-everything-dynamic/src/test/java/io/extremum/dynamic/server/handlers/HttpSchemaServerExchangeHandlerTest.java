package io.extremum.dynamic.server.handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import io.extremum.dynamic.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.testcontainers.shaded.com.google.common.net.HttpHeaders;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.String.format;
import static org.mockito.Mockito.*;

class HttpSchemaServerExchangeHandlerTest {
    @Test
    void respondWithFileContent() throws IOException {
        HttpExchange exchange = mock(HttpExchange.class);

        ByteArrayOutputStream responseBody = spy(ByteArrayOutputStream.class);

        String fileName = "simple.schema.json";
        URI requestUri = URI.create(format("http://localhost:8080/%s", fileName));

        Headers responseHeaders = new Headers();

        doNothing().when(responseBody).close();

        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getRequestURI()).thenReturn(requestUri);
        when(exchange.getResponseHeaders()).thenReturn(responseHeaders);

        ArgumentCaptor<Integer> codeCaptor = ArgumentCaptor.forClass(Integer.class);

        Path basePath = Paths.get(this.getClass().getClassLoader().getResource("schemas").getPath());

        HttpSchemaServerExchangeHandler handler = new HttpSchemaServerExchangeHandler(exchange, basePath);

        handler.run();

        // response output stream is closed
        verify(responseBody).close();

        // headers with status code was sent
        verify(exchange).sendResponseHeaders(codeCaptor.capture(), anyLong());

        InputStream contentIs = TestUtils.loadResourceAsInputStream(this.getClass().getClassLoader(), format("schemas/%s", fileName));

        // check response content
        String expectedContent = TestUtils.convertInputStreamToString(contentIs);
        String actualContent = new String(responseBody.toByteArray());
        Assertions.assertEquals(expectedContent, actualContent);

        // check content-type
        Assertions.assertEquals(MediaType.APPLICATION_JSON_VALUE, responseHeaders.get(HttpHeaders.CONTENT_TYPE).get(0));

        // check response code
        Assertions.assertEquals(HttpStatus.OK.value(), (int) codeCaptor.getValue());
    }

    @Test
    void respond_404_ifRequestedResourceNotFound() throws IOException {
        HttpExchange exchange = mock(HttpExchange.class);

        Path baseCatalog = Paths.get("unknown_catalog");

        OutputStream responseOutputStream = mock(OutputStream.class);

        doNothing().when(responseOutputStream).close();

        when(exchange.getRequestURI()).thenReturn(URI.create("http://localhost"));
        when(exchange.getResponseHeaders()).thenReturn(mock(Headers.class));
        when(exchange.getResponseBody()).thenReturn(responseOutputStream);

        HttpSchemaServerExchangeHandler handler = new HttpSchemaServerExchangeHandler(exchange, baseCatalog);
        handler.run();

        ArgumentCaptor<Integer> captorCode = ArgumentCaptor.forClass(Integer.class);
        verify(exchange).sendResponseHeaders(captorCode.capture(), anyLong());

        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), (int) captorCode.getValue());
        verify(responseOutputStream).close();
    }
}