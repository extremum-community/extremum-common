package io.extremum.dynamic.server.impl;

import io.extremum.dynamic.server.handlers.HttpSchemaServerHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.util.SocketUtils;

import java.io.IOException;
import java.net.Socket;

class HttpSchemaServerTest {
    @Test
    void launch() {
    }

    @Test
    void shutdownWillReleasePort() throws InterruptedException {
        int port = SocketUtils.findAvailableTcpPort();

        HttpSchemaServerHandler handler = Mockito.mock(HttpSchemaServerHandler.class);

        HttpSchemaServer server = new HttpSchemaServer(port, "/", handler);

        Assertions.assertFalse(isPortInUse(port));
        Assertions.assertFalse(server.isRunning());

        server.launch();

        Thread.sleep(2000);

        Assertions.assertTrue(isPortInUse(port));
        Assertions.assertTrue(server.isRunning());

        server.shutdown();

        Thread.sleep(2000);

        Assertions.assertFalse(isPortInUse(port));
        Assertions.assertFalse(server.isRunning());
    }

    private boolean isPortInUse(int port) {
        try {
            new Socket("localhost", port).close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}