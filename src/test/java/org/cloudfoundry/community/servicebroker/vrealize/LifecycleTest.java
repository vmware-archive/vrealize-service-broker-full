package org.cloudfoundry.community.servicebroker.vrealize;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.log4j.Logger;
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

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
@Ignore
public class LifecycleTest {

    private static final Logger LOG = Logger.getLogger(LifecycleTest.class);

    private static final String R_ID = "e8ae8c36-056b-4a79-bd8c-b12646c66214";

    @Autowired
    private VraClient client;

    @Autowired
    TokenService tokenService;

    @Autowired
    CatalogService catalogService;

    @Test
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
    public void testGetStatus() throws ServiceBrokerException {
        VrServiceInstance instance = TestConfig.getServiceInstance();
        GetLastServiceOperationResponse silo = new GetLastServiceOperationResponse().withDescription(R_ID).withOperationState(OperationState.IN_PROGRESS);
        instance.withLastOperation(silo);
        GetLastServiceOperationResponse status = client.getRequestStatus(instance);
        assertNotNull(status);
        assertEquals(OperationState.SUCCEEDED, status.getState());
    }

    @Test
    public void testDelete() throws Exception {
        VrServiceInstance instance = getServiceInstanceToDelete();

        instance = client.deleteInstance(instance);

        // // LOG.info("get a token.");
        // // String token = tokenService.getToken();
        // // assertNotNull(token);
        //
        // LOG.info("get a delete template.");
        // // JsonElement deleteRequestTemplate =
        // client.getDeleteRequestTemplate(
        // // token, R_ID);
        //
        // JsonElement deleteRequestTemplate =
        // client.getDeleteRequestTemplate(instance);
        // assertNotNull(deleteRequestTemplate);
        //
        // LOG.info("edit the delete template.");
        // JsonObject editedDeleteRequestTemplate = client
        // .prepareDeleteRequestTemplate(deleteRequestTemplate, S_ID);
        // assertNotNull(editedDeleteRequestTemplate);
        //
        // LOG.info("posting delete request: "
        // + editedDeleteRequestTemplate.toString());
        // ResponseEntity<JsonElement> deleteResponse =
        // client.postDeleteRequest(
        // token, editedDeleteRequestTemplate, R_ID);

        String requestId = instance.getCurrentOperationRequestId();
        assertNotNull(requestId);
        LOG.info("delete request id: " + requestId);

        OperationState status = instance.getServiceInstanceLastOperation().getState();
        assertNotNull(status);
        LOG.info("delete request state: " + status);

        // LOG.info("wait for delete request to complete....");
        // ServiceInstanceLastOperation silo = client
        // .getRequestStatus(token, R_ID);
        // assertNotNull(silo);
        // while (silo.getState().equals(
        // VrServiceInstance.OPERATION_STATE_IN_PROGRESS)) {
        // TimeUnit.SECONDS.sleep(10);
        // silo = client.getRequestStatus(token, R_ID);
        // LOG.info("state is: " + silo.getState());
        // }
        //
        // LOG.info("state is: " + client.getRequestStatus(token,
        // R_ID).getState());
    }

    private VrServiceInstance getServiceInstanceToDelete() throws Exception {
        ServiceDefinition sd = catalogService
                .getServiceDefinition(TestConfig.SD_ID);

        VrServiceInstance instance = VrServiceInstance.create(TestConfig
                .getCreateServiceInstanceRequest(sd));

        instance.getMetadata().put(VrServiceInstance.CREATE_REQUEST_ID, R_ID);
        instance.getMetadata().put(VrServiceInstance.LOCATION, "foo" + R_ID);

        JsonParser parser = new JsonParser();
        JsonElement je = parser.parse(TestConfig
                .getContents("requestResponse.json"));

        instance.getParameters().putAll(
                client.getParametersFromCreateResponse(je));

        client.loadDataFromResourceResponse(tokenService.getToken(), instance);

        return instance;
    }

}