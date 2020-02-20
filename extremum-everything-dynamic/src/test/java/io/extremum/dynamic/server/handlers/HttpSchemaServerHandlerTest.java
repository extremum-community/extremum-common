package io.extremum.dynamic.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import io.extremum.dynamic.server.supports.FilesSupportsService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.mockito.Mockito.*;

class HttpSchemaServerHandlerTest {
    @Test
    void handlingPlacedHttpExchangeInPassedExecutor() {
        ExecutorService executur = mock(ExecutorService.class);

        HttpSchemaServerHandler handler = new HttpSchemaServerHandler(executur, mock(Path.class), mock(FilesSupportsService.class));

        HttpExchange exchange = mock(HttpExchange.class);
        when(executur.submit(any(Runnable.class))).thenReturn(mock(Future.class));

        handler.handle(exchange);

        Mockito.verify(executur).submit(Mockito.any(Runnable.class));
    }
}