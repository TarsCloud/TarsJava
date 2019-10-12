package com.qq.tars.spring.bean;

import com.qq.tars.server.core.Server;
import com.qq.tars.spring.annotation.RemoteConfigSource;
import com.qq.tars.support.config.ConfigHelper;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConfigSourceListener implements ApplicationListener<ApplicationStartingEvent> {

    private static final AtomicBoolean INIT = new AtomicBoolean();

    @Override
    public void onApplicationEvent(ApplicationStartingEvent event) {
        if (!INIT.compareAndSet(false, true)) {
            return;
        }

        RemoteConfigSource sources = event.getSpringApplication().getMainApplicationClass().getAnnotation(RemoteConfigSource.class);

        if (sources != null) {
            String configPath = Server.getInstance().getServerConfig().getBasePath() + "/conf/";
            Path path = Paths.get(configPath);
            File configDirectory = path.toFile();
            if (configDirectory.isDirectory()) {
                File[] files = configDirectory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (!file.delete()) {
                            throw new RuntimeException("[TARS] delete legacy config failed: " + file.getName());
                        }
                    }
                }
            }

            for (String name : sources.value()) {
                if (!ConfigHelper.getInstance().loadConfig(name)) {
                    throw new RuntimeException("[TARS] load config failed: " + name);
                } else {
                    System.out.println("[TARS] load config: " + name);
                }
            }
        }
    }
}
