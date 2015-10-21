package org.cloudfoundry.community.servicebroker.vrealize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
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
import org.springframework.core.io.ClassPathResource;
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

	private static final String SD_ID = "e06ff060-dc7a-4f46-a7a7-c32c031fa31e";
	private static final String R_ID = "5c09a0f6-a19f-4ce9-904a-8f3bf8242ddc";

	// private static final String P_ID =
	// "e06ff060-dc7a-4f46-a7a7-c32c031fa31e";

	@Test
	public void testGetRequestTemplate() throws ServiceBrokerException {
		ServiceDefinition sd = catalogService.getServiceDefinition(SD_ID);
		assertNotNull(sd);

		String token = tokenService.getToken();
		assertNotNull(token);

		JsonElement template = client.getCreateRequestTemplate(token, sd);
		assertNotNull(template);
	}

	@Test
	public void testPrepareRequest() throws ServiceBrokerException {
		JsonParser parser = new JsonParser();
		JsonObject o = (JsonObject) parser
				.parse(getContents("requestTemplate.json"));
		String s = client.prepareCreateRequestTemplate(o, "abc123").toString();
		assertEquals(getContents("filteredRequestTemplate.json"), s);
	}

	private String getContents(String fileName) throws ServiceBrokerException {
		try {
			URI u = new ClassPathResource(fileName).getURI();
			return new String(Files.readAllBytes(Paths.get(u)));
		} catch (IOException e) {
			throw new ServiceBrokerException("error reading template.", e);
		}
	}

	@Test
	public void testGetRequestId() throws Exception {
		JsonParser parser = new JsonParser();
		JsonElement o = (JsonElement) parser
				.parse(getContents("requestResponse.json"));
		assertNotNull(o);
		String s = client.getRequestId(o);
		assertNotNull(s);
		assertEquals(R_ID, s);
	}

	@Test
	public void testGetRequestStatus() throws ServiceBrokerException {
		CreateServiceInstanceRequest req = new CreateServiceInstanceRequest();
		String token = tokenService.getToken();

		VrServiceInstance si = VrServiceInstance.create(req, R_ID);
		ServiceInstanceLastOperation silo = client.getRequestStatus(token, si);
		assertNotNull(silo);
		assertEquals("succeeded", silo.getState());
	}

	@Test
	public void testGetParameters() throws Exception {
		String json = getContents("requestResponse.json");
		JsonParser parser = new JsonParser();
		JsonElement je = parser.parse(json);

		Map<Enum<VrServiceInstance.ParameterKeys>, Object> m = client
				.getParameters(je);
		assertNotNull(m);
		assertEquals(5, m.size());
		assertEquals("3306", m.get(VrServiceInstance.ParameterKeys.PORT));
		assertEquals("P1v0t4l!",
				m.get(VrServiceInstance.ParameterKeys.PASSWORD));
		assertEquals("mysqluser",
				m.get(VrServiceInstance.ParameterKeys.USER_ID));
		assertEquals("db01", m.get(VrServiceInstance.ParameterKeys.DB_ID));
		// assertEquals("foo", m.get("host"));
	}

	@Test
	public void testGetLastOperation() throws ServiceBrokerException {
		JsonParser parser = new JsonParser();
		JsonElement je = parser
				.parse(getContents("submittedRequestResponse.json"));

		ServiceInstanceLastOperation silo = client.getLastOperation(je);
		assertNotNull(silo);
		assertEquals("in progress", silo.getState());

		je = parser.parse(getContents("inProgressRequestResponse.json"));
		silo = client.getLastOperation(je);
		assertNotNull(silo);
		assertEquals("in progress", silo.getState());

		je = parser.parse(getContents("pendingPreRequestResponse.json"));
		silo = client.getLastOperation(je);
		assertNotNull(silo);
		assertEquals("in progress", silo.getState());

		je = parser.parse(getContents("pendingPostRequestResponse.json"));
		silo = client.getLastOperation(je);
		assertNotNull(silo);
		assertEquals("in progress", silo.getState());

		je = parser.parse(getContents("successfulPostRequestResponse.json"));
		silo = client.getLastOperation(je);
		assertNotNull(silo);
		assertEquals("succeeded", silo.getState());

		je = parser.parse(getContents("failedPostRequestResponse.json"));
		silo = client.getLastOperation(je);
		assertNotNull(silo);
		assertEquals("failed", silo.getState());

		je = parser.parse(getContents("rejectedRequestResponse.json"));
		silo = client.getLastOperation(je);
		assertNotNull(silo);
		assertEquals("failed", silo.getState());

		je = parser.parse(getContents("bogusStateRequestResponse.json"));
		silo = client.getLastOperation(je);
		assertNotNull(silo);
		assertEquals("failed", silo.getState());

		je = parser.parse(getContents("missingStateRequestResponse.json"));
		silo = client.getLastOperation(je);
		assertNotNull(silo);
		assertEquals("failed", silo.getState());

	}

	@Test
	@Ignore
	public void testGetDeleteRequestTemplate() throws ServiceBrokerException {
		CreateServiceInstanceRequest req = new CreateServiceInstanceRequest();
		VrServiceInstance si = VrServiceInstance.create(req,
				"9ca10dee-730e-486a-9138-d8aade4913e2");
		si = VrServiceInstance.delete(si,
				"9ca10dee-730e-486a-9138-d8aade4913e2");

		String token = tokenService.getToken();
		client.loadMetadata(token, si);

		JsonElement je = client.getDeleteRequestTemplate(token, si);
		assertNotNull(je);
	}

	@Test
	public void testGetLinks() throws Exception {
		JsonParser parser = new JsonParser();
		JsonElement je = parser.parse(getContents("requestResources.json"));
		Map<Enum<VrServiceInstance.MetatdataKeys>, String> m = client
				.getDeleteLinks(je);
		assertNotNull(m);
		assertEquals(2, m.size());
		assertEquals(
				"https://vra.vra.lab/catalog-service/api/consumer/resources/d591e58d-b2cf-4061-aec1-7f41168b7a6d/actions/fe9af618-f21d-47a2-bebc-62d5914f6e6c/requests/template",
				m.get(VrServiceInstance.MetatdataKeys.DELETE_TEMPLATE_LINK));
		assertEquals(
				"https://vra.vra.lab/catalog-service/api/consumer/resources/d591e58d-b2cf-4061-aec1-7f41168b7a6d/actions/fe9af618-f21d-47a2-bebc-62d5914f6e6c/requests",
				m.get(VrServiceInstance.MetatdataKeys.DELETE_LINK));
	}

	@Test
	@Ignore
	public void testDelete() throws Exception {
		String token = tokenService.getToken();
		JsonElement deleteTemplate = repo
				.getRequest(
						"Bearer " + token,
						"catalog-service/api/consumer/resources/df13aba9-278b-4fb9-beec-1e14f29a2337/actions/fe9af618-f21d-47a2-bebc-62d5914f6e6c/requests/template");
		JsonElement edited = client.prepareDeleteRequestTemplate(
				deleteTemplate, "12345");
		JsonElement results = repo
				.postRequest(
						"Bearer " + token,
						"catalog-service/api/consumer/resources/df13aba9-278b-4fb9-beec-1e14f29a2337/actions/fe9af618-f21d-47a2-bebc-62d5914f6e6c/requests",
						edited);
		System.out.println(results);
	}
}
