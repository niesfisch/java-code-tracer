package de.marcelsauer.profiler.config;

import java.io.*;

/**
 * @author msauer
 */
public class FileUtils {

    public static InputStream getConfigFromHomeDirAsStream(String configFileName) {
        try {
            File homeDir = new File(System.getProperty("user.home"));
            File configFile = new File(homeDir + "/.code-tracer/", configFileName);
            return new FileInputStream(configFile);
        } catch (IOException e) {
            throw new RuntimeException("could not load " + configFileName);
        }
    }

    public static String getLocalResource(String theName) {
        try {
            InputStream input = FileUtils.class.getClassLoader().getResourceAsStream(theName);
            if (input == null) {
                throw new RuntimeException("Can not load " + theName);
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
