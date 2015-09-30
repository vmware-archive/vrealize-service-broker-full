package org.cloudfoundry.community.servicebroker.vrealize;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceExistsException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceUpdateNotSupportedException;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.DeleteServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.UpdateServiceInstanceRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
public class VrServiceInstanceServiceTest {

	private static final String SERVICE_DEF_ID = "2ce37e80-e526-416a-bf68-a52176ced367";
	private static final String PLAN_ID = "Infrastructure.CatalogItem.Machine.Cloud.AmazonEC2";

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
	public void testCreateAndGetAndDeleteServiceInstance()
			throws ServiceInstanceExistsException, ServiceBrokerException {

		// create
		CreateServiceInstanceRequest request = new CreateServiceInstanceRequest();
		request.setOrganizationGuid("anOrg");
		request.setSpaceGuid("aSpace");
		request.setServiceDefinitionId(SERVICE_DEF_ID);
		request.setPlanId(PLAN_ID);

		ServiceInstance si = vrServiceInstanceService
				.createServiceInstance(request);
		assertNotNull(si);
		assertNotNull(si.getServiceInstanceId());

		// get
		ServiceInstance si2 = vrServiceInstanceService.getServiceInstance(si
				.getServiceInstanceId());
		assertNotNull(si2);

		// delete
		DeleteServiceInstanceRequest req = new DeleteServiceInstanceRequest(
				si.getServiceInstanceId(), SERVICE_DEF_ID, PLAN_ID);
		ServiceInstance si3 = vrServiceInstanceService
				.deleteServiceInstance(req);
		assertNotNull(si3);

		assertNull(vrServiceInstanceService.getServiceInstance(si
				.getServiceInstanceId()));
	}

	@Test(expected = ServiceInstanceUpdateNotSupportedException.class)
	public void testUpdate() throws Exception {
		UpdateServiceInstanceRequest request = new UpdateServiceInstanceRequest();
		vrServiceInstanceService.updateServiceInstance(request);
	}
}
