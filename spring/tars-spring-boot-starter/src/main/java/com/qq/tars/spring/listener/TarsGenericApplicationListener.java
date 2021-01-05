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
 * tars应用程序事件监听器
 * <ol>
 * <li>负责在应用程序启动之前初始化Tars配置并拉取远端配置，确保springboot能够正常读取远端配置(包括日志系统)</li>
 * <li>在应用程序环境准备就绪后注入拉取的配置文件</li>
 * <li>在应用程序启动失败时停止所有线程并输出错误提示</li>
 * </ol>
 *
 * @author kongyuanyuan
 */
public class TarsGenericApplicationListener implements GenericApplicationListener {
    /**
     * 要注入springboot环境的远程配置
     */
    private final Map<String, Properties> propertiesMap = new LinkedHashMap<>();
    /**
     * 是否拥有tars环境的启动配置。
     * System.getProperty("config")
     *
     * @see Server#init()
     */
    private final boolean hasConfigProperty;
    /**
     * 配置文件路径
     */
    private String configPath;

    public TarsGenericApplicationListener() {
        this.hasConfigProperty = StringUtils.hasText(System.getProperty("config"));
        if (hasConfigProperty) {
            //tars环境，初始化服务配置
            Server server = Server.getInstance();
            server.init();
            configPath = server.getServerConfig().getBasePath() + "/conf/";
        } else {
            //非tars环境，输出提示
            System.out.println("[TARS] Running in non-tars environment. Be careful to use the communicator.");
        }
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationEvent applicationEvent) {
        if (!hasConfigProperty) {
            //没有tars环境配置属性，说明是非tars环境，不处理
            return;
        }
        if (applicationEvent instanceof ApplicationStartingEvent) {
            //应用程序启动前，拉取远端配置，让日志系统能在环境准备就绪时自动初始化
            //同时注入默认配置，比如让spring来读取tars环境中的conf目录下的配置
            handleApplicationStartingEvent((ApplicationStartingEvent) applicationEvent);
        } else if (applicationEvent instanceof ApplicationEnvironmentPreparedEvent) {
            //环境准备就绪，此时可以注入配置了
            handleApplicationEnvironmentPreparedEvent((ApplicationEnvironmentPreparedEvent) applicationEvent);
        } else if (applicationEvent instanceof ApplicationFailedEvent) {
            //应用程序启动失败
            handleApplicationFailedEvent((ApplicationFailedEvent) applicationEvent);
        }
    }

    /**
     * 处理应用程序启动前的事件
     * <ol>
     *     <li>根据{@link RemotePropertySource}拉取远端配置到本地</li>
     *     <li>设置默认的配置</li>
     * </ol>
     *
     * @param applicationEvent ApplicationStartingEvent
     */
    private void handleApplicationStartingEvent(ApplicationStartingEvent applicationEvent) {
        //删除旧的配置文件
        deleteLegacyConfig();
        //加载要注入环境的远程配置
        loadRemotePropertiesConfig(applicationEvent.getSpringApplication().getMainApplicationClass());
        //加载远程配置到本地，但不注入environment
        loadRemoteConfig(applicationEvent.getSpringApplication().getMainApplicationClass());
        //设置默认的配置
        configDefaultProperties(applicationEvent);
    }

    /**
     * 加载要注入环境的properties配置文件
     *
     * @param mainClass mainClas
     */
    private void loadRemotePropertiesConfig(Class<?> mainClass) {
        RemotePropertySource sources = mainClass.getAnnotation(RemotePropertySource.class);
        if (sources == null) {
            //没有要加载的远程配置
            return;
        }
        loadRemoteConfig(sources.value(), true);
    }

    /**
     * 仅加载配置文件到本地但不会注入environment
     *
     * @param mainClass mainClass
     */
    private void loadRemoteConfig(Class<?> mainClass) {
        RemoteConfigSource sources = mainClass.getAnnotation(RemoteConfigSource.class);
        if (sources == null) {
            //没有要加载的远程配置
            return;
        }
        loadRemoteConfig(sources.value(), false);
    }

    /**
     * 加载要注入环境的properties配置文件
     *
     * @param filenameList 要加载的文件列表
     * @param inject       是否要解析并注入environment
     */
    private void loadRemoteConfig(final String[] filenameList, final boolean inject) {
        for (String name : filenameList) {
            Assert.state(StringUtils.hasText(name), "[TARS] The value property of @RemoteConfigSource/@RemotePropertySource cannot be empty");
            //load config from remote
            Assert.state(ConfigHelper.getInstance().loadConfig(name), "[TARS] failed to load config: " + name + ", probably due to failed to execute UpdateConfigCallback");
            System.out.println("[TARS] load config: " + name);
            if (inject) {
                //加载配置成功，注入properties
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
     * 应用程序启动失败
     * <p>
     * 强行停止程序，不要上报心跳了。
     *
     * @param applicationEvent ApplicationFailedEvent
     */
    private void handleApplicationFailedEvent(ApplicationFailedEvent applicationEvent) {
        //应用程序启动失败
        Throwable exception = applicationEvent.getException();
        System.out.println("[TARS] start application fail. exception:");
        exception.printStackTrace();
        //退出程序，结束所有线程
        System.exit(-1);
    }

    /**
     * 处理环境准备就绪的事件
     *
     * @param applicationEvent ApplicationEnvironmentPreparedEvent
     */
    private void handleApplicationEnvironmentPreparedEvent(ApplicationEnvironmentPreparedEvent applicationEvent) {
        //注入加载的properties配置文件到environment
        injectPropertySource(applicationEvent);
    }

    /**
     * 删除旧的配置文件
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
     * 配置默认的properties
     *
     * @param applicationEvent ApplicationStartingEvent
     */
    private void configDefaultProperties(ApplicationStartingEvent applicationEvent) {
        // 设置spring-boot启动参数
        Map<String, Object> defaultProperties = new LinkedHashMap<>();
        //设置一下配置文件的路径，让spring去读取tars平台下发的配置文件，包括日志系统
        defaultProperties.put("spring.config.additional-location", configPath);
        //特殊处理一下日志配置
        //第一个找到的配置文件会被当作默认配置
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
     * 把刚刚拉取的配置文件注入环境
     *
     * @param applicationEvent ApplicationEnvironmentPreparedEvent
     */
    private void injectPropertySource(ApplicationEnvironmentPreparedEvent applicationEvent) {
        propertiesMap.entrySet().stream()
                .map(entry -> new PropertiesPropertySource(entry.getKey(), entry.getValue()))
                .forEach(applicationEvent.getEnvironment().getPropertySources()::addFirst);
    }

    /**
     * 得到常用的日志配置文件名列表。会被自动注入到spring中，让spring来自动初始化日志系统。
     *
     * @return 日志配置文件名列表
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