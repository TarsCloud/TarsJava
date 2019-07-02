/**
 * Tencent is pleased to support the open source community by making Tars available.
 *
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.qq.tars.server.apps;

import com.qq.tars.common.support.Endpoint;
import com.qq.tars.common.util.StringUtils;
import com.qq.tars.net.core.Processor;
import com.qq.tars.protocol.annotation.Servant;
import com.qq.tars.protocol.util.TarsHelper;
import com.qq.tars.rpc.protocol.Codec;
import com.qq.tars.server.config.ConfigurationManager;
import com.qq.tars.server.config.ServantAdapterConfig;
import com.qq.tars.server.config.ServerConfig;
import com.qq.tars.server.core.AppContextListener;
import com.qq.tars.server.core.ServantAdapter;
import com.qq.tars.server.core.ServantHomeSkeleton;
import com.qq.tars.spring.annotation.TarsListener;
import com.qq.tars.spring.annotation.TarsServant;
import com.qq.tars.spring.config.ListenerConfig;
import com.qq.tars.spring.config.ServantConfig;
import com.qq.tars.support.om.OmConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.Map;

public class SpringBootAppContext extends BaseAppContext {
    
    private final static Logger logger = LoggerFactory.getLogger(SpringBootAppContext.class);
    
    private ApplicationContext applicationContext;

    public SpringBootAppContext(ApplicationContext applicationContext) {
        super();
        this.applicationContext = applicationContext;
    }

    @Override
    protected void loadServants() {
        loadAppContextListeners(this.applicationContext);
        loadAppContextListeners();
        
        //load Adapter
        loadAppServants(this.applicationContext);
        loadDefaultFilter();
        loadAppFilters(this.applicationContext);
        
        
        loadAppServants();
    }

    private void loadAppContextListeners() {
        Map<String, Object> listenerMap = applicationContext.getBeansWithAnnotation(TarsListener.class);
        for (Map.Entry<String, Object> entry : listenerMap.entrySet()) {
            AppContextListener listener;

            try {
                listener = (AppContextListener) entry.getValue();
                listeners.add(listener);
            } catch (ClassCastException e) {
                System.err.println("invalid listener config|It is NOT a ContextListener:" + entry.getValue().getClass());
            } catch (Exception e) {
                System.err.println("create listener instance failed.");
                e.printStackTrace();
            }
        }
    }

    private void loadAppServants() {
        Map<String, Object> servantMap = applicationContext.getBeansWithAnnotation(TarsServant.class);
        for (Map.Entry<String, Object> entry : servantMap.entrySet()) {
            try {
                ServantHomeSkeleton skeleton = loadServant(entry.getValue());
                skeletonMap.put(skeleton.name(), skeleton);
                appServantStarted(skeleton);
            } catch (Exception e) {
                System.err.println("init a Servant failed");
                e.printStackTrace();
            }
        }

    }

    private ServantHomeSkeleton loadServant(Object bean) throws Exception {
        String homeName = null;
        Class<?> homeApiClazz = null;
        Class<? extends Codec> codecClazz = null;
        Class<? extends Processor> processorClazz = null;
        Object homeClassImpl = null;
        ServantHomeSkeleton skeleton = null;
        int maxLoadLimit = -1;

        ServerConfig serverCfg = ConfigurationManager.getInstance().getServerConfig();

        homeName = AnnotationUtils.getAnnotation(bean.getClass(), TarsServant.class).name();
        if (StringUtils.isEmpty(homeName)) {
            throw new RuntimeException("servant name is null.");
        }
        homeName = String.format("%s.%s.%s", serverCfg.getApplication(), serverCfg.getServerName(), homeName);
        Class implClass = bean.getClass();

        if (bean instanceof Advised) {
            implClass = ((Advised) bean).getTargetSource().getTargetClass();
        }

        for (Class clazz : implClass.getInterfaces()) {
            if (clazz.isAnnotationPresent(Servant.class)) {
                homeApiClazz = clazz;
                break;
            }
        }

        if (homeApiClazz == null)
            throw new Exception("servant is not TarServant");

        homeClassImpl = bean;

        if (TarsHelper.isServant(homeApiClazz)) {
            String servantName = homeApiClazz.getAnnotation(Servant.class).name();
            if (!StringUtils.isEmpty(servantName) && servantName.matches("^[\\w]+\\.[\\w]+\\.[\\w]+$")) {
                homeName = servantName;
            }
        }

        ServantAdapterConfig servantAdapterConfig = serverCfg.getServantAdapterConfMap().get(homeName);

        ServantAdapter ServerAdapter = new ServantAdapter(servantAdapterConfig);
        skeleton = new ServantHomeSkeleton(homeName, homeClassImpl, homeApiClazz, codecClazz, processorClazz, maxLoadLimit);
        skeleton.setAppContext(this);
        ServerAdapter.bind(skeleton);
        servantAdapterMap.put(homeName, ServerAdapter);
        return skeleton;
    }
    
    private void loadAppContextListeners(ApplicationContext applicationContext) {
        Map<String, ListenerConfig> servantMap = applicationContext.getBeansOfType(ListenerConfig.class);
        for (Map.Entry<String, ListenerConfig> entry : servantMap.entrySet()) {
            AppContextListener listener;

            listener = (AppContextListener) applicationContext.getBean(entry.getValue().getRef());
            listeners.add(listener);
        }
    }

    private void loadAppServants(ApplicationContext applicationContext) {
        Map<String, ServantConfig> servantMap = applicationContext.getBeansOfType(ServantConfig.class);
        
        //load Adapter
        this.loadServantAdapterConf(servantMap);
        
        for (Map.Entry<String, ServantConfig> entry : servantMap.entrySet()) {
            try {
                ServantHomeSkeleton skeleton = loadServant(entry.getValue());
                skeletonMap.put(skeleton.name(), skeleton);
                appServantStarted(skeleton);
            } catch (Exception e) {
                System.err.println("init a service failed");
                e.printStackTrace();
            }
        }
    }

    private void loadAppFilters(ApplicationContext applicationContext) {
        
    }

    private ServantHomeSkeleton loadServant(ServantConfig servantConfig) throws Exception {
        String homeName = null, homeApiName = null;
        Class<?> homeApiClazz = null;
        Class<? extends Codec> codecClazz = null;
        Class<? extends Processor> processorClazz = null;
        Object homeClassImpl = null;
        ServantHomeSkeleton skeleton = null;
        int maxLoadLimit = -1;

        ServerConfig serverCfg = ConfigurationManager.getInstance().getServerConfig();

        homeName = servantConfig.getName();
        if (StringUtils.isEmpty(homeName)) {
            throw new RuntimeException("servant name is null.");
        }
        homeName = String.format("%s.%s.%s", serverCfg.getApplication(), serverCfg.getServerName(), homeName);
        homeApiName = servantConfig.getInterface();

        homeApiClazz = Class.forName(homeApiName);
        homeClassImpl = this.applicationContext.getBean(servantConfig.getRef());

        if (TarsHelper.isServant(homeApiClazz)) {
            String servantName = homeApiClazz.getAnnotation(Servant.class).name();
            if (!StringUtils.isEmpty(servantName) && servantName.matches("^[\\w]+\\.[\\w]+\\.[\\w]+$")) {
                homeName = servantName;
            }
        }

        ServantAdapterConfig servantAdapterConfig = serverCfg.getServantAdapterConfMap().get(homeName);

        ServantAdapter ServerAdapter = new ServantAdapter(servantAdapterConfig);
        skeleton = new ServantHomeSkeleton(homeName, homeClassImpl, homeApiClazz, codecClazz, processorClazz, maxLoadLimit);
        skeleton.setAppContext(this);
        ServerAdapter.bind(skeleton);
        servantAdapterMap.put(homeName, ServerAdapter);
        return skeleton;
    }
    
    private void loadServantAdapterConf(Map<String, ServantConfig> servantMap)
    {
        boolean logDebug = ConfigurationManager.getInstance().getServerConfig().isLogDebug();
        
        if (logDebug)
        {
            logger.info("debug init create Adapter start....");
            ServerConfig serverCfg = ConfigurationManager.getInstance().getServerConfig();
            for (Map.Entry<String, ServantConfig> entry : servantMap.entrySet()) {
                String name = entry.getValue().getName();
                try {
                    ServantAdapterConfig servantAdapterConfig = new ServantAdapterConfig();
                    servantAdapterConfig.setEndpoint(new Endpoint("tcp", "0.0.0.0", 0, 60000, 0, 0, null));
                    String key = String.format("%s.%s.%s",serverCfg.getApplication(), serverCfg.getServerName(), name);
                    servantAdapterConfig.setServant(key);
                    //servantAdapterConfig.setMaxConns(200000);
                    //servantAdapterConfig.setQueueCap(10000);
                    servantAdapterConfig.setQueueTimeout(60000);
                    servantAdapterConfig.setThreads(10);
                    serverCfg.getServantAdapterConfMap().put(key,servantAdapterConfig);
                } catch (Exception e) {
                   // System.err.println("create Adapter init a service failed , name : " + name);
                   // e.printStackTrace();
                    logger.error("debug init create {} Adapter init a service failed , msg : {} ",name , e.getMessage() ,e);
                }
            }
            logger.info("debug init create Adapter start.... ok");
        }
    }
}
