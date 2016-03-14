package org.cloudfoundry.community.servicebroker.vrealize;

import org.cloudfoundry.community.servicebroker.vrealize.domain.Creds;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.LastOperation;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.VrServiceInstance;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.VrServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.vrealize.service.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
@PropertySource("classpath:test.properties")
public class TestConfig {

    public static final String SD_ID = "71d3235c-f5f9-4140-94a4-64d375cbd783";

    @Autowired
    Environment env;

    @Autowired
    CatalogService catalogService;

    @Bean
    Creds creds() {
        return new Creds(env.getProperty("vRuser"),
                env.getProperty("vRpass"), env.getProperty("vRtenant"));
    }

    @Bean
    String serviceUri() {
        return env.getProperty("vRserviceUri");
    }

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setHostName("localhost");
        factory.setPort(6379);
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

    public static String getContents(String fileName) throws Exception {
        URI u = new ClassPathResource(fileName).getURI();
        return new String(Files.readAllBytes(Paths.get(u)));
    }

    public static CreateServiceInstanceRequest getCreateServiceInstanceRequest() {
        CreateServiceInstanceRequest req = new CreateServiceInstanceRequest(
                SD_ID, "pId", "orgId", "spaceId", null);
        req.withServiceInstanceId("anID");
        return req;
    }

    public static CreateServiceInstanceRequest getCreateServiceInstanceRequest(
            ServiceDefinition sd) {
        CreateServiceInstanceRequest req = new CreateServiceInstanceRequest(
                sd.getId(), sd.getPlans().get(0).getId(), "testOrgId",
                "testSpaceId", null);
        req.withServiceInstanceId("anID");
        return req;
    }

//	public static DeleteServiceInstanceRequest getDeleteServiceInstanceRequest() {
//		CreateServiceInstanceRequest creq = getCreateServiceInstanceRequest();
//		DeleteServiceInstanceRequest dreq = new DeleteServiceInstanceRequest(
//				creq.getServiceInstanceId(), creq.getServiceDefinitionId(),
//				creq.getPlanId(), true);
//		return dreq;
//	}

    public static VrServiceInstance getServiceInstance() {
        VrServiceInstance si = new VrServiceInstance(getCreateServiceInstanceRequest());
        si.getParameters().put(VrServiceInstance.SERVICE_TYPE, "mysql");
        si.getParameters().put(VrServiceInstance.DB_ID, "aDB");
        si.getParameters().put(VrServiceInstance.HOST, "aHost");
        si.getParameters().put(VrServiceInstance.PASSWORD, "secret");
        si.getParameters().put(VrServiceInstance.PORT, "1234");
        si.getParameters().put(VrServiceInstance.USER_ID, "aUser");

        LastOperation lo = new LastOperation(OperationState.IN_PROGRESS, "aRequestId", false);
        si.withLastOperation(lo);

        si.getMetadata().put(VrServiceInstance.CREATE_REQUEST_ID, "aRequestId");
        return si;
    }

    public static CreateServiceInstanceBindingRequest getCreateBindingRequest() {
        VrServiceInstance si = getServiceInstance();
        CreateServiceInstanceBindingRequest req = new CreateServiceInstanceBindingRequest(
                si.getServiceDefinitionId(), si.getPlanId(), "anAppId",
                si.getParameters());
        req.withBindingId("98765");
        req.withServiceInstanceId(si.getId());
        return req;
    }

    public static VrServiceInstanceBinding getServiceInstanceBinding()
            throws ServiceBrokerException {
        CreateServiceInstanceBindingRequest req = getCreateBindingRequest();
        return new VrServiceInstanceBinding(req.getBindingId(),
                req.getServiceInstanceId(), getServiceInstance()
                .getCredentials(), null, null);
    }

    public DeleteServiceInstanceBindingRequest getDeleteBindingRequest() {
        VrServiceInstance si = getServiceInstance();
        CreateServiceInstanceBindingRequest creq = getCreateBindingRequest();
        return new DeleteServiceInstanceBindingRequest(
                si.getId(),
                creq.getBindingId(), si.getServiceDefinitionId(),
                si.getPlanId(), catalogService.getServiceDefinition(si.getServiceDefinitionId()));
    }

}