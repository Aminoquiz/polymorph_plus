package com.shiver.polymorphlegacy.priority;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class PriorityLoader {
    private static final Logger LOG = LogManager.getLogger("Polymorph+");
    private static final String[] JAR_PATHS = {"data/polymorph_plus/priority.json",
            "data/polymorph/priority.json"};

    private PriorityLoader() {
    }

    public static File configFile(File configDir) {
        return new File(new File(configDir, "polymorph"), "priority.json");
    }

    /** The config file sets the order, then mod jars append the namespaces it did not name. */
    public static List<String> load(File configDir) {
        List<List<String>> sources = new ArrayList<>();
        File config = configFile(configDir);
        if (config.isFile()) {
            try {
                sources.add(RecipePriority.parse(new String(Files.readAllBytes(config.toPath()),
                        StandardCharsets.UTF_8)));
            } catch (Exception e) {
                LOG.error("Failed to read {}", config, e);
            }
        }
        ClassLoader loader = PriorityLoader.class.getClassLoader();
        for (String path : JAR_PATHS) {
            try {
                Enumeration<URL> urls = loader.getResources(path);
                for (URL url : Collections.list(urls)) {
                    try (InputStream in = url.openStream()) {
                        sources.add(RecipePriority.parse(IOUtils.toString(in, StandardCharsets.UTF_8)));
                    } catch (Exception e) {
                        LOG.error("Failed to read {}", url, e);
                    }
                }
            } catch (Exception e) {
                LOG.error("Failed to list {}", path, e);
            }
        }
        List<String> merged = RecipePriority.merge(sources);
        RecipePriority.setPackNamespaces(merged);
        if (!merged.isEmpty()) {
            LOG.info("Recipe source priority: {}", String.join(" > ", merged));
        }
        return merged;
    }
}
