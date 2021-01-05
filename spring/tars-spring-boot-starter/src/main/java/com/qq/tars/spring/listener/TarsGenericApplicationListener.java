package com.qq.tars.spring.listener;

import com.qq.tars.rpc.exc.TarsException;
import com.qq.tars.server.core.Server;
import com.qq.tars.spring.annotation.RemoteConfigSource;
import com.qq.tars.spring.annotation.RemotePropertySource;
import com.qq.tars.support.config.ConfigHelper;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.boot.logging.AbstractLoggingSystem;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * tars generic application listener
 * <ol>
 * <li>Init tars config and load remote config when application is starting, make sure Spring environment will load properties appropriately, including logging system.</li>
 * <li>Inject the properties when the environment for application is prepared.</li>
 * <li>Stop the whole process when the application start failed.</li>
 * </ol>
 *
 * @author kongyuanyuan
 */
public class TarsGenericApplicationListener implements GenericApplicationListener {
    /**
     * properties to be injected into the environment
     */
    private final Map<String, Properties> propertiesMap = new LinkedHashMap<>();
    /**
     * is {@code System.getProperty("config")} presented
     *
     * @see Server#init()
     */
    private final boolean hasConfigProperty;
    /**
     * config path for tars
     */
    private String configPath;

    public TarsGenericApplicationListener() {
        this.hasConfigProperty = StringUtils.hasText(System.getProperty("config"));
        if (hasConfigProperty) {
            //init tars config
            Server server = Server.getInstance();
            server.init();
            //set config path
            configPath = server.getServerConfig().getBasePath() + "/conf/";
        } else {
            //running in non-tars environment, notice it
            System.out.println("[TARS] Running in non-tars environment. Be careful to use the communicator.");
        }
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationEvent applicationEvent) {
        if (!hasConfigProperty) {
            //non-tars environment, ignore all event
            return;
        }
        if (applicationEvent instanceof ApplicationStartingEvent) {
            //Init tars config and load remote config when application is starting,
            // make sure Spring environment will load properties appropriately, including logging system.
            //inject default properties, let spring do its job
            handleApplicationStartingEvent((ApplicationStartingEvent) applicationEvent);
        } else if (applicationEvent instanceof ApplicationEnvironmentPreparedEvent) {
            //the environment is prepared, inject properties
            handleApplicationEnvironmentPreparedEvent((ApplicationEnvironmentPreparedEvent) applicationEvent);
        } else if (applicationEvent instanceof ApplicationFailedEvent) {
            //application start failed
            handleApplicationFailedEvent((ApplicationFailedEvent) applicationEvent);
        }
    }

    /**
     * handle application starting event.
     * <ol>
     *     <li>load remote config for {@link RemotePropertySource} and {@link RemoteConfigSource}</li>
     *     <li>Inject default properties.</li>
     * </ol>
     *
     * @param applicationEvent ApplicationStartingEvent
     */
    private void handleApplicationStartingEvent(ApplicationStartingEvent applicationEvent) {
        //delete legacy config
        deleteLegacyConfig();
        //load remote config file for injection
        loadRemotePropertiesConfig(applicationEvent.getSpringApplication().getMainApplicationClass());
        //load remote config without injection
        loadRemoteConfig(applicationEvent.getSpringApplication().getMainApplicationClass());
        //set default config
        configDefaultProperties(applicationEvent);
    }

    /**
     * load remote config file for injection
     *
     * @param mainClass mainClas
     */
    private void loadRemotePropertiesConfig(Class<?> mainClass) {
        RemotePropertySource sources = mainClass.getAnnotation(RemotePropertySource.class);
        if (sources == null) {
            //nothing to load
            return;
        }
        loadRemoteConfig(sources.value(), true);
    }

    /**
     * load remote config without injection
     *
     * @param mainClass mainClass
     */
    private void loadRemoteConfig(Class<?> mainClass) {
        RemoteConfigSource sources = mainClass.getAnnotation(RemoteConfigSource.class);
        if (sources == null) {
            //nothing to do
            return;
        }
        loadRemoteConfig(sources.value(), false);
    }

    /**
     * load remote config file for injection
     *
     * @param filenameList file name list
     * @param inject       need to inject into the environment
     */
    private void loadRemoteConfig(final String[] filenameList, final boolean inject) {
        for (String name : filenameList) {
            Assert.state(StringUtils.hasText(name), "[TARS] The value property of @RemoteConfigSource/@RemotePropertySource cannot be empty");
            //load config from remote
            Assert.state(ConfigHelper.getInstance().loadConfig(name), "[TARS] failed to load config: " + name + ", probably due to failed to execute UpdateConfigCallback");
            System.out.println("[TARS] load config: " + name);
            if (inject) {
                //load config successfully, inject into properties
                File config = new File(configPath + name);
                Assert.state(config.exists(), "[TARS] read config file failed: file does not exist, filename:" + config.getAbsolutePath());
                Properties properties = new Properties();
                try (FileInputStream inStream = new FileInputStream(config)) {
                    properties.load(inStream);
                } catch (IOException exception) {
                    throw new TarsException("[TARS] read config file failed: IO exception:" + exception + ", filename:" + config.getAbsolutePath());
                }
                //put properties to map, which will be injected to the environment later
                propertiesMap.put(name, properties);
            }
        }
    }

