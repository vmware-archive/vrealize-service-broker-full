package org.cloudfoundry.community.servicebroker.vrealize;

import org.apache.log4j.Logger;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.DeleteServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
@Ignore
public class ServiceInstanceServiceTest {

	private static final Logger LOG = Logger
			.getLogger(ServiceInstanceServiceTest.class);

	@Autowired
	ServiceInstanceService service;

	private static final String SD_ID = "e06ff060-dc7a-4f46-a7a7-c32c031fa31e";
	private static final String P_ID = "e06ff060-dc7a-4f46-a7a7-c32c031fa31e";

	@Test
	public void testLifecycle() throws Exception {
		CreateServiceInstanceRequest creq = new CreateServiceInstanceRequest(
				SD_ID, P_ID, "orgId", "spaceId", true, null);
		creq.withServiceInstanceId("12345");

		ServiceInstance instance = service.createServiceInstance(creq);
		String state = instance.getServiceInstanceLastOperation().getState();
		LOG.info("request status: " + state);

		while (state.equals("in progress")) {
			service.getServiceInstance(instance.getServiceInstanceId());
			state = instance.getServiceInstanceLastOperation().getState();
			LOG.info("request status: " + state);
			Thread.sleep(10000);
		}

		DeleteServiceInstanceRequest dreq = new DeleteServiceInstanceRequest(
				instance.getServiceInstanceId(), SD_ID, P_ID, true);

		instance = service.deleteServiceInstance(dreq);
		state = instance.getServiceInstanceLastOperation().getState();
		LOG.info("request status: " + state);
		while (state.equals("in progress")) {
			service.getServiceInstance(instance.getServiceInstanceId());
			state = instance.getServiceInstanceLastOperation().getState();
			LOG.info("request status: " + state);
			Thread.sleep(10000);
		}
	}
}
