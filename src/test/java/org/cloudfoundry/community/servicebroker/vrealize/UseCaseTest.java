package org.cloudfoundry.community.servicebroker.vrealize;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.vrealize.service.CatalogService;
import org.cloudfoundry.community.servicebroker.vrealize.service.TokenService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
@Ignore
public class UseCaseTest {

	private static final String SD_ID = "e06ff060-dc7a-4f46-a7a7-c32c031fa31e";

	@Autowired
	private VraClient client;

	@Autowired
	TokenService tokenService;

	@Autowired
	CatalogService catalogService;

	// @Autowired
	// ServiceInstanceService serviceInstanceService;

	@Test
	public void testUseCase() throws ServiceBrokerException {
		// get a token
		String token = tokenService.getToken();
		assertNotNull(token);

		// get the catalog
		Catalog catalog = catalogService.getCatalog();
		assertNotNull(catalog);
		assertTrue(catalog.getServiceDefinitions().size() > 0);

		// as for info on an item in the catalog
		ServiceDefinition sd = catalogService.getServiceDefinition(SD_ID);
		assertNotNull(sd);

		// ask for a request template for an item in catalog
		JsonElement template = client.getCreateRequestTemplate(token, sd);
		assertNotNull(template);

		// edit the request
		JsonObject jo = client.prepareCreateRequestTemplate(template, "12345");
		assertNotNull(jo);

		// submit the request
		JsonElement je = client.postCreateRequest(token, jo, sd);
		assertNotNull(je);

		System.out.println(je.toString());

		// poll for response

		// ask to bind to the service

		// unbind from the service

		// delete the service
		// JsonElement deleteRequest = client.getDeleteRequestTemplate(token,
		// si);
		// fail("not completely implemented yet.");
	}
}
