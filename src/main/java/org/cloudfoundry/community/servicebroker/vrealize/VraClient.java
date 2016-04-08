package org.cloudfoundry.community.servicebroker.vrealize;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import net.minidev.json.JSONArray;
import org.apache.log4j.Logger;
import org.cloudfoundry.community.servicebroker.vrealize.adapter.Adaptors;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.LastOperation;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.VrServiceInstance;
import org.cloudfoundry.community.servicebroker.vrealize.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.GetLastServiceOperationResponse;
import org.springframework.cloud.servicebroker.model.OperationState;
import org.springframework.cloud.servicebroker.model.ServiceDefinition;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;

@Service
public class VraClient {

    private static final Logger LOG = Logger.getLogger(VraClient.class);

    static final String SUCCESSFUL = "SUCCESSFUL";
    static final String UNSUBMITTED = "UNSUBMITTED";
    static final String SUBMITTED = "SUBMITTED";
    static final String PENDING_PRE_APPROVAL = "PENDING_PRE_APPROVAL";
    static final String PRE_APPROVED = "PRE_APPROVED";
    static final String IN_PROGRESS = "IN_PROGRESS";
    static final String PENDING_POST_APPROVAL = "PENDING_POST_APPROVAL";
    static final String POST_APPROVED = "POST_APPROVED";
    static final String PROVIDER_COMPLETED = "PROVIDER_COMPLETED";

    @Autowired
    private VraRepository vraRepository;

    @Autowired
    String serviceUri;

    @Autowired
    TokenService tokenService;

    public VrServiceInstance createInstance(
            CreateServiceInstanceRequest request, ServiceDefinition sd) {

        try {
            LOG.info("creating instance.");

            String token = tokenService.getToken();

            LOG.info("getting a template for the create request.");
            JsonElement template = getCreateRequestTemplate(token, sd);

            LOG.info("customizing the create template.");
            JsonElement edited = prepareCreateRequestTemplate(template,
                    request.getServiceInstanceId());

            LOG.info("posting the create request.");
            ResponseEntity<JsonElement> response = postCreateRequest(token, edited,
                    sd);

            LOG.info("service request response: " + response.toString());

            String location = getLocation(response);
            String requestId = getRequestId(response);

            VrServiceInstance instance = new VrServiceInstance(request);

            LOG.info("loading metadata onto instance from response.");
            instance.getMetadata().put(VrServiceInstance.LOCATION, location);
            instance.getMetadata().put(VrServiceInstance.CREATE_REQUEST_ID,
                    requestId);

            LastOperation lo = new LastOperation(OperationState.IN_PROGRESS, requestId, false);
            instance.withLastOperation(lo);

            return instance;
        } catch (Throwable t) {
            LOG.error("error processing create request.", t);
            return null;
        }
    }

    public void loadCredentials(VrServiceInstance instance) {
        String token = tokenService.getToken();

        String locationPath = pathFromLink(instance.getLocation().toString());
        JsonElement requestResponse = vraRepository.getRequest(
                "Bearer " + token, locationPath).getBody();

        instance.getParameters().putAll(
                getParametersFromCreateResponse(requestResponse));

        JsonElement resourcesResponse = getRequestResources(token, instance
                .getCreateRequestId().toString());

        instance.getParameters().putAll(
                getParametersFromResourceResponse(resourcesResponse));
    }

    public void loadDataFromResourceResponse(String token,
                                                          VrServiceInstance instance) {
        JsonElement resources = getRequestResources(token, instance
                .getCreateRequestId().toString());

        Map<String, String> links = getDeleteLinks(resources);
        instance.getMetadata().putAll(links);
    }

    public VrServiceInstance deleteInstance(VrServiceInstance instance) {

        try {
            String token = tokenService.getToken();

            LOG.info("getting delete template link from instance metadata.");
            String deleteTemplateLink = instance.getMetadata()
                    .get(VrServiceInstance.DELETE_TEMPLATE_LINK).toString();
            String deleteTemplatePath = pathFromLink(deleteTemplateLink);

            LOG.info("requesting delete template.");
            JsonElement template = vraRepository.getRequest("Bearer " + token,
                    deleteTemplatePath).getBody();

            LOG.info("customizing delete template.");
            JsonElement edited = prepareDeleteRequestTemplate(template,
                    instance.getId());

            LOG.info("getting delete link from instance metadata.");
            String deleteLink = instance.getMetadata()
                    .get(VrServiceInstance.DELETE_LINK).toString();
            String deletePath = pathFromLink(deleteLink);

            LOG.info("posting delete request.");
            ResponseEntity<JsonElement> response = vraRepository.postRequest(
                    "Bearer " + token, deletePath, edited);

            LOG.info("delete request response: " + response.toString());

            String requestId = getRequestIdFromLocation(getLocation(response));

            LOG.info("adding delete request metadata.");
            instance.getMetadata().put(VrServiceInstance.DELETE_REQUEST_ID, requestId);

            LastOperation lo = new LastOperation(OperationState.IN_PROGRESS, requestId, true);
            instance.withLastOperation(lo);

            return instance;
        } catch (Throwable t) {
            LOG.error("error processing delete request.", t);
            return null;
        }
    }

