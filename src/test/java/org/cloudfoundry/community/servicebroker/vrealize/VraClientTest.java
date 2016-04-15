package org.cloudfoundry.community.servicebroker.vrealize;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.VrServiceInstance;
import org.cloudfoundry.community.servicebroker.vrealize.service.CatalogService;
import org.cloudfoundry.community.servicebroker.vrealize.service.TokenService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.GetLastServiceOperationResponse;
import org.springframework.cloud.servicebroker.model.OperationState;
import org.springframework.cloud.servicebroker.model.ServiceDefinition;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
public class VraClientTest {

    @Autowired
    private VraClient client;

    @Autowired
    TokenService tokenService;

    @Autowired
    CatalogService catalogService;

    @Autowired
    Gson gson;

    @Autowired
    VraRepository repo;

    @Test
    public void testGetRequestResources() {
        ResponseEntity<JsonElement> re = repo.getRequestResources("Bearer " + tokenService.getToken(), TestConfig.REQ_ID);
        assertNotNull(re.getBody());
    }

    @Test
    public void testGetRequestTemplate() throws ServiceBrokerException {
        ServiceDefinition sd = catalogService
                .getServiceDefinition(TestConfig.SD_ID);
        assertNotNull(sd);

        String token = tokenService.getToken();
        assertNotNull(token);

        JsonElement template = client.getCreateRequestTemplate(token, sd);
        assertNotNull(template);
    }

    @Test
    public void testPrepareRequest() throws Exception {
        JsonParser parser = new JsonParser();
        JsonObject o = (JsonObject) parser.parse(TestConfig
                .getContents("requestTemplate.json"));
        String s = client.prepareCreateRequestTemplate(o, TestConfig.getServiceInstance()).toString();
        assertEquals(TestConfig.getContents("filteredRequestTemplate.json"), s);
    }

    @Test
    public void testGetRequestStatus() throws Exception {
        JsonParser parser = new JsonParser();
        JsonElement je = parser.parse(TestConfig
                .getContents("requestResponse.json"));

        GetLastServiceOperationResponse silo = client.getLastOperation(je);
        assertNotNull(silo);
        assertEquals(OperationState.IN_PROGRESS, silo.getState());
    }

    @Test
    public void testGetLastOperation() throws Exception {
        JsonParser parser = new JsonParser();
        JsonElement je = parser.parse(TestConfig
                .getContents("submittedRequestResponse.json"));

        GetLastServiceOperationResponse silo = client.getLastOperation(je);
        assertNotNull(silo);
        assertEquals(OperationState.IN_PROGRESS, silo.getState());

        je = parser.parse(TestConfig
                .getContents("inProgressRequestResponse.json"));
        silo = client.getLastOperation(je);
        assertNotNull(silo);
        assertEquals(OperationState.IN_PROGRESS, silo.getState());

        je = parser.parse(TestConfig
                .getContents("pendingPreRequestResponse.json"));
        silo = client.getLastOperation(je);
        assertNotNull(silo);
        assertEquals(OperationState.IN_PROGRESS, silo.getState());

        je = parser.parse(TestConfig
                .getContents("pendingPostRequestResponse.json"));
        silo = client.getLastOperation(je);
        assertNotNull(silo);
        assertEquals(OperationState.IN_PROGRESS, silo.getState());

        je = parser.parse(TestConfig
                .getContents("successfulPostRequestResponse.json"));
        silo = client.getLastOperation(je);
        assertNotNull(silo);
        assertEquals(OperationState.SUCCEEDED, silo.getState());

        je = parser.parse(TestConfig
                .getContents("failedPostRequestResponse.json"));
        silo = client.getLastOperation(je);
        assertNotNull(silo);
        assertEquals(OperationState.FAILED, silo.getState());

        je = parser.parse(TestConfig
                .getContents("rejectedRequestResponse.json"));
        silo = client.getLastOperation(je);
        assertNotNull(silo);
        assertEquals(OperationState.FAILED, silo.getState());

        je = parser.parse(TestConfig
                .getContents("bogusStateRequestResponse.json"));
        silo = client.getLastOperation(je);
        assertNotNull(silo);
        assertEquals(OperationState.FAILED, silo.getState());

        je = parser.parse(TestConfig
                .getContents("missingStateRequestResponse.json"));
        silo = client.getLastOperation(je);
        assertNotNull(silo);
        assertEquals(OperationState.FAILED, silo.getState());
    }