    /**
     * application start failed
     * <p>
     * halt the application, don't report heartbeat
     *
     * @param applicationEvent ApplicationFailedEvent
     */
    private void handleApplicationFailedEvent(ApplicationFailedEvent applicationEvent) {
        Throwable exception = applicationEvent.getException();
        System.out.println("[TARS] start application fail. exception:");
        exception.printStackTrace();
        //halt, stop all threads
        System.exit(-1);
    }

    /**
     * handle environment prepared event
     *
     * @param applicationEvent ApplicationEnvironmentPreparedEvent
     */
    private void handleApplicationEnvironmentPreparedEvent(ApplicationEnvironmentPreparedEvent applicationEvent) {
        //inject properties into the environment
        injectPropertySource(applicationEvent);
    }

    /**
     * delete legacy config
     */
    private void deleteLegacyConfig() {
        Path path = Paths.get(configPath);
        File configDirectory = path.toFile();
        if (configDirectory.isDirectory()) {
            File[] files = configDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    try {
                        Files.delete(file.toPath());
                    } catch (IOException exception) {
                        throw new TarsException("[TARS] delete legacy config failed: " + file.getName(), exception);
                    }
                }
            }
        }
    }

    /**
     * set default properties
     *
     * @param applicationEvent ApplicationStartingEvent
     */
    private void configDefaultProperties(ApplicationStartingEvent applicationEvent) {
        Map<String, Object> defaultProperties = new LinkedHashMap<>();
        //set the config file location, let spring load the remote config
        defaultProperties.put("spring.config.additional-location", configPath);
        //config the logging system
        //the first logging config found will be used as default logging system config.
        for (String configName : getConventionLoggingConfig()) {
            Properties logbackConfig = propertiesMap.get(configName);
            if (logbackConfig != null) {
                defaultProperties.put("logging.config", configPath + configName);
                break;
            }
        }
        applicationEvent.getSpringApplication().setDefaultProperties(defaultProperties);
    }

    /**
     * inject the properties into the environment
     *
     * @param applicationEvent ApplicationEnvironmentPreparedEvent
     */
    private void injectPropertySource(ApplicationEnvironmentPreparedEvent applicationEvent) {
        propertiesMap.entrySet().stream()
                .map(entry -> new PropertiesPropertySource(entry.getKey(), entry.getValue()))
                .forEach(applicationEvent.getEnvironment().getPropertySources()::addFirst);
    }

    /**
     * get some convention logging config.
     *
     * @return logging config file name
     * @see org.springframework.boot.logging.LoggingSystem
     * @see AbstractLoggingSystem#initialize
     */
    private static String[] getConventionLoggingConfig() {
        return new String[]{"logback.groovy", "logback.xml",
                "log4j2.properties", "log4j2.yaml", "log4j2.yml", "log4j2.json", "log4j2.jsn", "log4j2.xml",
                "logging.properties"};
    }

    /**
     * Determine whether this listener actually supports the given event type.
     *
     * @param eventType the event type (never {@code null})
     */
    @Override
    public boolean supportsEventType(ResolvableType eventType) {
        Class<?> type = eventType.getRawClass();
        if (type == null) {
            return false;
        }
        return ApplicationStartingEvent.class.isAssignableFrom(type)
                || ApplicationEnvironmentPreparedEvent.class.isAssignableFrom(type)
                || ApplicationFailedEvent.class.isAssignableFrom(type);
    }

    /**
     * Determine whether this listener actually supports the given source type.
     *
     * @param sourceType sourceType
     */
    @Override
    public boolean supportsSourceType(Class<?> sourceType) {
        return true;
    }

    /**
     * Get the order value of this object.
     * <p>Higher values are interpreted as lower priority. As a consequence,
     * the object with the lowest value has the highest priority (somewhat
     * analogous to Servlet {@code load-on-startup} values).
     * <p>Same order values will result in arbitrary sort positions for the
     * affected objects.
     *
     * @return the order value
     * @see #HIGHEST_PRECEDENCE
     * @see #LOWEST_PRECEDENCE
     */
    @Override
    public int getOrder() {
        return 0;
    }
}