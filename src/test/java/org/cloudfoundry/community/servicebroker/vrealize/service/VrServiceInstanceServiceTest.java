package org.cloudfoundry.community.servicebroker.vrealize.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.apache.log4j.Logger;
import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.DeleteServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.OperationState;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceLastOperation;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.cloudfoundry.community.servicebroker.vrealize.Application;
import org.cloudfoundry.community.servicebroker.vrealize.TestConfig;
import org.cloudfoundry.community.servicebroker.vrealize.VraClient;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.VrServiceInstance;
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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
@Ignore
public class VrServiceInstanceServiceTest {

	private static final Logger LOG = Logger
			.getLogger(VrServiceInstanceServiceTest.class);

	@Autowired
	@InjectMocks
	ServiceInstanceService service;

	@Autowired
	Gson gson;

	@Mock
	VraClient vraClient;

	@Mock
	TokenService tokenService;

	@Mock
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

		// ServiceDefinition sd = new ServiceDefinition(TestConfig.SD_ID,
		// "A Service Definition", "It's a great service", true, null);

		Catalog catalog = gson.fromJson(
				TestConfig.getContents("catItems.json"), Catalog.class);

		when(catalogService.getCatalog()).thenReturn(catalog);

		when(catalogService.getServiceDefinition(Matchers.anyString()))
				.thenCallRealMethod();

	}

	@Test
	public void testLifecycle() throws Exception {
		CreateServiceInstanceRequest creq = new CreateServiceInstanceRequest(
				TestConfig.SD_ID, TestConfig.P_ID, "orgId", "spaceId", true,
				null);
		creq.withServiceInstanceId("12345");

		VrServiceInstance instance = (VrServiceInstance) service
				.createServiceInstance(creq);
		assertNotNull(instance);
		assertTrue(instance.isAsync());
		assertTrue(instance.isCurrentOperationCreate());
		assertTrue(instance.isInProgress());
		assertEquals("12345", instance.getServiceInstanceId());

		String state = instance.getServiceInstanceLastOperation().getState();
		assertEquals("in progress", state);

		while (state.equals("in progress")) {
			LOG.info("request status: " + state);

			// pretend this is taking a few seconds...
			Thread.sleep(3000);
			instance = (VrServiceInstance) service.getServiceInstance(instance
					.getServiceInstanceId());
			assertNotNull(instance);
			assertEquals("12345", instance.getServiceInstanceId());
			state = instance.getServiceInstanceLastOperation().getState();
			assertNotNull(state);
		}

		LOG.info("request status: " + state);
		assertEquals("succeeded", state);

		DeleteServiceInstanceRequest dreq = new DeleteServiceInstanceRequest(
				instance.getServiceInstanceId(), TestConfig.SD_ID,
				TestConfig.P_ID, true);

		instance = (VrServiceInstance) service.deleteServiceInstance(dreq);
		assertNotNull(instance);
		assertEquals("12345", instance.getServiceInstanceId());
		state = instance.getServiceInstanceLastOperation().getState();
		assertNotNull(state);
		assertEquals("in progress", state);

		while (state.equals("in progress")) {
			LOG.info("request status: " + state);
			Thread.sleep(3000);
			instance = (VrServiceInstance) service.getServiceInstance(instance
					.getServiceInstanceId());
			assertNotNull(instance);
			assertEquals("12345", instance.getServiceInstanceId());
			state = instance.getServiceInstanceLastOperation().getState();
			assertNotNull(state);
		}

		LOG.info("request status: " + state);
		assertEquals("succeeded", state);
	}

	private JsonElement getJsonElement(String fileName) throws Exception {
		JsonParser parser = new JsonParser();
		JsonElement o = (JsonElement) parser.parse(TestConfig
				.getContents(fileName));

		return o;
	}
}
