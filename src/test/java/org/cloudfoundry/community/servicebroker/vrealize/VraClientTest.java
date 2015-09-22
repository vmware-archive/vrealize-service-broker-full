package org.cloudfoundry.community.servicebroker.vrealize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Map;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceExistsException;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.DeleteServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.vrealize.Application;
import org.cloudfoundry.community.servicebroker.vrealize.VraClient;
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

	@Test
	public void testCreateBinding() throws ServiceBrokerException,
			ServiceInstanceBindingExistsException {
		ServiceInstanceBinding b = client
				.createBinding(getCreateServiceInstanceBindingRequest());
		assertNotNull(b);
		assertEquals("app123", b.getAppGuid());
		Map<String, Object> m = b.getCredentials();
		assertNotNull(m);
		assertEquals("http://localhost:8080/hello/en", m.get("uri"));
		assertNotNull(b.getId());
		assertEquals("hello", b.getServiceInstanceId());
	}

	@Test
	public void testCreateInstance() throws ServiceBrokerException,
			ServiceInstanceExistsException {
		CreateServiceInstanceRequest req = getCreateServiceInstanceRequest();
		ServiceInstance si = client.createInstance(req);
		assertNotNull(si);
		assertEquals("orgid", si.getOrganizationGuid());
		assertEquals("english", si.getPlanId());
		assertEquals("hello", si.getServiceDefinitionId());
		assertEquals("abc", si.getServiceInstanceId());
		assertEquals("spaceid", si.getSpaceGuid());
	}

	@Test
	public void testDeleteBinding() throws Exception {

		CreateServiceInstanceBindingRequest c = getCreateServiceInstanceBindingRequest();
		ServiceInstance si = client.getInstance(c.getServiceInstanceId());

		DeleteServiceInstanceBindingRequest req = new DeleteServiceInstanceBindingRequest(
				c.getBindingId(), si, c.getServiceDefinitionId(), c.getPlanId());

		assertNotNull(client.deleteBinding(req));
	}

	@Test
	public void testDeleteInstance() throws Exception {
		DeleteServiceInstanceRequest req = new DeleteServiceInstanceRequest(
				getCreateServiceInstanceRequest().getServiceInstanceId(),
				"hello", "english");
		ServiceInstance si = client.deleteInstance(req);
		assertNotNull(si);
		assertEquals("english", si.getPlanId());
		assertEquals("hello", si.getServiceDefinitionId());
		assertEquals("abc", si.getServiceInstanceId());
		assertEquals("orgid", si.getOrganizationGuid());
		assertEquals("spaceid", si.getSpaceGuid());
	}

	@Test
	public void testGetInstance() throws Exception {
		CreateServiceInstanceRequest req = getCreateServiceInstanceRequest();
		req.withServiceInstanceId("xyz");
		ServiceInstance si = client.createInstance(req);
		assertNotNull(si);
		assertNotNull(client.getInstance(si.getServiceInstanceId()));
	}

	@Test
	public void testUpdateInstance() {
		assertNull(client.updateInstance(null));
	}

	private CreateServiceInstanceRequest getCreateServiceInstanceRequest() {
		CreateServiceInstanceRequest req = new CreateServiceInstanceRequest();
		req.setOrganizationGuid("orgid");
		req.setPlanId("english");
		req.setServiceDefinitionId("hello");
		req.setSpaceGuid("spaceid");
		req.withServiceInstanceId("abc");

		return req;
	}

	private CreateServiceInstanceBindingRequest getCreateServiceInstanceBindingRequest() {
		CreateServiceInstanceBindingRequest req = new CreateServiceInstanceBindingRequest();
		req.setAppGuid("app123");
		req.setPlanId("english");
		req.setServiceDefinitionId("hello");
		req.withBindingId("abc");
		return req;
	}

}
