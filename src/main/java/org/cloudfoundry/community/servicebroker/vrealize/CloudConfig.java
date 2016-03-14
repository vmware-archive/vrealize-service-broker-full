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