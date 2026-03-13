package de.marcelsauer.profiler.config;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author msauer
 */
class FileUtils {

    static String getLocalResource(String file) {
        InputStream input = getLocalResourceAsStream(file);
        if (input == null) {
            throw new RuntimeException("Can not load " + file);
        }

        String resource = getResource(input);

        // convert EOLs
        String[] lines = resource.split("\\r?\\n");
        StringBuilder normalized = new StringBuilder(resource.length());
        for (String line : lines) {
            normalized.append(line).append('\n');
        }
        return normalized.toString();
    }

    private static InputStream getLocalResourceAsStream(String file) {
        String normalizedFile = file.startsWith("/") ? file : "/" + file;

        InputStream input = FileUtils.class.getResourceAsStream(normalizedFile);
        if (input != null) {
            return input;
        }

        ClassLoader classLoader = FileUtils.class.getClassLoader();
        if (classLoader != null) {
            input = classLoader.getResourceAsStream(file);
            if (input != null) {
                return input;
            }
        }

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null) {
            return contextClassLoader.getResourceAsStream(file);
        }

        return null;
    }

    private static String getResource(InputStream input) {
        String resource;
        try (InputStream bufferedInput = new BufferedInputStream(input);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = bufferedInput.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            resource = output.toString(StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return resource;
    }

}
