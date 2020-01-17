package io.extremum.dynamic.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class HttpSchemaServerExchangeHandler implements Runnable {
    private final HttpExchange exchange;
    private final Path basicSchemaDirectory;

    public HttpSchemaServerExchangeHandler(HttpExchange exchange, Path basicSchemaDirectory) {
        this.exchange = exchange;
        this.basicSchemaDirectory = basicSchemaDirectory;
    }

    @Override
    public void run() {
        try {
            String path = exchange.getRequestURI().getPath();
            Path schemaPath = Paths.get(basicSchemaDirectory.toString(), path);

            if (Files.exists(schemaPath)) {
                respondStatus(HttpStatus.OK);

                exchange.getResponseHeaders()
                        .add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

                Files.copy(schemaPath, exchange.getResponseBody());
            } else {
                respondStatus(HttpStatus.NOT_FOUND);
            }

            exchange.getResponseBody().close();
        } catch (Exception e) {
            log.error("Exception occurred {}", e, e);
            respondInternalError();
        }
    }

    private void respondStatus(HttpStatus status) throws IOException {
        exchange.sendResponseHeaders(status.value(), 0);
    }

    private void respondInternalError() {
        try {
            exchange.sendResponseHeaders(HttpStatus.INTERNAL_SERVER_ERROR.value(), 0);
            exchange.getResponseBody().close();
        } catch (IOException e) {
            log.error("Unable to respond", e);
        }
    }
}
