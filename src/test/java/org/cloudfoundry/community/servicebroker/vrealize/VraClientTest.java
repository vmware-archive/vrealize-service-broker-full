package org.cloudfoundry.community.servicebroker.vrealize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
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
import com.google.gson.JsonParser;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
public class VraClientTest {

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
	public void testGetAndPrepRequestTemplate() throws ServiceBrokerException {
		ServiceDefinition sd = catalogService
				.getServiceDefinition(TestConfig.SD_ID);
		assertNotNull(sd);

		String token = tokenService.getToken();
		assertNotNull(token);

		JsonElement template = client.getCreateRequestTemplate(token, sd);
		assertNotNull(template);
		JsonObject jo = client.prepareCreateRequestTemplate(template, TestConfig.R_ID);
		assertNotNull(jo);
	}

	@Test
	public void testGetRequestId() throws Exception {
		JsonParser parser = new JsonParser();
		JsonElement o = (JsonElement) parser.parse(TestConfig
				.getContents("requestResponse.json"));
		assertNotNull(o);
		String s = client.getRequestId(o);
		assertNotNull(s);
		assertEquals(TestConfig.R_ID, s);
	}

	@Test
	public void testGetRequestStatus() throws ServiceBrokerException {
		CreateServiceInstanceRequest req = new CreateServiceInstanceRequest();
		req.withServiceInstanceId(TestConfig.R_ID);
		String token = tokenService.getToken();

		ServiceInstance si = new ServiceInstance(req);
		ServiceInstanceLastOperation silo = client.getRequestStatus(token, si);
		assertNotNull(silo);
		assertEquals("succeeded", silo.getState());
	}

	@Test
	public void testGetParameters() throws Exception {
		String json = TestConfig.getContents("requestResponse.json");
		JsonParser parser = new JsonParser();
		JsonElement je = parser.parse(json);

		Map<String, Object> m = client.getParameters(je);
		assertNotNull(m);
		assertEquals(6, m.size());
		assertEquals("3306", m.get(Constants.PORT));
		assertEquals("P1v0t4l!", m.get(Constants.PASSWORD));
		assertEquals("mysqluser", m.get(Constants.USER_ID));
		assertEquals("db01", m.get(Constants.DB_ID));
		// assertEquals("foo", m.get("HOST"));
		assertEquals("mysql", m.get(Constants.SERVICE_TYPE));
	}

	@Test
	public void testGetLastOperation() throws Exception {
		JsonParser parser = new JsonParser();
		JsonElement je = parser.parse(TestConfig
				.getContents("submittedRequestResponse.json"));

		ServiceInstanceLastOperation silo = client.getLastOperation(je);
		assertNotNull(silo);
		assertEquals("in progress", silo.getState());

		je = parser.parse(TestConfig
				.getContents("inProgressRequestResponse.json"));
		silo = client.getLastOperation(je);
		assertNotNull(silo);
		assertEquals("in progress", silo.getState());

		je = parser.parse(TestConfig
				.getContents("pendingPreRequestResponse.json"));
		silo = client.getLastOperation(je);
		assertNotNull(silo);
		assertEquals("in progress", silo.getState());

		je = parser.parse(TestConfig
				.getContents("pendingPostRequestResponse.json"));
		silo = client.getLastOperation(je);
		assertNotNull(silo);
		assertEquals("in progress", silo.getState());

		je = parser.parse(TestConfig
				.getContents("successfulPostRequestResponse.json"));
		silo = client.getLastOperation(je);
		assertNotNull(silo);
		assertEquals("succeeded", silo.getState());

		je = parser.parse(TestConfig
				.getContents("failedPostRequestResponse.json"));
		silo = client.getLastOperation(je);
		assertNotNull(silo);
		assertEquals("failed", silo.getState());

		je = parser.parse(TestConfig
				.getContents("rejectedRequestResponse.json"));
		silo = client.getLastOperation(je);
		assertNotNull(silo);
		assertEquals("failed", silo.getState());

		je = parser.parse(TestConfig
				.getContents("bogusStateRequestResponse.json"));
		silo = client.getLastOperation(je);
		assertNotNull(silo);
		assertEquals("failed", silo.getState());

		je = parser.parse(TestConfig
				.getContents("missingStateRequestResponse.json"));
		silo = client.getLastOperation(je);
		assertNotNull(silo);
		assertEquals("failed", silo.getState());

	}

	@Test
	public void testGetAndPrepDeleteRequestTemplate() throws ServiceBrokerException {
		String token = tokenService.getToken();
		JsonElement je = client.getDeleteRequestTemplate(token, TestConfig.R_ID);
		assertNotNull(je);
		JsonObject jo = client.prepareDeleteRequestTemplate(je, TestConfig.R_ID);
		assertNotNull(jo);
	}
	
	@Test
	public void testDelete() throws ServiceBrokerException {
		String token = tokenService.getToken();
		JsonElement je = client.getDeleteRequestTemplate(token, TestConfig.R_ID);
		assertNotNull(je);
//		JsonObject jo = client.prepareDeleteRequestTemplate(je, TestConfig.R_ID);
//		assertNotNull(jo);
		client.postDeleteRequest(token, je.getAsJsonObject(), TestConfig.R_ID);
	}

	@Test
	public void testGetLinks() throws Exception {
		JsonParser parser = new JsonParser();
		JsonElement je = parser.parse(TestConfig
				.getContents("requestResources.json"));
		Map<String, String> m = client.getDeleteLinks(je);
		assertNotNull(m);
		assertEquals(2, m.size());
		assertEquals(
				"https://vra.vra.lab/catalog-service/api/consumer/resources/d591e58d-b2cf-4061-aec1-7f41168b7a6d/actions/fe9af618-f21d-47a2-bebc-62d5914f6e6c/requests/template",
				m.get(Constants.DELETE_TEMPLATE_LINK));
		assertEquals(
				"https://vra.vra.lab/catalog-service/api/consumer/resources/d591e58d-b2cf-4061-aec1-7f41168b7a6d/actions/fe9af618-f21d-47a2-bebc-62d5914f6e6c/requests",
				m.get(Constants.DELETE_LINK));
	}
}
