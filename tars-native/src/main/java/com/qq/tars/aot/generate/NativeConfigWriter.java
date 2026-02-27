package com.qq.tars.aot.generate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.qq.tars.aot.api.JdkProxyDescriber;
import com.qq.tars.aot.api.ResourceDescriber;
import com.qq.tars.aot.api.TypeDescriber;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serialises {@link TypeDescriber}, {@link JdkProxyDescriber} and
 * {@link ResourceDescriber} lists into GraalVM native-image JSON config files.
 */
public final class NativeConfigWriter {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private NativeConfigWriter() {
    }

    /**
     * Writes {@code reflect-config.json}.
     */
    public static void writeReflectConfig(List<TypeDescriber> types, Path outputDir) throws IOException {
        List<Map<String, Object>> entries = new ArrayList<>();
        for (TypeDescriber td : types) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("name", td.getClassName());
            if (td.isAllDeclaredConstructors()) entry.put("allDeclaredConstructors", true);
            if (td.isAllDeclaredMethods()) entry.put("allDeclaredMethods", true);
            if (td.isAllDeclaredFields()) entry.put("allDeclaredFields", true);
            entries.add(entry);
        }
        writeJson(entries, outputDir.resolve("reflect-config.json"));
    }

    /**
     * Writes {@code proxy-config.json}.
     */
    public static void writeProxyConfig(List<JdkProxyDescriber> proxies, Path outputDir) throws IOException {
        List<Map<String, Object>> entries = new ArrayList<>();
        for (JdkProxyDescriber pd : proxies) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("interfaces", pd.getInterfaces());
            entries.add(entry);
        }
        writeJson(entries, outputDir.resolve("proxy-config.json"));
    }

    /**
     * Writes {@code resource-config.json}.
     */
    public static void writeResourceConfig(List<ResourceDescriber> resources, Path outputDir) throws IOException {
        List<Map<String, Object>> patterns = new ArrayList<>();
        for (ResourceDescriber rd : resources) {
            Map<String, Object> p = new HashMap<>();
            p.put("pattern", rd.getPattern());
            patterns.add(p);
        }
        Map<String, Object> includes = new HashMap<>();
        includes.put("includes", patterns);
        Map<String, Object> root = new HashMap<>();
        root.put("resources", includes);
        writeJson(root, outputDir.resolve("resource-config.json"));
    }

    private static void writeJson(Object obj, Path file) throws IOException {
        Files.createDirectories(file.getParent());
        try (Writer writer = Files.newBufferedWriter(file)) {
            GSON.toJson(obj, writer);
        }
    }
}
