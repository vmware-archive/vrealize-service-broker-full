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
import org.cloudfoundry.community.servicebroker.vrealize.service.VrServiceInstanceService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
public class VrServiceInstanceServiceTest {

	private static final String SERVICE_DEF_ID = "e06ff060-dc7a-4f46-a7a7-c32c031fa31e";
	private static final String PLAN_ID = "e06ff060-dc7a-4f46-a7a7-c32c031fa31e";

	@Autowired
	VrServiceInstanceService vrServiceInstanceService;

	@Test
	public void testCreateAndGetAndDeleteServiceInstance()
			throws ServiceInstanceExistsException, ServiceBrokerException {

		// create
		CreateServiceInstanceRequest request = new CreateServiceInstanceRequest(
				SERVICE_DEF_ID, PLAN_ID, "anOrg", "aSpace", true, null);
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
				si.getServiceInstanceId(), SERVICE_DEF_ID, PLAN_ID, true);
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
