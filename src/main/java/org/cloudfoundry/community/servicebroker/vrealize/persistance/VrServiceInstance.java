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

package org.cloudfoundry.community.servicebroker.vrealize.persistance;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.cloudfoundry.community.servicebroker.vrealize.adapter.Adaptors;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.OperationState;
import org.springframework.cloud.servicebroker.model.UpdateServiceInstanceRequest;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class VrServiceInstance implements Serializable {

    public static final long serialVersionUID = 1L;

    // some "known" keys for metadata storage
    public static final String LOCATION = "LOCATION";
    public static final String CREATE_REQUEST_ID = "CREATE_REQUEST_ID";
    public static final String DELETE_REQUEST_ID = "DELETE_REQUEST_ID";
    public static final String DELETE_TEMPLATE_LINK = "GET Template: {com.vmware.csp.component.cafe.composition@resource.action.deployment.destroy.name}";
    public static final String DELETE_LINK = "POST: {com.vmware.csp.component.cafe.composition@resource.action.deployment.destroy.name}";
    public static final String SERVICE_TYPE = "SERVICE_TYPE";
    public static final String HOST = "HOST";

    public static final String URI = "uri";

    @JsonSerialize
    @JsonProperty("service_instance_id")
    private String id;

    @JsonSerialize
    @JsonProperty("parameters")
    private final Map<String, Object> parameters = new HashMap<String, Object>();

    @JsonSerialize
    @JsonProperty("metadata")
    private final Map<String, Object> metadata = new HashMap<String, Object>();

    @JsonSerialize
    @JsonProperty("last_operation")
    private LastOperation lastOperation;

    @JsonSerialize
    @JsonProperty("service_id")
    private String serviceDefinitionId;

    @JsonSerialize
    @JsonProperty("plan_id")
    private String planId;

    @JsonSerialize
    @JsonProperty("organization_guid")
    private String organizationGuid;

    @JsonSerialize
    @JsonProperty("space_guid")
    private String spaceGuid;

    public LastOperation getServiceInstanceLastOperation() {
        return lastOperation;
    }

    public void withLastOperation(LastOperation lastOperation) {
        this.lastOperation = lastOperation;
    }

    public VrServiceInstance() {
        this(new CreateServiceInstanceRequest());
    }

    public VrServiceInstance(CreateServiceInstanceRequest request) {
        this.serviceDefinitionId = request.getServiceDefinitionId();
        this.planId = request.getPlanId();
        this.organizationGuid = request.getOrganizationGuid();
        this.spaceGuid = request.getSpaceGuid();
        this.id = request.getServiceInstanceId();
        this.lastOperation = new LastOperation(OperationState.IN_PROGRESS, "Provisioning", false);

        if (request.getParameters() != null) {
            getParameters().putAll(request.getParameters());
        }
    }

    public VrServiceInstance(DeleteServiceInstanceRequest request) {
        this.id = request.getServiceInstanceId();
        this.planId = request.getPlanId();
        this.serviceDefinitionId = request.getServiceDefinitionId();
        this.lastOperation = new LastOperation(OperationState.IN_PROGRESS, "Deprovisioning", true);
    }

    public VrServiceInstance(UpdateServiceInstanceRequest request) {
        this.id = request.getServiceInstanceId();
        this.planId = request.getPlanId();
        this.lastOperation = new LastOperation(OperationState.IN_PROGRESS, "Updating", false);
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public boolean isInProgress() {
        if (getServiceInstanceLastOperation() == null
                || getServiceInstanceLastOperation().getState() == null) {
            return false;
        }

        return getServiceInstanceLastOperation().getState().equals(OperationState.IN_PROGRESS);
    }

    public boolean isCurrentOperationDelete() {
        return getMetadata().containsKey(DELETE_REQUEST_ID);
    }

    boolean isCurrentOperationCreate() {
        return getMetadata().containsKey(CREATE_REQUEST_ID)
                && !isCurrentOperationDelete();
    }

    public boolean isCurrentOperationSuccessful() {
        if (getServiceInstanceLastOperation() == null
                || getServiceInstanceLastOperation().getState() == null) {
            return false;
        }
        return getServiceInstanceLastOperation().getState().equals(OperationState.SUCCEEDED);
    }

    public String getCurrentOperationRequestId() {
        if (getServiceInstanceLastOperation() == null) {
            return null;
        }
        return getServiceInstanceLastOperation().getDescription();
    }

    public String getCreateRequestId() {
        if (getMetadata().get(CREATE_REQUEST_ID) == null) {
            return null;
        }
        return getMetadata().get(CREATE_REQUEST_ID).toString();
    }

    public Object getLocation() {
        return getMetadata().get(LOCATION);
    }

    public String getServiceType() {
        if (getMetadata() == null || getMetadata().get(SERVICE_TYPE) == null) {
            return null;
        }
        return getMetadata().get(SERVICE_TYPE).toString();
    }

    public void setServiceType(String serviceType) {
        getMetadata().put(SERVICE_TYPE, serviceType);
    }

    public String getHost() {
        if (getMetadata() == null || getMetadata().get(HOST) == null) {
            return null;
        }
        return getMetadata().get(HOST).toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Object> getCredentials() {
        return Adaptors.getCredentials(this);
    }

    public String getServiceDefinitionId() {
        return serviceDefinitionId;
    }

    public String getPlanId() {
        return planId;
    }

    public String getOrganizationGuid() {
        return organizationGuid;
    }

    public String getSpaceGuid() {
        return spaceGuid;
    }
}
