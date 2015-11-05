package org.cloudfoundry.community.servicebroker.vrealize;

import java.net.URI;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.DeleteServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.vrealize.domain.Creds;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.VrServiceInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;

@Configuration
public class TestConfig {

	public static final String SD_ID = "71d3235c-f5f9-4140-94a4-64d375cbd783";
	public static final String R_ID = "4331735d-4a47-4055-b82e-21eb7666f18f";
	public static final String P_ID = "e06ff060-dc7a-4f46-a7a7-c32c031fa31e";
	public static final String MONGO_DB_NAME = "test-mongo-db";

	@Bean
	Creds creds() {
		return new Creds("vdude1", "P1v0t4l!", "lab");
	}

	@Bean
	String serviceUri() {
		return "https://vra.vra.lab";
	}

	public @Bean MongoTemplate mongoTemplate(Mongo mongo)
			throws UnknownHostException {
		return new MongoTemplate(mongo, MONGO_DB_NAME);
	}

	public @Bean MongoClient mongoClient() throws UnknownHostException {
		return new MongoClient("localhost");
	}

	public static String getContents(String fileName) throws Exception {
		URI u = new ClassPathResource(fileName).getURI();
		return new String(Files.readAllBytes(Paths.get(u)));
	}

	public static CreateServiceInstanceRequest getCreateServiceInstanceRequest() {
		CreateServiceInstanceRequest req = new CreateServiceInstanceRequest(
				SD_ID, "pId", "orgId", "spaceId", true, null);
		req.withServiceInstanceId("anID");
		return req;
	}

	public static DeleteServiceInstanceRequest getDeleteServiceInstanceRequest() {
		CreateServiceInstanceRequest creq = getCreateServiceInstanceRequest();
		DeleteServiceInstanceRequest dreq = new DeleteServiceInstanceRequest(
				creq.getServiceInstanceId(), creq.getServiceDefinitionId(),
				creq.getPlanId(), true);
		return dreq;
	}

	public static VrServiceInstance getServiceInstance() {
		VrServiceInstance si = VrServiceInstance.create(
				getCreateServiceInstanceRequest(), "12345");
		si.getParameters().put(VrServiceInstance.SERVICE_TYPE, "mysql");
		si.getParameters().put(VrServiceInstance.DB_ID, "aDB");
		si.getParameters().put(VrServiceInstance.HOST, "aHost");
		si.getParameters().put(VrServiceInstance.PASSWORD, "secret");
		si.getParameters().put(VrServiceInstance.PORT, "1234");
		si.getParameters().put(VrServiceInstance.USER_ID, "aUser");
		return si;
	}

	public static CreateServiceInstanceBindingRequest getCreateBindingRequest() {
		VrServiceInstance si = getServiceInstance();
		CreateServiceInstanceBindingRequest req = new CreateServiceInstanceBindingRequest(
				si.getServiceDefinitionId(), si.getPlanId(), "anAppId",
				si.getParameters());
		req.withBindingId("98765");
		req.withServiceInstanceId(si.getServiceInstanceId());
		return req;
	}

	public static ServiceInstanceBinding getServiceInstanceBinding()
			throws ServiceBrokerException {
		CreateServiceInstanceBindingRequest req = getCreateBindingRequest();
		return new ServiceInstanceBinding(req.getBindingId(),
				req.getServiceInstanceId(), getServiceInstance()
						.getCredentials(), null, req.getAppGuid());
	}

	public static DeleteServiceInstanceBindingRequest getDeleteBindingRequest() {
		VrServiceInstance si = getServiceInstance();
		CreateServiceInstanceBindingRequest creq = getCreateBindingRequest();
		DeleteServiceInstanceBindingRequest dreq = new DeleteServiceInstanceBindingRequest(
				creq.getBindingId(), si, si.getServiceDefinitionId(),
				si.getPlanId());
		return dreq;
	}

}