package org.cloudfoundry.community.servicebroker.vrealize;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
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
	public void testGetCatalog() throws ServiceBrokerException {
		String token = client.getToken(getCredentials());
		System.out.println(token);
		assertNotNull(token);
		Map<String, Object> s = client.getCatalog(token);
		assertNotNull(s);
		System.out.println(s);
	}

	@Ignore
	@Test
	public void testGetToken() throws ServiceBrokerException {
		String s = client.getToken(getCredentials());
		//System.out.println(s);
		assertNotNull(s);
	}
	
	@Ignore
	@Test
	public void testCheckToken() throws ServiceBrokerException {
		assertTrue(client.checkToken(client.getToken(getCredentials())));
		assertFalse(client.checkToken("foo"));
	}
	
	private Map<String, String> getCredentials() {
		Map<String, String> credentials = new HashMap<String, String>();
		credentials.put("username", "vdude1");
		credentials.put("tenant", "LAB");
		credentials.put("password", "P1v0t4l!");
		
		return credentials;
	}
}