    @Test
    public void testGetParmsAndMeta() throws Exception {
        JsonParser parser = new JsonParser();

        JsonElement crtemplate = parser.parse(TestConfig
                .getContents("requestTemplate.json"));

        VrServiceInstance instance = new VrServiceInstance(TestConfig.getCreateServiceInstanceRequest());

        JsonElement rr = parser.parse(TestConfig
                .getContents("resourcesResponse.json"));

        Map<String, Object> meta = client
                .getMetadataFromResourceResponse(rr);

        meta.put(VrServiceInstance.HOST, client.getHostIP(rr));

        String serviceType = client.getServiceType(crtemplate);
        meta.put(VrServiceInstance.SERVICE_TYPE, serviceType);

        instance.getMetadata().putAll(meta);

        assertNotNull(meta);
        assertEquals(4, meta.size());
        assertEquals("mysql", meta.get(VrServiceInstance.SERVICE_TYPE));
        assertEquals("192.168.200.214", meta.get(VrServiceInstance.HOST));
        assertEquals("https://vra-cafe.vra.pcflab.net/catalog-service/api/consumer/resources/9450b691-4b5a-43b4-8cd1-a54912c4ba85/actions/25e17ec5-e2fd-4bba-bb3b-25b69dd18bd7/requests/template",
                meta.get(VrServiceInstance.DELETE_TEMPLATE_LINK));
        assertEquals("https://vra-cafe.vra.pcflab.net/catalog-service/api/consumer/resources/9450b691-4b5a-43b4-8cd1-a54912c4ba85/actions/25e17ec5-e2fd-4bba-bb3b-25b69dd18bd7/requests",
                meta.get(VrServiceInstance.DELETE_LINK));

        assertEquals("mysql://root:secret@192.168.200.214:1234/aDB", instance.getCredentials().get(VrServiceInstance.URI));
    }

    @Test
    public void testStateTranslation() {
        assertEquals(OperationState.FAILED,
                client.vrStatusToOperationState(null));
        assertEquals(OperationState.FAILED,
                client.vrStatusToOperationState(""));
        assertEquals(OperationState.FAILED,
                client.vrStatusToOperationState("foo"));
        assertEquals(OperationState.IN_PROGRESS,
                client.vrStatusToOperationState(VraClient.IN_PROGRESS));
        assertEquals(OperationState.IN_PROGRESS,
                client.vrStatusToOperationState(VraClient.PENDING_POST_APPROVAL));
        assertEquals(OperationState.IN_PROGRESS,
                client.vrStatusToOperationState(VraClient.PENDING_PRE_APPROVAL));
        assertEquals(OperationState.IN_PROGRESS,
                client.vrStatusToOperationState(VraClient.POST_APPROVED));
        assertEquals(OperationState.IN_PROGRESS,
                client.vrStatusToOperationState(VraClient.PRE_APPROVED));
        assertEquals(OperationState.IN_PROGRESS,
                client.vrStatusToOperationState(VraClient.SUBMITTED));
        assertEquals(OperationState.IN_PROGRESS,
                client.vrStatusToOperationState(VraClient.PROVIDER_COMPLETED));
        assertEquals(OperationState.IN_PROGRESS,
                client.vrStatusToOperationState(VraClient.UNSUBMITTED));
        assertEquals(OperationState.SUCCEEDED,
                client.vrStatusToOperationState(VraClient.SUCCESSFUL));
    }
}
