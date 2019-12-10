package io.extremum.dynamic;

import java.io.InputStream;
import java.util.Scanner;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Utils {
    public static String convertInputStreamToString(InputStream inputStream) {
        Scanner scanner = new Scanner(inputStream, UTF_8.name());
        scanner.useDelimiter("\\A");
        return scanner.next();
    }
}