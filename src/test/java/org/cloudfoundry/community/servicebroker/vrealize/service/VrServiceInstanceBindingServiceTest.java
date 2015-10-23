package org.cloudfoundry.community.servicebroker.vrealize.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.cloudfoundry.community.servicebroker.model.OperationState;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceLastOperation;
import org.cloudfoundry.community.servicebroker.vrealize.Application;
import org.cloudfoundry.community.servicebroker.vrealize.TestConfig;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.ServiceInstanceBindingRepository;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.VrServiceInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
public class VrServiceInstanceBindingServiceTest {

	@Autowired
	@InjectMocks
	VrServiceInstanceBindingService vrServiceInstanceBindingService;

	@Mock
	VrServiceInstanceService vrServiceInstanceService;

	@Autowired
	ServiceInstanceBindingRepository repo;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		VrServiceInstance si = TestConfig.getServiceInstance();
		ServiceInstanceLastOperation silo = new ServiceInstanceLastOperation(
				"anOp", OperationState.SUCCEEDED);
		si.withLastOperation(silo);

		when(vrServiceInstanceService.getServiceInstance(Matchers.anyString()))
				.thenReturn(si);

		when(
				vrServiceInstanceService
						.saveInstance(any(VrServiceInstance.class)))
				.thenReturn(si);

		when(
				vrServiceInstanceService
						.deleteInstance(any(VrServiceInstance.class)))
				.thenReturn(si);

		repo.deleteAll();
	}

	@After
	public void cleanUp() throws Exception {
		repo.deleteAll();
	}

	@Test
	public void testBinding() throws ServiceBrokerException,
			ServiceInstanceBindingExistsException {

		ServiceInstanceBinding b = TestConfig.getServiceInstanceBinding();
		assertNotNull(b);
		assertEquals("anAppId", b.getAppGuid());
		Map<String, Object> m = b.getCredentials();
		assertNotNull(m);
		assertEquals("mysql://aUser:secret@aHost:1234/aDB",
				m.get(VrServiceInstance.URI));
		assertNotNull(b.getId());
		assertEquals("anID", b.getServiceInstanceId());
	}

	@Test
	public void testCreateAndDeleteBinding() throws ServiceBrokerException,
			ServiceInstanceBindingExistsException {

		ServiceInstanceBinding b = vrServiceInstanceBindingService
				.createServiceInstanceBinding(TestConfig
						.getCreateBindingRequest());
		assertNotNull(b);
		Map<String, Object> m = b.getCredentials();
		assertNotNull(m);
		assertEquals("mysql://aUser:secret@aHost:1234/aDB",
				m.get(VrServiceInstance.URI));
		String id = b.getId();
		assertNotNull(id);

		b = vrServiceInstanceBindingService
				.deleteServiceInstanceBinding(TestConfig
						.getDeleteBindingRequest());
		assertNotNull(b);
		assertEquals(id, b.getId());
	}
}
