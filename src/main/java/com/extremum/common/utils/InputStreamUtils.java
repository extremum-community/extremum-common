package com.extremum.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

@Slf4j
public class InputStreamUtils {
    private static final int BYTE_BUFFER_SIZE = 1024;

    public static byte[] toByteArray(InputStream is) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        byte[] buffer = new byte[BYTE_BUFFER_SIZE];

        int read;

        try {
            while ((read = is.read(buffer, 0, buffer.length)) != -1) {
                bos.write(buffer, 0, read);
            }
        } catch (IOException ex) {
            log.error("Can't read an InputStream", ex);
            throw new RuntimeException("Can't read an InputStream", ex);
        }

        return bos.toByteArray();
    }

    public static InputStream fromByteArray(byte[] bytes) {
        return new ByteArrayInputStream(bytes);
    }

    public static InputStream fromString(String string) {
        return new ByteArrayInputStream(string.getBytes());
    }

    public static String convertToString(InputStream inputStream) {
        Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }
}
