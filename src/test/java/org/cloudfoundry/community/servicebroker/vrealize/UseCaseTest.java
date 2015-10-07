package org.cloudfoundry.community.servicebroker.vrealize;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.vrealize.service.TokenService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.JsonElement;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
public class UseCaseTest {

	private static final String SD_ID = "e06ff060-dc7a-4f46-a7a7-c32c031fa31e";

	@Autowired
	private VraClient client;

	@Autowired
	TokenService tokenService;

	@Autowired
	Catalog catalog;

	@Test
	public void testUseCase() throws ServiceBrokerException {
		// get a token
		String token = tokenService.getToken();
		assertNotNull(token);

		// get the catalog
		assertNotNull(catalog);
		assertTrue(catalog.getServiceDefinitions().size() > 0);

		// as for info on an item in the catalog
		ServiceDefinition sd = client.getEntitledCatalogItem(SD_ID);
		assertNotNull(sd);

		// ask for a request template for an item in catalog
		JsonElement template = client.getRequestTemplate(token, sd);
		assertNotNull(template);

		// submit the request

		// poll for response

		// ask to bind to the service

		// unbind from the service

		// delete the service
		//fail("not completely implemented yet.");
	}
}
