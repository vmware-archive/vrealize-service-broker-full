package org.cloudfoundry.community.servicebroker.vrealize.persistance;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.cloudfoundry.community.servicebroker.vrealize.adapter.Adaptors;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Document
public class VrServiceInstance {

    // some "known" keys to store stuff under metatdata keys
    public static final String LOCATION = "LOCATION";
    public static final String CREATE_REQUEST_ID = "CREATE_REQUEST_ID";
    public static final String DELETE_REQUEST_ID = "DELETE_REQUEST_ID";
    public static final String DELETE_TEMPLATE_LINK = "DELETE_TEMPLATE_LINK";
    public static final String DELETE_LINK = "DELETE_LINK";

    // parameter keys
    public static final String USER_ID = "USER_ID";
    public static final String PASSWORD = "PASSWORD";
    public static final String DB_ID = "DB_ID";
    public static final String HOST = "HOST";
    public static final String PORT = "PORT";
    public static final String SERVICE_TYPE = "SERVICE_TYPE";

    // other keys
    public static final String URI = "uri";

    @Id
    private String id;

    @JsonSerialize
    @JsonProperty("parameters")
    private final Map<String, Object> parameters = new HashMap<String, Object>();

    @JsonSerialize
    @JsonProperty("metadata")
    private final Map<String, Object> metadata = new HashMap<String, Object>();

    @JsonSerialize
    @JsonProperty("last_operation")
    private GetLastServiceOperationResponse lastOperation;

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

    public static VrServiceInstance delete(VrServiceInstance instance,
                                           String deleteRequestId) {
        instance.getMetadata().put(DELETE_REQUEST_ID, deleteRequestId);
        GetLastServiceOperationResponse silo = new GetLastServiceOperationResponse().withDescription(deleteRequestId).
                withDeleteOperation(true).withOperationState(OperationState.IN_PROGRESS);
        instance.withLastOperation(silo);

        return instance;
    }

    public GetLastServiceOperationResponse getServiceInstanceLastOperation() {
        return lastOperation;
    }

    public VrServiceInstance withLastOperation(GetLastServiceOperationResponse lastOperation) {
        this.lastOperation = lastOperation;
        return this;
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
        this.lastOperation = new GetLastServiceOperationResponse()
                .withOperationState(OperationState.IN_PROGRESS)
                .withDescription("Provisioning");
    }

    public VrServiceInstance(DeleteServiceInstanceRequest request) {
        this.id = request.getServiceInstanceId();
        this.planId = request.getPlanId();
        this.serviceDefinitionId = request.getServiceDefinitionId();
        this.lastOperation = new GetLastServiceOperationResponse()
                .withOperationState(OperationState.IN_PROGRESS)
                .withDescription("Deprovisioning");
    }

    public VrServiceInstance(UpdateServiceInstanceRequest request) {
        this.id = request.getServiceInstanceId();
        this.planId = request.getPlanId();
        this.lastOperation = new GetLastServiceOperationResponse()
                .withOperationState(OperationState.IN_PROGRESS)
                .withDescription("Updating");
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

    public boolean isCurrentOperationCreate() {
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

    public Object getCreateRequestId() {
        return getMetadata().get(CREATE_REQUEST_ID);
    }

    public Object getLocation() {
        return getMetadata().get(VrServiceInstance.LOCATION);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Object> getCredentials() throws ServiceBrokerException {
        return Adaptors.getCredentials(this);
    }

    public boolean hasCredentials() {
        return Adaptors.hasCredentials(this);
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
