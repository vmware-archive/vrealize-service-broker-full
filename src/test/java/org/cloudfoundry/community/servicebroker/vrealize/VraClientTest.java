package org.cloudfoundry.community.servicebroker.vrealize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceLastOperation;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.VrServiceInstance;
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
	public void testGetRequestTemplate() throws ServiceBrokerException {
		ServiceDefinition sd = catalogService
				.getServiceDefinition(TestConfig.SD_ID);
		assertNotNull(sd);

		String token = tokenService.getToken();
		assertNotNull(token);

		JsonElement template = client.getCreateRequestTemplate(token, sd);
		assertNotNull(template);
	}

	@Test
	public void testPrepareRequest() throws Exception {
		JsonParser parser = new JsonParser();
		JsonObject o = (JsonObject) parser.parse(TestConfig
				.getContents("requestTemplate.json"));
		String s = client.prepareCreateRequestTemplate(o, "abc123").toString();
		assertEquals(TestConfig.getContents("filteredRequestTemplate.json"), s);
	}

	@Test
	public void testGetRequestStatus() throws Exception {
		JsonParser parser = new JsonParser();
		JsonElement je = parser.parse(TestConfig
				.getContents("requestResponse.json"));

		ServiceInstanceLastOperation silo = client.getLastOperation(je);
		assertNotNull(silo);
		assertEquals("in progress", silo.getState());
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
	public void testGetParmsAndMeta() throws Exception {
		JsonParser parser = new JsonParser();
		JsonElement cr = parser.parse(TestConfig
				.getContents("requestResponse.json"));

		JsonElement rvr = parser.parse(TestConfig
				.getContents("resourceViewResponse.json"));

		Map<String, Object> parms1 = client.getParametersFromCreateResponse(cr);
		Map<String, Object> parms2 = client
				.getParametersFromResourceResponse(rvr);
		Map<String, String> meta = client.getDeleteLinks(rvr);

		assertNotNull(parms1);
		assertEquals(6, parms1.size());
		assertEquals("3306", parms1.get(VrServiceInstance.PORT));
		assertEquals("P1v0t4l!", parms1.get(VrServiceInstance.PASSWORD));
		assertEquals("mysqluser", parms1.get(VrServiceInstance.USER_ID));
		assertEquals("db01", parms1.get(VrServiceInstance.DB_ID));
		assertEquals("mysql", parms1.get(VrServiceInstance.SERVICE_TYPE));

		assertNotNull(parms2);
		assertEquals(1, parms2.size());
		assertEquals("192.168.201.17", parms2.get(VrServiceInstance.HOST));

		assertNotNull(meta);
		assertEquals(2, meta.size());
		assertEquals(
				"https://vra.vra.lab/catalog-service/api/consumer/resources/06852d93-466d-4d73-80bc-78764b3d768a/actions/051a18db-6bf5-4468-97e0-942330528c92/requests/template",
				meta.get(VrServiceInstance.DELETE_TEMPLATE_LINK));
		assertEquals(
				"https://vra.vra.lab/catalog-service/api/consumer/resources/06852d93-466d-4d73-80bc-78764b3d768a/actions/051a18db-6bf5-4468-97e0-942330528c92/requests",
				meta.get(VrServiceInstance.DELETE_LINK));
	}

	@Test
	public void testLoadCredentials() throws ServiceBrokerException {
		VrServiceInstance instance = VrServiceInstance.create(TestConfig
				.getCreateServiceInstanceRequest());
		assertNotNull(instance);
		instance.getMetadata()
				.put(VrServiceInstance.LOCATION,
						"https://vra.vra.lab/catalog-service/api/consumer/requests/8720ac04-9910-4426-b8b3-758f6e02e3bc");
		instance.getMetadata().put(VrServiceInstance.CREATE_REQUEST_ID,
				"8720ac04-9910-4426-b8b3-758f6e02e3bc");

		assertFalse(instance.hasCredentials());
		client.loadCredentials(instance);
		assertTrue(instance.hasCredentials());
		assertNotNull(instance.getCredentials());
		assertEquals("mysql://mysqluser:P1v0t4l!@192.168.201.17:3306/db01",
				instance.getCredentials().get(VrServiceInstance.URI));
	}
}
