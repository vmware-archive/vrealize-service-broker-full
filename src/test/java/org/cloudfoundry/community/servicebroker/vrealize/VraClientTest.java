package org.cloudfoundry.community.servicebroker.vrealize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceLastOperation;
import org.cloudfoundry.community.servicebroker.vrealize.service.TokenService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
	Catalog catalog;

	private static final String SD_ID = "e06ff060-dc7a-4f46-a7a7-c32c031fa31e";
	private static final String SI_ID = "5c09a0f6-a19f-4ce9-904a-8f3bf8242ddc";

	@Test
	public void testGetEntitledCatalog() throws ServiceBrokerException {
		assertNotNull(catalog);
		assertTrue(catalog.getServiceDefinitions().size() > 0);
	}

	@Test
	public void testGetEntitledCatalogItem() throws ServiceBrokerException {
		assertNull(client.getEntitledCatalogItem(null));
		assertNull(client.getEntitledCatalogItem(""));
		assertNotNull(client.getEntitledCatalogItem(SD_ID));
	}

	@Test
	public void testGetRequestTemplate() throws ServiceBrokerException {
		ServiceDefinition sd = client.getEntitledCatalogItem(SD_ID);
		assertNotNull(sd);

		String token = tokenService.getToken();
		assertNotNull(token);

		JsonElement template = client.getRequestTemplate(token, sd);
		assertNotNull(template);
	}

	@Test
	public void testPrepareRequest() throws ServiceBrokerException {
		JsonParser parser = new JsonParser();
		JsonObject o = (JsonObject) parser
				.parse(getContents("requestTemplate.json"));
		String s = client.prepareRequest(o).toString();
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
		assertEquals("5c09a0f6-a19f-4ce9-904a-8f3bf8242ddc", s);
	}

	@Test
	public void testGetRequestStatus() throws ServiceBrokerException {
		CreateServiceInstanceRequest req = new CreateServiceInstanceRequest();
		String token = tokenService.getToken();

		ServiceInstance si = new ServiceInstance(req);
		ServiceInstanceLastOperation silo = client.getRequestStatus(token, si);
		assertNotNull(silo);
		assertEquals("failed", silo.getState());

		req.withServiceInstanceId(SI_ID);
		si = new ServiceInstance(req);
		silo = client.getRequestStatus(token, si);
		assertNotNull(silo);
		assertEquals("succeeded", silo.getState());
	}
}
