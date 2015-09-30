package org.cloudfoundry.community.servicebroker.vrealize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceExistsException;
import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
public class VraClientTest {

	@Autowired
	private VraClient client;

	@Autowired
	VrServiceInstanceService vrServiceInstanceService;

	// @Test
	// public void testCreateBinding() throws ServiceBrokerException,
	// ServiceInstanceBindingExistsException {
	// ServiceInstanceBinding b = client
	// .createBinding(getCreateServiceInstanceBindingRequest());
	// assertNotNull(b);
	// assertEquals("app123", b.getAppGuid());
	// Map<String, Object> m = b.getCredentials();
	// assertNotNull(m);
	// assertEquals("http://localhost:8080/hello/en", m.get("uri"));
	// assertNotNull(b.getId());
	// assertEquals("hello", b.getServiceInstanceId());
	// }

	// @Test
	// public void testCreateInstance() throws ServiceBrokerException,
	// ServiceInstanceExistsException {
	// CreateServiceInstanceRequest req = getCreateServiceInstanceRequest();
	// ServiceInstance si = client.createInstance(req);
	// assertNotNull(si);
	// assertEquals("orgid", si.getOrganizationGuid());
	// assertEquals("english", si.getPlanId());
	// assertEquals("hello", si.getServiceDefinitionId());
	// assertEquals("abc", si.getServiceInstanceId());
	// assertEquals("spaceid", si.getSpaceGuid());
	// }

	// @Test
	// public void testDeleteBinding() throws Exception {
	//
	// CreateServiceInstanceBindingRequest c =
	// getCreateServiceInstanceBindingRequest();
	// ServiceInstance si = client.getInstance(c.getServiceInstanceId());
	//
	// DeleteServiceInstanceBindingRequest req = new
	// DeleteServiceInstanceBindingRequest(
	// c.getBindingId(), si, c.getServiceDefinitionId(), c.getPlanId());
	//
	// assertNotNull(client.deleteBinding(req));
	// }

	// @Test
	// public void testDeleteInstance() throws Exception {
	// DeleteServiceInstanceRequest req = new DeleteServiceInstanceRequest(
	// getCreateServiceInstanceRequest().getServiceInstanceId(),
	// "hello", "english");
	// ServiceInstance si = client.deleteInstance(req);
	// assertNotNull(si);
	// assertEquals("english", si.getPlanId());
	// assertEquals("hello", si.getServiceDefinitionId());
	// assertEquals("abc", si.getServiceInstanceId());
	// assertEquals("orgid", si.getOrganizationGuid());
	// assertEquals("spaceid", si.getSpaceGuid());
	// }

	// @Test
	// public void testGetInstance() throws Exception {
	// CreateServiceInstanceRequest req = getCreateServiceInstanceRequest();
	// req.withServiceInstanceId("xyz");
	// ServiceInstance si = client.createInstance(req);
	// assertNotNull(si);
	// assertNotNull(client.getInstance(si.getServiceInstanceId()));
	// }

	// @Test
	// public void testUpdateInstance() {
	// assertNull(client.updateInstance(null));
	// }
	//
	// private CreateServiceInstanceRequest getCreateServiceInstanceRequest() {
	// CreateServiceInstanceRequest req = new CreateServiceInstanceRequest();
	// req.setOrganizationGuid("orgid");
	// req.setPlanId("english");
	// req.setServiceDefinitionId("hello");
	// req.setSpaceGuid("spaceid");
	// req.withServiceInstanceId("abc");
	//
	// return req;
	// }

	// private CreateServiceInstanceBindingRequest
	// getCreateServiceInstanceBindingRequest() {
	// Creds credentials = new Creds();
	// credentials.Password = "secret";
	// credentials.Tenant = "mycompany";
	// credentials.Username = "tester";
	//
	// Map<String, Object> parameters = new HashMap<String,Object>();
	// parameters.put("credentials", credentials);
	//
	// CreateServiceInstanceBindingRequest req = new
	// CreateServiceInstanceBindingRequest("7c8275d6-1bd6-452a-97c4-d6c053e4baa4",
	// "7c8275d6-1bd6-452a-97c4-d6c053e4baa4", "myApp", parameters);
	// req.setAppGuid("myApp");
	// req.setPlanId("7c8275d6-1bd6-452a-97c4-d6c053e4baa4");
	// req.setServiceDefinitionId("7c8275d6-1bd6-452a-97c4-d6c053e4baa4");
	// // req.withBindingId("abc");
	// req.setParameters(parameters);
	// return req;
	// }

	@Test
	public void testGetAllCatalog() throws ServiceBrokerException {
		String token = client.getToken(getCredentials());
		// System.out.println(token);
		assertNotNull(token);
		Catalog c = client.getAllCatalogItems(token);
		assertNotNull(c);
		assertEquals(17, c.getServiceDefinitions().size());
	}

	@Test
	public void testGetEntitledCatalog() throws ServiceBrokerException {
		String token = client.getToken(getCredentials());
		// System.out.println(token);
		assertNotNull(token);
		Catalog c = client.getAllCatalogItems(token);
		assertNotNull(c);
	}

	@Test
	public void testGetToken() throws ServiceBrokerException {
		String s = client.getToken(getCredentials());
		System.out.println(s);
		assertNotNull(s);
	}

	@Ignore
	@Test
	public void testCheckToken() throws ServiceBrokerException {
		assertTrue(client.checkToken(client.getToken(getCredentials())));
		assertFalse(client.checkToken("foo"));
	}

	@Test
	@Ignore
	public void testGetServiceDefinition() throws ServiceBrokerException {
		Catalog catalog = client.getAllCatalogItems(client
				.getToken(getCredentials()));
		assertNotNull(catalog);
		ServiceDefinition sd = client.getServiceDefinition(catalog,
				"Amazon Machine");
		assertNotNull(sd);
	}

	@Test
	public void testCreateServiceInstance()
			throws ServiceInstanceExistsException, ServiceBrokerException {
		CreateServiceInstanceRequest request = new CreateServiceInstanceRequest();
		request.setOrganizationGuid("anOrg");
		request.setSpaceGuid("aSpace");
		request.setServiceDefinitionId("2ce37e80-e526-416a-bf68-a52176ced367");
		request.setPlanId("Infrastructure.CatalogItem.Machine.Cloud.AmazonEC2");

		ServiceInstance si = vrServiceInstanceService
				.createServiceInstance(request);
		assertNotNull(si);
		assertNotNull(si.getServiceInstanceId());
	}

	private Creds getCredentials() {
		return new Creds("vdude01@vra.lab", "P1v0t4l!", "LAB");
	}
}
