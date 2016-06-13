/**
 * vrealize-service-broker
 * <p>
 * Copyright (c) 2015-Present Pivotal Software, Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * limitations under the License.
 */

package org.cloudfoundry.community.servicebroker.vrealize;

import org.cloudfoundry.community.servicebroker.vrealize.domain.Creds;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.VrServiceInstance;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.VrServiceInstanceBinding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@Profile("cloud")
public class CloudConfig {

    @Autowired
    private Environment env;

    @Bean
    Creds creds() {
        return new Creds(env.getProperty("VRA_USER_ID"),
                env.getProperty("VRA_USER_PASSWORD"),
                env.getProperty("VRA_TENANT"));
    }

    @Bean
    String serviceUri() {
        return env.getProperty("SERVICE_URI");
    }

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setUsePool(true);
        return factory;
    }

    @Bean
    @Qualifier("siTemplate")
    RedisTemplate<String, VrServiceInstance> siTemplate() {
        RedisTemplate<String, VrServiceInstance> template = new RedisTemplate<String, VrServiceInstance>();
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }

    @Bean
    @Qualifier("sibTemplate")
    RedisTemplate<String, VrServiceInstanceBinding> sibTemplate() {
        RedisTemplate<String, VrServiceInstanceBinding> template = new RedisTemplate<String, VrServiceInstanceBinding>();
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }
}