package com.team8.fooddelivery.util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;

/**
 * Копирует папку webapp из classpath во временную директорию,
 * чтобы embedded Tomcat мог обслуживать JSP при запуске JAR.
 */
public final class WebAppExtractor {

    private WebAppExtractor() {
    }

    public static Path extractWebAppToTemp() throws IOException, URISyntaxException {
        URL webAppUrl = Thread.currentThread().getContextClassLoader().getResource("webapp");
        if (webAppUrl == null) {
            throw new IllegalStateException("Не найдена папка webapp в classpath");
        }

        Path tempDir = Files.createTempDirectory("food-delivery-webapp");
        URI uri = webAppUrl.toURI();

        if ("jar".equalsIgnoreCase(uri.getScheme())) {
            String[] parts = uri.toString().split("!");
            URI jarUri = URI.create(parts[0]);
            try (FileSystem fs = FileSystems.newFileSystem(jarUri, Collections.emptyMap())) {
                Path jarWebAppPath = fs.getPath(parts[1]);
                copyDirectory(jarWebAppPath, tempDir);
            }
        } else {
            Path webAppPath = Paths.get(uri);
            copyDirectory(webAppPath, tempDir);
        }

        return tempDir;
    }

    private static void copyDirectory(Path source, Path targetRoot) throws IOException {
        Files.walk(source).forEach(path -> {
            try {
                Path relative = source.relativize(path);
                Path target = targetRoot.resolve(relative.toString());
                if (Files.isDirectory(path)) {
                    Files.createDirectories(target);
                } else {
                    Files.createDirectories(target.getParent());
                    Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new RuntimeException("Ошибка копирования webapp: " + e.getMessage(), e);
            }
        });
    }
}
