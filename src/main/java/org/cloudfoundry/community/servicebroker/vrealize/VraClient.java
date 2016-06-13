/**
 * vrealize-service-broker
 * <p>
 * Copyright (c) 2015-Present Pivotal Software, Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * limitations under the License.
 */

package org.cloudfoundry.community.servicebroker.vrealize;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import net.minidev.json.JSONArray;
import org.apache.log4j.Logger;
import org.cloudfoundry.community.servicebroker.vrealize.adapter.Adaptor;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

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
            VrServiceInstance instance = new VrServiceInstance(request);

            String token = tokenService.getToken();

            LOG.info("getting a template for the create request.");
            JsonElement template = getCreateRequestTemplate(token, sd);
            String serviceType = getServiceType(template);

            LOG.debug("template for create request: " + template.toString());

            LOG.info("customizing the create template.");
            JsonObject edited = prepareCreateRequestTemplate(template, instance);

            LOG.debug("customed create template: " + edited.toString());

            LOG.info("posting the create request.");
            ResponseEntity<JsonElement> response = postCreateRequest(token, edited,
                    sd);

            LOG.debug("service request response: " + response.toString());

            String location = getLocation(response);
            String requestId = getRequestId(response.getBody());

            LOG.info("loading metadata onto instance from catalog item request response.");
            instance.getMetadata().put(VrServiceInstance.LOCATION, location);
            instance.getMetadata().put(VrServiceInstance.CREATE_REQUEST_ID,
                    requestId);
            instance.setServiceType(serviceType);

            LastOperation lo = new LastOperation(OperationState.IN_PROGRESS, requestId, false);
            instance.withLastOperation(lo);

            return instance;
        } catch (Throwable t) {
            LOG.error("error processing create request.", t);
            throw new ServiceBrokerException("Unable to process create request.", t);
        }
    }

    public void loadCredentials(VrServiceInstance instance) {
        String token = tokenService.getToken();

        LOG.info("loading host for request: " + instance.getCreateRequestId());
        JsonElement resourcesResponse = vraRepository.getRequestResources("Bearer " + token, instance.getCreateRequestId()).getBody();

        LOG.debug("loading credentials from: " + instance.getLocation().toString() + ": " + resourcesResponse.toString());
        instance.getMetadata().putAll(getLinks(resourcesResponse));

        LOG.debug("loading host from response: " + resourcesResponse.toString());
        instance.getMetadata().put(VrServiceInstance.HOST, getHostIP(resourcesResponse));
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

            LOG.debug("delete request response: " + response.toString());

            String requestId = getRequestIdFromLocation(getLocation(response));

            LOG.info("adding delete request metadata.");
            instance.getMetadata().put(VrServiceInstance.DELETE_REQUEST_ID, requestId);

            LastOperation lo = new LastOperation(OperationState.IN_PROGRESS, requestId, true);
            instance.withLastOperation(lo);

            return instance;
        } catch (Throwable t) {
            LOG.error("error processing delete request.", t);
            throw new ServiceBrokerException("Unable to process delete request.", t);
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
                                            VrServiceInstance serviceInstance) {
        DocumentContext ctx = JsonPath.parse(template.toString());

        String serviceType = ctx.read("$.data.SERVICE_TYPE");
        serviceInstance.setServiceType(serviceType);

        Adaptor adaptor = Adaptors.getAdaptor(serviceType);

        if (adaptor == null) {
            throw new ServiceBrokerException("service adaptor not found for SERVICE_TYPE: " + serviceType);
        }

        //add service specific information onto the request
        adaptor.prepareRequest(ctx, serviceInstance);

        //add some identifying information onto the request
        ctx.set("$.description", serviceInstance.getId());
        ctx.set("$.reasons", "CF vRA Service Broker request.");

        return new Gson().fromJson(ctx.jsonString(), JsonElement.class).getAsJsonObject();
    }

    private JsonObject prepareDeleteRequestTemplate(JsonElement template,
                                                    String serviceInstanceId) {

        JsonObject jo = template.getAsJsonObject();
        jo.addProperty("description", serviceInstanceId);
        return jo;
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

    String getRequestId(JsonElement requestResponse) {
        if (requestResponse == null) {
            return null;
        }

        JsonElement je = requestResponse.getAsJsonObject().get("id");
        if (je == null) {
            return null;
        }

        return je.getAsString();
    }

    String getServiceType(JsonElement requestResponse) {
        if (requestResponse == null) {
            return null;
        }

        ReadContext ctx = JsonPath.parse(requestResponse.toString());
        return ctx.read("$.data." + VrServiceInstance.SERVICE_TYPE);
    }

    String getHostIP(JsonElement requestResponse) {
        if (requestResponse == null) {
            return null;
        }

        ReadContext ctx = JsonPath.parse(requestResponse.toString());
        JSONArray o = ctx.read("$.content[*].data.ip_address");

        if (o == null || o.size() < 1 || o.get(0) == null) {
            return null;
        }

        return o.get(0).toString();
    }

    private String pathFromLink(String link) {
        return link.substring(serviceUri.length() + 1);
    }

    Map<String, Object> getLinks(JsonElement resources) {
        Map<String, Object> map = new HashMap<String, Object>();
        ReadContext ctx = JsonPath.parse(resources.toString());

        JSONArray o = ctx.read("$.content[*].links[*]");
        Iterator it = o.iterator();
        while (it.hasNext()) {
            LinkedHashMap ja = (LinkedHashMap) it.next();
            Object key = ja.get("rel");
            Object val = ja.get("href");

            if (key != null && val != null) {
                map.put(key.toString(), val);
            }

        }
        return map;
    }
}
