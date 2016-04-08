package org.cloudfoundry.community.servicebroker.vrealize;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.log4j.Logger;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.LastOperation;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.VrServiceInstance;
import org.cloudfoundry.community.servicebroker.vrealize.service.CatalogService;
import org.cloudfoundry.community.servicebroker.vrealize.service.TokenService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.GetLastServiceOperationResponse;
import org.springframework.cloud.servicebroker.model.OperationState;
import org.springframework.cloud.servicebroker.model.ServiceDefinition;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * These tests can be used to test the vRA API directly, outside of the Service Broker code.
 **/
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
public class LifecycleTest {

    private static final Logger LOG = Logger.getLogger(LifecycleTest.class);

    private static final String REQ_ID = "0c2ed34c-d8ef-4045-827a-a2555a8284e3";

    @Autowired
    private VraClient client;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private CatalogService catalogService;

    @Test
    @Ignore
    public void testCreate() throws Exception {
        LOG.info("get a token.");
        String token = tokenService.getToken();
        assertNotNull(token);

        LOG.info("get a service def.");
        ServiceDefinition sd = catalogService
                .getServiceDefinition(TestConfig.SD_ID);
        assertNotNull(sd);

        LOG.info("submitting create request.");
        VrServiceInstance instance = client.createInstance(
                TestConfig.getCreateServiceInstanceRequest(sd), sd);

        Object requestId = instance.getCreateRequestId();
        assertNotNull(requestId);

        OperationState status = instance.getServiceInstanceLastOperation().getState();
        assertNotNull(status);

        LOG.info("request status is: " + status + " requestId is: " + requestId);

        Object location = instance.getLocation();
        assertNotNull(location);

        LOG.info("location: " + location);

        LOG.info("wait for create request to complete....");

        GetLastServiceOperationResponse silo = client.getRequestStatus(token,
                requestId.toString());
        assertNotNull(silo);
        while (silo.getState().equals(OperationState.IN_PROGRESS)) {
            TimeUnit.SECONDS.sleep(10);
            silo = client.getRequestStatus(token, requestId.toString());
            LOG.info("state is: " + silo.getState() + ": "
                    + silo.getDescription());
        }

        LOG.info("state is: "
                + client.getRequestStatus(token, requestId.toString()).getState());
    }

    @Test
    @Ignore
    public void testGetStatus() throws ServiceBrokerException {
        VrServiceInstance instance = TestConfig.getServiceInstance();
        LastOperation lo = new LastOperation(OperationState.IN_PROGRESS, REQ_ID, false);
        instance.withLastOperation(lo);
        GetLastServiceOperationResponse status = client.getRequestStatus(instance);
        assertNotNull(status);
        assertEquals(OperationState.SUCCEEDED, status.getState());
    }

    @Test
    @Ignore
    public void testDelete() throws Exception {
        LOG.info("get a token.");
        String token = tokenService.getToken();
        assertNotNull(token);

        VrServiceInstance instance = getServiceInstanceToDelete();
        instance = client.deleteInstance(instance);

        String requestId = instance.getCurrentOperationRequestId();
        assertNotNull(requestId);
        LOG.info("delete request id: " + requestId);

        OperationState status = instance.getServiceInstanceLastOperation().getState();
        assertNotNull(status);
        LOG.info("delete request state: " + status);

        LOG.info("wait for delete request to complete....");

        GetLastServiceOperationResponse silo = client.getRequestStatus(token,
                requestId);
        assertNotNull(silo);
        while (silo.getState().equals(OperationState.IN_PROGRESS)) {
            TimeUnit.SECONDS.sleep(10);
            silo = client.getRequestStatus(token, requestId);
            LOG.info("state is: " + silo.getState() + ": "
                    + silo.getDescription());
        }

        LOG.info("state is: "
                + client.getRequestStatus(token, requestId).getState());
    }

    private VrServiceInstance getServiceInstanceToDelete() throws Exception {
        ServiceDefinition sd = catalogService
                .getServiceDefinition(TestConfig.SD_ID);

        VrServiceInstance instance = new VrServiceInstance(TestConfig
                .getCreateServiceInstanceRequest(sd));

        instance.getMetadata().put(VrServiceInstance.CREATE_REQUEST_ID, REQ_ID);
        instance.getMetadata().put(VrServiceInstance.LOCATION, "foo" + REQ_ID);

        JsonParser parser = new JsonParser();
        JsonElement je = parser.parse(TestConfig
                .getContents("requestResponse.json"));

        instance.getParameters().putAll(
                client.getParametersFromCreateResponse(je));

        client.loadDataFromResourceResponse(tokenService.getToken(), instance);

        return instance;
    }

}