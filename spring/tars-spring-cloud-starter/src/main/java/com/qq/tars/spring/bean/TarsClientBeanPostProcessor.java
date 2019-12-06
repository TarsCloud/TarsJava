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

package com.qq.tars.spring.bean;

import com.qq.tars.client.Communicator;
import com.qq.tars.client.CommunicatorConfig;
import com.qq.tars.client.CommunicatorFactory;
import com.qq.tars.client.ServantProxyConfig;
import com.qq.tars.common.util.BeanAccessor;
import com.qq.tars.common.util.StringUtils;
import com.qq.tars.protocol.annotation.Servant;
import com.qq.tars.register.RegisterHandler;
import com.qq.tars.register.RegisterManager;
import com.qq.tars.server.config.ConfigurationManager;
import com.qq.tars.spring.annotation.TarsClient;
import com.qq.tars.spring.config.TarsClientProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

public class TarsClientBeanPostProcessor implements BeanPostProcessor, InitializingBean, ApplicationContextAware {

    @Autowired
    private TarsClientProperties clientProperties;

    private Communicator communicator;

    private ApplicationContext applicationContext;

    public TarsClientBeanPostProcessor() {
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            RegisterManager.getInstance().setHandler(applicationContext.getBean(RegisterHandler.class));
        } catch (Exception e) {
        }
        communicator = CommunicatorFactory.getInstance().getCommunicator(clientProperties);
        BeanAccessor.setBeanValue(CommunicatorFactory.getInstance(), "communicator", communicator);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class clazz = bean.getClass();
        processFields(bean, clazz.getDeclaredFields());
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    private void processFields(Object bean, Field[] declaredFields) {
        for (Field field : declaredFields) {
            TarsClient annotation = AnnotationUtils.getAnnotation(field, TarsClient.class);
            if (annotation == null) {
                continue;
            }

            if (field.getType().getAnnotation(Servant.class) == null) {
                throw new RuntimeException("[TARS] autoware client failed: target field is not  tars  client");
            }

            String objName = annotation.name();

            if (StringUtils.isEmpty(annotation.value())) {
                throw new RuntimeException("[TARS] autoware client failed: objName is empty");
            }

            ServantProxyConfig config = new ServantProxyConfig(objName);
            CommunicatorConfig communicatorConfig = ConfigurationManager.getInstance().getServerConfig().getCommunicatorConfig();
            config.setModuleName(communicatorConfig.getModuleName(), communicatorConfig.isEnableSet(), communicatorConfig.getSetDivision());
            config.setEnableSet(annotation.enableSet());
            config.setSetDivision(annotation.setDivision());
            if (StringUtils.isNotEmpty(annotation.setDivision())) {
                config.setEnableSet(true);
                config.setSetDivision(annotation.setDivision());
            }
            config.setConnections(annotation.connections());
            config.setConnectTimeout(annotation.connectTimeout());
            config.setSyncTimeout(annotation.syncTimeout());
            config.setAsyncTimeout(annotation.asyncTimeout());
            config.setTcpNoDelay(annotation.tcpNoDelay());
            config.setCharsetName(annotation.charsetName());

            Object proxy = communicator.stringToProxy(field.getType(), config);

            ReflectionUtils.makeAccessible(field);
            ReflectionUtils.setField(field, bean, proxy);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
