package org.cloudfoundry.community.servicebroker.vrealize.service;

import org.cloudfoundry.community.servicebroker.vrealize.Application;
import org.cloudfoundry.community.servicebroker.vrealize.TestConfig;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.ServiceInstanceBindingRepository;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.VrServiceInstance;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.VrServiceInstanceBinding;
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
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.springframework.cloud.servicebroker.model.*;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
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
        GetLastServiceOperationResponse silo = new GetLastServiceOperationResponse().withDescription("anOp").
                withOperationState(OperationState.SUCCEEDED);
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

        VrServiceInstanceBinding b = TestConfig.getServiceInstanceBinding();
        assertNotNull(b);
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

        CreateServiceInstanceBindingRequest req = TestConfig
                .getCreateBindingRequest();

        CreateServiceInstanceBindingResponse b = vrServiceInstanceBindingService
                .createServiceInstanceBinding(req);
        assertNotNull(b);

        VrServiceInstance si = vrServiceInstanceService.getServiceInstance(req.getServiceInstanceId());
        assertNotNull(si);

        Map<String, Object> m = si.getCredentials();
        assertNotNull(m);
        assertEquals("mysql://aUser:secret@aHost:1234/aDB",
                m.get(VrServiceInstance.URI));
    }
}
