package org.cloudfoundry.community.servicebroker.vrealize.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.cloudfoundry.community.servicebroker.vrealize.Application;
import org.cloudfoundry.community.servicebroker.vrealize.TestConfig;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.Gson;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
@Ignore
public class CatalogServiceTest {

	@Autowired
	TokenService tokenService;

	@Autowired
	CatalogService catalogService;

	@Autowired
	Gson gson;

	@Test
	public void testGetEntitledCatalog() throws ServiceBrokerException {
		Catalog catalog = catalogService.getCatalog();
		assertNotNull(catalog);
		assertTrue(catalog.getServiceDefinitions().size() > 0);
	}

	@Test
	public void testGetEntitledCatalogItem() throws ServiceBrokerException {
		assertNull(catalogService.getServiceDefinition(null));
		assertNull(catalogService.getServiceDefinition(""));
		assertNotNull(catalogService.getServiceDefinition(TestConfig.SD_ID));
	}
}
