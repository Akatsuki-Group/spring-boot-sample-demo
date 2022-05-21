/*
 * Tencent is pleased to support the open source community by making Spring Cloud Tencent available.
 *
 * Copyright (C) 2019 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package cn.polarismesh.boot.discovery.feign.autoconfig;

import cn.polarismesh.boot.discovery.feign.PolarisFeignBuilder;
import com.tencent.polaris.api.core.ConsumerAPI;
import com.tencent.polaris.client.api.SDKContext;
import com.tencent.polaris.factory.api.DiscoveryAPIFactory;
import feign.Feign;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(Feign.class)
public class FeignAutoConfiguration {
    @Bean
    public ConsumerAPI consumerAPI(SDKContext sdkContext) {
        return DiscoveryAPIFactory.createConsumerAPIByContext(sdkContext);
    }

    @Bean
    public PolarisFeignBuilder polarisTargetBuilder(ConsumerAPI consumerAPI) {
        return new PolarisFeignBuilder(consumerAPI);
    }
}
