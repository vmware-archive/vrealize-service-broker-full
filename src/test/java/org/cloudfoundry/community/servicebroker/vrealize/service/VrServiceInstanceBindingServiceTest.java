package org.cloudfoundry.community.servicebroker.vrealize.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.vrealize.Application;
import org.cloudfoundry.community.servicebroker.vrealize.service.VrServiceInstanceBindingService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
@Ignore
public class VrServiceInstanceBindingServiceTest {

	@Autowired
	VrServiceInstanceBindingService vrServiceInstanceBindingService;

	@Test
	public void testCreateBinding() throws ServiceBrokerException,
			ServiceInstanceBindingExistsException {

		CreateServiceInstanceBindingRequest req = new CreateServiceInstanceBindingRequest();

		ServiceInstanceBinding b = vrServiceInstanceBindingService
				.createServiceInstanceBinding(req);
		assertNotNull(b);
		assertEquals("app123", b.getAppGuid());
		Map<String, Object> m = b.getCredentials();
		assertNotNull(m);
		assertEquals("http://localhost:8080/hello/en", m.get("uri"));
		assertNotNull(b.getId());
		assertEquals("hello", b.getServiceInstanceId());
	}

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

}
