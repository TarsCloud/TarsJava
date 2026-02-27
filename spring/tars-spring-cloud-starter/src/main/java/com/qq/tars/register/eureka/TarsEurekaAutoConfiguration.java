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

package com.qq.tars.register.eureka;

import com.qq.tars.spring.TarsConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.HealthCheckHandler;
import com.netflix.discovery.EurekaClient;
import com.qq.tars.register.RegisterHandler;

@Configuration
@EnableConfigurationProperties({ TarsEurekaClientConfig.class, TarsEurekaInstance.class })
@ConditionalOnProperty(value = "eureka.client.enabled", matchIfMissing = true)
@AutoConfigureAfter({ TarsConfiguration.class, EurekaClientAutoConfiguration.class })
public class TarsEurekaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(value = HealthCheckHandler.class, search = SearchStrategy.CURRENT)
    public HealthCheckHandler HealthCheckHandler() {
        return new TarsEurekaHealthCheckHandler();
    }

    @Bean
    public RegisterHandler registryHandler(EurekaClient client, TarsEurekaInstance instanceConfig,
                                           HealthCheckHandler healthCheckHandler,
                                           ApplicationInfoManager applicationInfoManager) {
        applicationInfoManager.registerStatusChangeListener(new TarsStatusChangeListener());
        return new EurekaRegisterHandler(client, instanceConfig, healthCheckHandler, applicationInfoManager);
    }

}
