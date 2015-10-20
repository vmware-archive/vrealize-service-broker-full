package org.cloudfoundry.community.servicebroker.vrealize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.OperationState;
import org.cloudfoundry.community.servicebroker.vrealize.domain.VrServiceInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
public class VrServiceInstanceTest {

	@Test
	public void testStates() {
		CreateServiceInstanceRequest req = new CreateServiceInstanceRequest();

		// new create request
		VrServiceInstance si = VrServiceInstance.create(req, "12345");

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
		assertEquals(
				"23456",
				si.getMetadata().get(
						VrServiceInstance.MetatdataKeys.DELETE_REQUEST_ID));
		assertEquals(
				"12345",
				si.getMetadata().get(
						VrServiceInstance.MetatdataKeys.CREATE_REQUEST_ID));

		// delete failed
		si = VrServiceInstance.update(si, OperationState.FAILED);

		assertFalse(si.isCurrentOperationCreate());
		assertTrue(si.isCurrentOperationDelete());
		assertFalse(si.isCurrentOperationSuccessful());
		assertFalse(si.isInProgress());
		assertEquals("23456", si.getServiceInstanceLastOperation()
				.getDescription());
		assertEquals(
				"23456",
				si.getMetadata().get(
						VrServiceInstance.MetatdataKeys.DELETE_REQUEST_ID));
		assertEquals(
				"12345",
				si.getMetadata().get(
						VrServiceInstance.MetatdataKeys.CREATE_REQUEST_ID));

		// delete succeeded
		si = VrServiceInstance.update(si, OperationState.SUCCEEDED);

		assertFalse(si.isCurrentOperationCreate());
		assertTrue(si.isCurrentOperationDelete());
		assertTrue(si.isCurrentOperationSuccessful());
		assertFalse(si.isInProgress());
		assertEquals("23456", si.getServiceInstanceLastOperation()
				.getDescription());
		assertEquals(
				"23456",
				si.getMetadata().get(
						VrServiceInstance.MetatdataKeys.DELETE_REQUEST_ID));
		assertEquals(
				"12345",
				si.getMetadata().get(
						VrServiceInstance.MetatdataKeys.CREATE_REQUEST_ID));
	}

}
