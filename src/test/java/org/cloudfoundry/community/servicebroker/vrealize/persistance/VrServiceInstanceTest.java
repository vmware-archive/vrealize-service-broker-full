package org.cloudfoundry.community.servicebroker.vrealize.persistance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.OperationState;
import org.cloudfoundry.community.servicebroker.vrealize.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.Gson;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
public class VrServiceInstanceTest {

	@Autowired
	Gson gson;

	@Test
	public void testStates() {
		VrServiceInstance si = VrServiceInstance.create(
				getServiceInstanceRequest(), "12345");

		assertTrue(si.isCurrentOperationCreate());
		assertFalse(si.isCurrentOperationDelete());
		assertFalse(si.isCurrentOperationSuccessful());
		assertTrue(si.isInProgress());
		assertEquals("12345", si.getServiceInstanceLastOperation()
				.getDescription());

		// failed create request
		VrServiceInstance.update(si, OperationState.FAILED);

		assertTrue(si.isCurrentOperationCreate());
		assertFalse(si.isCurrentOperationDelete());
		assertFalse(si.isCurrentOperationSuccessful());
		assertFalse(si.isInProgress());
		assertEquals("12345", si.getServiceInstanceLastOperation()
				.getDescription());

		// succeeded create request
		VrServiceInstance.update(si, OperationState.SUCCEEDED);

		assertTrue(si.isCurrentOperationCreate());
		assertFalse(si.isCurrentOperationDelete());
		assertTrue(si.isCurrentOperationSuccessful());
		assertFalse(si.isInProgress());
		assertEquals("12345", si.getServiceInstanceLastOperation()
				.getDescription());

		// new delete request added to existing si
		si = VrServiceInstance.delete(si, "23456");

		assertFalse(si.isCurrentOperationCreate());
		assertTrue(si.isCurrentOperationDelete());
		assertFalse(si.isCurrentOperationSuccessful());
		assertTrue(si.isInProgress());
		assertEquals("23456", si.getServiceInstanceLastOperation()
				.getDescription());
		assertEquals("23456",
				si.getMetadata().get(VrServiceInstance.DELETE_REQUEST_ID));
		assertEquals("12345",
				si.getMetadata().get(VrServiceInstance.CREATE_REQUEST_ID));

		// delete failed
		si = VrServiceInstance.update(si, OperationState.FAILED);

		assertFalse(si.isCurrentOperationCreate());
		assertTrue(si.isCurrentOperationDelete());
		assertFalse(si.isCurrentOperationSuccessful());
		assertFalse(si.isInProgress());
		assertEquals("23456", si.getServiceInstanceLastOperation()
				.getDescription());
		assertEquals("23456",
				si.getMetadata().get(VrServiceInstance.DELETE_REQUEST_ID));
		assertEquals("12345",
				si.getMetadata().get(VrServiceInstance.CREATE_REQUEST_ID));

		// delete succeeded
		si = VrServiceInstance.update(si, OperationState.SUCCEEDED);

		assertFalse(si.isCurrentOperationCreate());
		assertTrue(si.isCurrentOperationDelete());
		assertTrue(si.isCurrentOperationSuccessful());
		assertFalse(si.isInProgress());
		assertEquals("23456", si.getServiceInstanceLastOperation()
				.getDescription());
		assertEquals("23456",
				si.getMetadata().get(VrServiceInstance.DELETE_REQUEST_ID));
		assertEquals("12345",
				si.getMetadata().get(VrServiceInstance.CREATE_REQUEST_ID));
	}

	private void toJson(VrServiceInstance si) {
		System.out.println(gson.toJson(si));
	}

	private CreateServiceInstanceRequest getServiceInstanceRequest() {
		CreateServiceInstanceRequest req = new CreateServiceInstanceRequest(
				"sdId", "pId", "orgId", "spaceId", true, null);
		req.withServiceInstanceId("anID");
		return req;
	}

	@Test
	public void testToJson() {
		VrServiceInstance si = VrServiceInstance.create(
				getServiceInstanceRequest(), "12345");
		si.getParameters().put("foo", "bar");
		toJson(si);
	}
}
