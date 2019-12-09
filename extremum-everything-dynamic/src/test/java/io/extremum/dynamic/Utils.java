package io.extremum.dynamic;

import java.io.InputStream;
import java.util.Scanner;

public class Utils {
    public static String convertInputStreamToString(InputStream inputStream) {
        Scanner scanner = new Scanner(inputStream);
        scanner.useDelimiter("\\A");
        return scanner.next();
    }
}
