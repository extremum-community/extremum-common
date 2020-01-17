package io.extremum.dynamic.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;

@Slf4j
@RequiredArgsConstructor
public class HttpSchemaServerHandler implements HttpHandler {
    private final ExecutorService executor;
    private final Path schemaDirectory;

    @Override
    public void handle(HttpExchange httpExchange) {
        executor.submit(new HttpSchemaServerExchangeHandler(httpExchange, schemaDirectory));
    }
}
