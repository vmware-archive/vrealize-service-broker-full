package org.cloudfoundry.community.servicebroker.vrealize;

import static org.junit.Assert.assertNotNull;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceLastOperation;
import org.cloudfoundry.community.servicebroker.vrealize.service.CatalogService;
import org.cloudfoundry.community.servicebroker.vrealize.service.TokenService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
//@Ignore
public class LifecycleTest {

	private static final Logger LOG = Logger.getLogger(LifecycleTest.class);

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
	public void testLifecycle() throws Exception {
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
				createTemplate, TestConfig.SD_ID);
		assertNotNull(editedCreateTemplate);

		LOG.info("post request.");
		JsonElement requestResponse = client.postCreateRequest(token,
				editedCreateTemplate, sd);
		assertNotNull(requestResponse);
		String requestId = client.getRequestId(requestResponse);
		LOG.info("requestId is: " + requestId);
		assertNotNull(requestId);

		LOG.info("wait for create request to complete....");
		ServiceInstanceLastOperation silo = client.getRequestStatus(token,
				requestId);
		assertNotNull(silo);
		while (silo.getState().equals(Constants.OPERATION_STATE_IN_PROGRESS)) {
			TimeUnit.SECONDS.sleep(10);
			silo = client.getRequestStatus(token, requestId);
			LOG.info("state is: " + silo.getState());
		}

		LOG.info("state is: "
				+ client.getRequestStatus(token, requestId).getState());

		LOG.info("get a delete template.");
		JsonElement deleteRequestTemplate = client.getDeleteRequestTemplate(
				token, requestId);
		assertNotNull(deleteRequestTemplate);

		LOG.info("edit the delete template.");
		JsonObject editedDeleteRequestTemplate = client
				.prepareDeleteRequestTemplate(deleteRequestTemplate, requestId);
		assertNotNull(editedDeleteRequestTemplate);

		LOG.info("posting delete request: " + editedDeleteRequestTemplate.toString());
		JsonElement deleteResponse = client.postDeleteRequest(token,
				editedDeleteRequestTemplate, requestId);
		//assertNotNull(deleteResponse);

		LOG.info("wait for delete request to complete....");
		silo = client.getRequestStatus(token, requestId);
		assertNotNull(silo);
		while (silo.getState().equals(Constants.OPERATION_STATE_IN_PROGRESS)) {
			TimeUnit.SECONDS.sleep(10);
			silo = client.getRequestStatus(token, requestId);
			LOG.info("state is: " + silo.getState());
		}

		LOG.info("state is: "
				+ client.getRequestStatus(token, requestId).getState());
	}

}
