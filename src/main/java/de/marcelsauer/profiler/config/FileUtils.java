package de.marcelsauer.profiler.config;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author msauer
 */
class FileUtils {

    static String getLocalResource(String file) {
        try {
            InputStream input = FileUtils.class.getClassLoader().getResourceAsStream(file);
            if (input == null) {
                throw new RuntimeException("Can not load " + file);
            }
            BufferedInputStream is = new BufferedInputStream(input);
            StringBuilder buf = new StringBuilder(3000);
            int i;
            try {
                while ((i = is.read()) != -1) {
                    buf.append((char) i);
                }
            } finally {
                is.close();
            }
            String resource = buf.toString();
            // convert EOLs
            String[] lines = resource.split("\\r?\\n");
            StringBuilder buffer = new StringBuilder();
            for (String line : lines) {
                buffer.append(line);
                buffer.append("\n");
            }
            return buffer.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
