package org.cloudfoundry.community.servicebroker.vrealize;

import static org.junit.Assert.assertNotNull;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
@Ignore
public class LifecycleTest {

	private static final Logger LOG = Logger.getLogger(LifecycleTest.class);

	private static final String REQUEST_ID_TO_DELETE = "bda7d982-cf75-456c-b8f2-550e36cbb5eb";
	private static final String S_ID = "aServiceInstanceId";

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

		LOG.info("get a request template.");
		JsonElement createTemplate = client.getCreateRequestTemplate(token, sd);
		assertNotNull(createTemplate);

		LOG.info("fill in template.");
		JsonObject editedCreateTemplate = client.prepareCreateRequestTemplate(
				createTemplate, S_ID);
		assertNotNull(editedCreateTemplate);

		LOG.info("post request.");
		ResponseEntity<JsonElement> requestResponse = client.postCreateRequest(
				token, editedCreateTemplate, sd);
		assertNotNull(requestResponse);

		String requestId = client.getRequestId(requestResponse);
		LOG.info("request status is: " + requestResponse.getStatusCode()
				+ " requestId is: " + requestId);
		assertNotNull(requestId);
		
		String hrid = requestResponse.getHeaders().getLocation().toString();
		LOG.info("location: " + hrid);

		LOG.info("wait for create request to complete....");
		ServiceInstanceLastOperation silo = client.getRequestStatus(token,
				requestId);
		assertNotNull(silo);
		while (silo.getState().equals(
				VrServiceInstance.OPERATION_STATE_IN_PROGRESS)) {
			TimeUnit.SECONDS.sleep(10);
			silo = client.getRequestStatus(token, requestId);
			LOG.info("state is: " + silo.getState() + ": " + silo.getDescription());
		}

		LOG.info("state is: "
				+ client.getRequestStatus(token, requestId).getState());
	}

	@Test
	public void testDelete() throws Exception {
		LOG.info("get a token.");
		String token = tokenService.getToken();
		assertNotNull(token);

		LOG.info("get a delete template.");
		JsonElement deleteRequestTemplate = client.getDeleteRequestTemplate(
				token, REQUEST_ID_TO_DELETE);
		assertNotNull(deleteRequestTemplate);

		LOG.info("edit the delete template.");
		JsonObject editedDeleteRequestTemplate = client
				.prepareDeleteRequestTemplate(deleteRequestTemplate,
						S_ID);
		assertNotNull(editedDeleteRequestTemplate);

		LOG.info("posting delete request: "
				+ editedDeleteRequestTemplate.toString());
		ResponseEntity<JsonElement> deleteResponse = client.postDeleteRequest(
				token, editedDeleteRequestTemplate, REQUEST_ID_TO_DELETE);

		LOG.info("delete request status is: " + deleteResponse.getStatusCode());

		LOG.info("wait for delete request to complete....");
		ServiceInstanceLastOperation silo = client.getRequestStatus(token,
				REQUEST_ID_TO_DELETE);
		assertNotNull(silo);
		while (silo.getState().equals(
				VrServiceInstance.OPERATION_STATE_IN_PROGRESS)) {
			TimeUnit.SECONDS.sleep(10);
			silo = client.getRequestStatus(token, REQUEST_ID_TO_DELETE);
			LOG.info("state is: " + silo.getState());
		}

		LOG.info("state is: "
				+ client.getRequestStatus(token, REQUEST_ID_TO_DELETE)
						.getState());
	}

}