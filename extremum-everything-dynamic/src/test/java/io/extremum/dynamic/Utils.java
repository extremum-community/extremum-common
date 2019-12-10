package io.extremum.dynamic;

import lombok.SneakyThrows;

import java.io.InputStream;
import java.util.Scanner;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Utils {
    @SneakyThrows
    public static String convertInputStreamToString(InputStream inputStream) {
        Scanner scanner = new Scanner(inputStream, UTF_8.name());
        scanner.useDelimiter("\\A");
        try {
            return scanner.next();
        } finally {
            inputStream.close();
        }
    }
}
