package org.cloudfoundry.community.servicebroker.vrealize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.log4j.Logger;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.DeleteServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.OperationState;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceLastOperation;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.cloudfoundry.community.servicebroker.vrealize.domain.VrServiceInstance;
import org.cloudfoundry.community.servicebroker.vrealize.service.CatalogService;
import org.cloudfoundry.community.servicebroker.vrealize.service.TokenService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
@Ignore
public class ServiceInstanceServiceTest {

	private static final Logger LOG = Logger
			.getLogger(ServiceInstanceServiceTest.class);

	@Autowired
	@InjectMocks
	ServiceInstanceService service;

	@Mock
	VraClient vraClient;

	@Mock
	TokenService tokenService;

	@Autowired
	CatalogService catalogService;

	@Before
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);

		when(tokenService.getToken()).thenReturn("1234abcd");

		when(
				vraClient.getCreateRequestTemplate(Matchers.anyString(),
						any(ServiceDefinition.class))).thenReturn(
				getJsonElement("requestTemplate.json"));

		when(
				vraClient.prepareCreateRequestTemplate(any(JsonElement.class),
						Matchers.anyString())).thenCallRealMethod();

		when(
				vraClient.postCreateRequest(Matchers.anyString(),
						any(JsonElement.class), any(ServiceDefinition.class)))
				.thenReturn(getJsonElement("requestResponse.json"));

		when(vraClient.getRequestId(any(JsonElement.class)))
				.thenCallRealMethod();

		when(vraClient.getParameters(any(JsonElement.class)))
				.thenCallRealMethod();

		when(
				vraClient.loadMetadata(Matchers.anyString(),
						any(VrServiceInstance.class))).thenCallRealMethod();

		ServiceInstanceLastOperation silo = new ServiceInstanceLastOperation(
				"aRequestId", OperationState.SUCCEEDED);

		when(
				vraClient.getRequestStatus(Matchers.anyString(),
						any(VrServiceInstance.class))).thenReturn(silo);

		when(
				vraClient.getDeleteRequestTemplate(Matchers.anyString(),
						any(VrServiceInstance.class))).thenReturn(
				getJsonElement("deleteTemplate.json"));

		when(
				vraClient.prepareDeleteRequestTemplate(any(JsonElement.class),
						Matchers.anyString())).thenCallRealMethod();

		when(
				vraClient.postDeleteRequest(Matchers.anyString(),
						any(JsonElement.class), any(VrServiceInstance.class)))
				.thenReturn(getJsonElement("deleteRequest.json"));
	}

	private static final String SD_ID = "e06ff060-dc7a-4f46-a7a7-c32c031fa31e";
	private static final String P_ID = "e06ff060-dc7a-4f46-a7a7-c32c031fa31e";

	@Test
	public void testLifecycle() throws Exception {
		CreateServiceInstanceRequest creq = new CreateServiceInstanceRequest(
				SD_ID, P_ID, "orgId", "spaceId", true, null);
		creq.withServiceInstanceId("12345");

		VrServiceInstance instance = (VrServiceInstance) service
				.createServiceInstance(creq);
		assertNotNull(instance);
		assertTrue(instance.isAsync());
		assertTrue(instance.isCurrentOperationCreate());
		assertTrue(instance.isInProgress());

		String state = instance.getServiceInstanceLastOperation().getState();
		assertEquals("in progress", state);

		while (state.equals("in progress")) {
			LOG.info("request status: " + state);

			// pretend this is taking a few seconds...
			Thread.sleep(10000);
			instance = (VrServiceInstance) service.getServiceInstance(instance
					.getServiceInstanceId());
			state = instance.getServiceInstanceLastOperation().getState();
		}
		LOG.info("request status: " + state);
		assertEquals("succeeded", state);

		DeleteServiceInstanceRequest dreq = new DeleteServiceInstanceRequest(
				instance.getServiceInstanceId(), SD_ID, P_ID, true);

		instance = (VrServiceInstance) service.deleteServiceInstance(dreq);
		state = instance.getServiceInstanceLastOperation().getState();
		assertEquals("in progress", state);

		while (state.equals("in progress")) {
			LOG.info("request status: " + state);
			Thread.sleep(10000);
			instance = (VrServiceInstance) service.getServiceInstance(instance
					.getServiceInstanceId());
			state = instance.getServiceInstanceLastOperation().getState();

		}
		LOG.info("request status: " + state);
		assertEquals("succeeded", state);
	}

	private JsonElement getJsonElement(String fileName) throws Exception {
		JsonParser parser = new JsonParser();
		JsonElement o = (JsonElement) parser.parse(getContents(fileName));

		return o;
	}

	private String getContents(String fileName) throws ServiceBrokerException {
		try {
			URI u = new ClassPathResource(fileName).getURI();
			return new String(Files.readAllBytes(Paths.get(u)));
		} catch (IOException e) {
			throw new ServiceBrokerException("error reading template.", e);
		}
	}
}