    private String getRequestIdFromLocation(String location) {
        if (location == null) {
            return null;
        }

        return location.substring(location.lastIndexOf('/') + 1);
    }

    private String getLocation(ResponseEntity<JsonElement> response) {
        return response.getHeaders().getLocation().toString();
    }

    JsonElement getCreateRequestTemplate(String token, ServiceDefinition sd) {
        if (token == null || sd == null) {
            return null;
        }

        return vraRepository.getRequest("Bearer " + token,
                getGetRequestPath(sd)).getBody();
    }

    private String getGetRequestPath(ServiceDefinition sd) {
        Map<String, Object> meta = sd.getPlans().get(0).getMetadata();
        String fullUri = meta.get("GET: Request Template").toString();
        return fullUri.substring(serviceUri.length() + 1);
    }

    private String getCreateRequestPath(ServiceDefinition sd) {
        Map<String, Object> meta = sd.getPlans().get(0).getMetadata();
        String fullUri = meta.get("POST: Submit Request").toString();
        return fullUri.substring(serviceUri.length() + 1);
    }

    private ResponseEntity<JsonElement> postCreateRequest(String token,
                                                          JsonElement body, ServiceDefinition sd) {

        if (token == null || body == null || sd == null) {
            return null;
        }

        return vraRepository.postRequest("Bearer " + token,
                getCreateRequestPath(sd), body);

    }

    JsonObject prepareCreateRequestTemplate(JsonElement template,
                                            String serviceInstanceId) {
        JsonObject jo = removeFields(template.getAsJsonObject(),
                getContents("fieldsToFilter.txt"));

        LOG.info("submitting request to vR for serviceInstance: "
                + serviceInstanceId);

        jo.addProperty("description", serviceInstanceId);
        jo.addProperty("reasons", "CF service broker request.");

        return jo;
    }

    private JsonObject prepareDeleteRequestTemplate(JsonElement template,
                                                    String serviceInstanceId) {//throws ServiceBrokerException {

        JsonObject jo = template.getAsJsonObject();
        jo.addProperty("description", serviceInstanceId);
        return jo;
    }

    private JsonObject removeFields(JsonObject json, List<String> fields) {
        if (json == null || fields == null) {
            return null;
        }
        Set<Entry<String, JsonElement>> cs = new HashSet<Entry<String, JsonElement>>();
        cs.addAll(json.entrySet());
        Iterator<Entry<String, JsonElement>> i = cs.iterator();
        while (i.hasNext()) {
            Entry<String, JsonElement> entry = i.next();
            if (fields.contains(entry.getKey())) {
                json.remove(entry.getKey());
            } else {
                if (entry.getValue().isJsonObject()) {
                    removeFields(entry.getValue().getAsJsonObject(), fields);
                }
            }
        }
        return json;
    }

    private List<String> getContents(String fileName) {
        try {
            URI u = new ClassPathResource(fileName).getURI();
            return Files.readAllLines(Paths.get(u), Charset.defaultCharset());
        } catch (IOException e) {
            LOG.error("error getting contents of file: " + fileName, e);
            throw new ServiceBrokerException("error reading template.", e);
        }
    }

    public GetLastServiceOperationResponse getRequestStatus(VrServiceInstance si) {
        if (si == null || si.getServiceInstanceLastOperation() == null) {
            return new GetLastServiceOperationResponse().withDescription("Unable to get request status: invalid request.").withOperationState(OperationState.FAILED);
        }

        String requestId = si.getCurrentOperationRequestId();
        if (requestId == null) {
            return new GetLastServiceOperationResponse().withDescription("Unable to get requestId from last operation.").withOperationState(OperationState.FAILED);
        }

        return getRequestStatus(tokenService.getToken(), requestId);
    }

    GetLastServiceOperationResponse getRequestStatus(String token, String requestId) {
        JsonElement je = vraRepository.getRequestStatus("Bearer " + token,
                requestId).getBody();

        if (je == null) {
            return new GetLastServiceOperationResponse().withDescription("Unable to get request status: nothing returned from vR service.").withOperationState(OperationState.FAILED);
        }

        return getLastOperation(je);
    }

