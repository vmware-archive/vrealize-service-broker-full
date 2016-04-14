package org.cloudfoundry.community.servicebroker.vrealize;

import org.apache.commons.collections.map.HashedMap;
import org.cloudfoundry.community.servicebroker.vrealize.adapter.MySqlAdapter;
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
import java.util.HashMap;
import java.util.Map;

@Configuration
@PropertySource("classpath:test.properties")
public class TestConfig {

    public static final String SD_ID = "a3d19350-c15e-4d81-878a-38f4868a4c95";
    public static final String REQ_ID = "e687dc4c-b8d6-44c9-af01-06665dce89fc";
    public static final String LOCATION = "https://vra-cafe.vra.pcflab.net/catalog-service/api/consumer/requests/" + REQ_ID;

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
        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put(MySqlAdapter.DB_NAME, "aDB");
        parms.put(MySqlAdapter.DB_ROOT_PASSWORD, "secret");
        parms.put(MySqlAdapter.DB_PORT, "1234");

        CreateServiceInstanceRequest req = new CreateServiceInstanceRequest(
                SD_ID, "pId", "orgId", "spaceId", parms);
        req.withServiceInstanceId("anID");
        return req;
    }

    public static CreateServiceInstanceRequest getCreateServiceInstanceRequest(
            ServiceDefinition sd) {

        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put(MySqlAdapter.DB_NAME, "aDB");
        parms.put(MySqlAdapter.DB_ROOT_PASSWORD, "secret");
        parms.put(MySqlAdapter.DB_PORT, "1234");

        CreateServiceInstanceRequest req = new CreateServiceInstanceRequest(
                sd.getId(), sd.getPlans().get(0).getId(), "testOrgId",
                "testSpaceId", parms);
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
        si.getMetadata().put(VrServiceInstance.SERVICE_TYPE, "mysql");
        si.getMetadata().put(VrServiceInstance.HOST, "aHost");
        si.getMetadata().put(VrServiceInstance.LOCATION,LOCATION);

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