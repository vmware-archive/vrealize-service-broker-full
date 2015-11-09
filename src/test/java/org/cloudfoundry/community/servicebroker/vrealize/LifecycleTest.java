package org.cloudfoundry.community.servicebroker.vrealize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.model.OperationState;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceLastOperation;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.VrServiceInstance;
import org.cloudfoundry.community.servicebroker.vrealize.service.CatalogService;
import org.cloudfoundry.community.servicebroker.vrealize.service.TokenService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
@Ignore
public class LifecycleTest {

	private static final Logger LOG = Logger.getLogger(LifecycleTest.class);

	private static final String R_ID = "8720ac04-9910-4426-b8b3-758f6e02e3bc";

	@Autowired
	private VraClient client;

	@Autowired
	TokenService tokenService;

	@Autowired
	CatalogService catalogService;

	@Autowired
	Gson gson;

	@Autowired
	VraRepository repo;

	@Test
	public void testCreate() throws Exception {
		LOG.info("get a token.");
		String token = tokenService.getToken();
		assertNotNull(token);

		LOG.info("get a service def.");
		ServiceDefinition sd = catalogService
				.getServiceDefinition(TestConfig.SD_ID);
		assertNotNull(sd);

		LOG.info("submitting create request.");
		VrServiceInstance instance = client.createInstance(
				TestConfig.getCreateServiceInstanceRequest(sd), sd);

		Object requestId = instance.getCreateRequestId();
		assertNotNull(requestId);

		String status = instance.getServiceInstanceLastOperation().getState();
		assertNotNull(status);

		LOG.info("request status is: " + status + " requestId is: " + requestId);

		Object location = instance.getLocation();
		assertNotNull(location);

		LOG.info("location: " + location);

		LOG.info("wait for create request to complete....");

		ServiceInstanceLastOperation silo = client.getRequestStatus(token,
				requestId.toString());
		assertNotNull(silo);
		while (silo.getState().equals(
				VrServiceInstance.OPERATION_STATE_IN_PROGRESS)) {
			TimeUnit.SECONDS.sleep(10);
			silo = client.getRequestStatus(token, requestId.toString());
			LOG.info("state is: " + silo.getState() + ": "
					+ silo.getDescription());
		}

		LOG.info("state is: "
				+ client.getRequestStatus(token, requestId.toString()).getState());
	}

	@Test
	public void testGetStatus() throws ServiceBrokerException {
		VrServiceInstance instance = TestConfig.getServiceInstance();
		ServiceInstanceLastOperation silo = new ServiceInstanceLastOperation(
				R_ID, OperationState.IN_PROGRESS);
		instance.withLastOperation(silo);
		ServiceInstanceLastOperation status = client.getRequestStatus(instance);
		assertNotNull(status);
		assertEquals("succeeded", status.getState());
	}

	@Test
	public void testDelete() throws Exception {
		VrServiceInstance instance = getServiceInstanceToDelete();

		instance = client.deleteInstance(instance);

		// // LOG.info("get a token.");
		// // String token = tokenService.getToken();
		// // assertNotNull(token);
		//
		// LOG.info("get a delete template.");
		// // JsonElement deleteRequestTemplate =
		// client.getDeleteRequestTemplate(
		// // token, R_ID);
		//
		// JsonElement deleteRequestTemplate =
		// client.getDeleteRequestTemplate(instance);
		// assertNotNull(deleteRequestTemplate);
		//
		// LOG.info("edit the delete template.");
		// JsonObject editedDeleteRequestTemplate = client
		// .prepareDeleteRequestTemplate(deleteRequestTemplate, S_ID);
		// assertNotNull(editedDeleteRequestTemplate);
		//
		// LOG.info("posting delete request: "
		// + editedDeleteRequestTemplate.toString());
		// ResponseEntity<JsonElement> deleteResponse =
		// client.postDeleteRequest(
		// token, editedDeleteRequestTemplate, R_ID);

		String requestId = instance.getCurrentOperationRequestId();
		assertNotNull(requestId);
		LOG.info("delete request id: " + requestId);

		String status = instance.getServiceInstanceLastOperation().getState();
		assertNotNull(status);
		LOG.info("delete request state: " + status);

		// LOG.info("wait for delete request to complete....");
		// ServiceInstanceLastOperation silo = client
		// .getRequestStatus(token, R_ID);
		// assertNotNull(silo);
		// while (silo.getState().equals(
		// VrServiceInstance.OPERATION_STATE_IN_PROGRESS)) {
		// TimeUnit.SECONDS.sleep(10);
		// silo = client.getRequestStatus(token, R_ID);
		// LOG.info("state is: " + silo.getState());
		// }
		//
		// LOG.info("state is: " + client.getRequestStatus(token,
		// R_ID).getState());
	}

	private VrServiceInstance getServiceInstanceToDelete() throws Exception {
		ServiceDefinition sd = catalogService
				.getServiceDefinition(TestConfig.SD_ID);

		VrServiceInstance instance = VrServiceInstance.create(TestConfig
				.getCreateServiceInstanceRequest(sd));

		instance.getMetadata().put(VrServiceInstance.CREATE_REQUEST_ID, R_ID);
		instance.getMetadata().put(VrServiceInstance.LOCATION, "foo" + R_ID);

		JsonParser parser = new JsonParser();
		JsonElement je = (JsonElement) parser.parse(TestConfig
				.getContents("requestResponse.json"));

		instance.getParameters().putAll(
				client.getParametersFromCreateResponse(je));

		client.loadDataFromResourceResponse(tokenService.getToken(), instance);

		return instance;
	}

}