    GetLastServiceOperationResponse getLastOperation(JsonElement jsonElement) {
        JsonObject jo = jsonElement.getAsJsonObject();
        JsonElement state = jo.get("state");
        JsonElement id = jo.get("id");

        if (id == null) {
            return new GetLastServiceOperationResponse().withDescription("Unable to determine id of request.").withOperationState(OperationState.FAILED);
        }

        if (state == null) {
            return new GetLastServiceOperationResponse().withDescription("Unable to determine state of request: " + id).withOperationState(OperationState.FAILED);
        }

        String requestId = id.getAsString();
        String vrState = state.getAsString();

        LOG.info("vra status for request id: " + requestId + " : " + vrState);

        return new GetLastServiceOperationResponse().withDescription(requestId).withOperationState(vrStatusToOperationState(vrState));
    }

    OperationState vrStatusToOperationState(String vrStatus) {
        if (vrStatus == null) {
            return OperationState.FAILED;
        }

        if (SUCCESSFUL.equals(vrStatus)) {
            return OperationState.SUCCEEDED;
        }

        if (UNSUBMITTED.equals(vrStatus) || SUBMITTED.equals(vrStatus)
                || PENDING_PRE_APPROVAL.equals(vrStatus)
                || PRE_APPROVED.equals(vrStatus)
                || IN_PROGRESS.equals(vrStatus)
                || PENDING_POST_APPROVAL.equals(vrStatus)
                || POST_APPROVED.equals(vrStatus)
                || PROVIDER_COMPLETED.equals(vrStatus)) {
            return OperationState.IN_PROGRESS;
        }

        return OperationState.FAILED;

    }

    private String getRequestId(ResponseEntity<JsonElement> requestResponse) {
        if (requestResponse == null) {
            return null;
        }

        JsonElement je = requestResponse.getBody().getAsJsonObject().get("id");
        if (je == null) {
            return null;
        }

        return je.getAsString();
    }

    private String getHostIP(JsonElement requestResponse) {
        if (requestResponse == null) {
            return null;
        }

        ReadContext ctx = JsonPath.parse(requestResponse.toString());
        JSONArray o = ctx.read("$.content[*].data.ip_address");

        if (o == null || o.size() < 1) {
            return null;
        }

        return o.get(0).toString();
    }

    Map<String, Object> getParametersFromCreateResponse(JsonElement response) {

        // get the custom parameters from the request info
        return Adaptors
                .getParameters(getCustomValues(response));
    }

    Map<String, Object> getParametersFromResourceResponse(
            JsonElement resourceViewResponse) throws ServiceBrokerException {

        Map<String, Object> m = new HashMap<String, Object>();

        // get the host from the resources view
        String host = getHostIP(resourceViewResponse);
        m.put(VrServiceInstance.HOST, host);

        return m;
    }

    private Map<String, Object> getCustomValues(JsonElement requestResponse) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        if (requestResponse == null) {
            return parameters;
        }

        ReadContext ctx = JsonPath.parse(requestResponse.toString());
        JSONArray ja = ctx
                .read("$.requestData.entries[*].value.values.entries[*]");

        if (ja == null) {
            return parameters;
        }

        for (int i = 0; i < ja.size(); i++) {
            JSONArray ka = ctx
                    .read("$.requestData.entries[*].value.values.entries[" + i
                            + "].key");

            JSONArray va = ctx
                    .read("$.requestData.entries[*].value.values.entries[" + i
                            + "].value.value");

            String key = null;
            Object value = null;

            if (ka != null && ka.size() > 0) {
                key = ka.get(0).toString();
            }

            if (va != null && va.size() > 0) {
                value = va.get(0).toString();
            }

            if (key != null) {
                parameters.put(key, value);
            }
        }
        return parameters;
    }

    private JsonElement getRequestResources(String token, String requestId) {
        return vraRepository.getRequestResources("Bearer " + token, requestId)
                .getBody();
    }

    private String pathFromLink(String link) {
        return link.substring(serviceUri.length() + 1);
    }

    Map<String, String> getDeleteLinks(JsonElement resources) {
        Map<String, String> map = new HashMap<String, String>();
        ReadContext ctx = JsonPath.parse(resources.toString());

        //seems to be no way to filter using json path where there is a '@' in the thing you are tyring to filter on?
        JSONArray o = ctx.read("$.content[*].links[*]");
        Iterator it = o.iterator();
        while (it.hasNext()) {
            LinkedHashMap ja = (LinkedHashMap) it.next();
            Object rel = ja.get("rel");

            if (rel.toString().equals("GET Template: {com.vmware.csp.component.cafe.composition@resource.action.deployment.destroy.name}")) {
                map.put(VrServiceInstance.DELETE_TEMPLATE_LINK, ja.get("href").toString());
            }

            if (rel.toString().equals("POST: {com.vmware.csp.component.cafe.composition@resource.action.deployment.destroy.name}")) {
                map.put(VrServiceInstance.DELETE_LINK, ja.get("href").toString());
            }
        }
        return map;
    }
}
