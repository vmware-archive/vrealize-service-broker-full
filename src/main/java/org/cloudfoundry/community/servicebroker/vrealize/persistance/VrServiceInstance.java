package org.cloudfoundry.community.servicebroker.vrealize.persistance;

import java.util.HashMap;
import java.util.Map;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.DeleteServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.OperationState;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceLastOperation;
import org.cloudfoundry.community.servicebroker.model.UpdateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.vrealize.adapter.Adaptors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Document
public class VrServiceInstance extends ServiceInstance {

	// some "known" keys to store stuff under
	// metatdata keys
	public static final String LOCATION = "LOCATION";
	public static final String CREATE_REQUEST_ID = "CREATE_REQUEST_ID";
	public static final String DELETE_REQUEST_ID = "DELETE_REQUEST_ID";
	public static final String CREATE_TEMPLATE_LINK = "CREATE_TEMPLATE_LINK";
	public static final String DELETE_TEMPLATE_LINK = "DELETE_TEMPLATE_LINK";
	public static final String DELETE_LINK = "DELETE_LINK";
	public static final String RESOURCES_LINK = "RESOURCES_LINK";

	// parameter keys
	public static final String USER_ID = "USER_ID";
	public static final String PASSWORD = "PASSWORD";
	public static final String DB_ID = "DB_ID";
	public static final String HOST = "HOST";
	public static final String PORT = "PORT";
	public static final String SERVICE_TYPE = "SERVICE_TYPE";

	// other keys
	public static final String URI = "uri";
	public static final String OPERATION_STATE_SUCCEEDED = "succeeded";
	public static final String OPERATION_STATE_IN_PROGRESS = "in progress";

	@Id
	private String id;

	@JsonSerialize
	@JsonProperty("parameters")
	private final Map<String, Object> parameters = new HashMap<String, Object>();

	@JsonSerialize
	@JsonProperty("metadata")
	private final Map<String, Object> metadata = new HashMap<String, Object>();

	public static VrServiceInstance create(CreateServiceInstanceRequest request) {
		VrServiceInstance instance = new VrServiceInstance(request);
		// ServiceInstanceLastOperation silo = new ServiceInstanceLastOperation(
		// createRequestId, OperationState.IN_PROGRESS);
		// instance.withLastOperation(silo);
		instance.withAsync(true);
		instance.setId(instance.getServiceInstanceId());

		return instance;
	}

	public static VrServiceInstance update(VrServiceInstance instance,
			OperationState state) {
		ServiceInstanceLastOperation silo = new ServiceInstanceLastOperation(
				instance.getServiceInstanceLastOperation().getDescription(),
				state);
		instance.withLastOperation(silo);
		return instance;
	}

	public static VrServiceInstance delete(VrServiceInstance instance,
			String deleteRequestId) {
		instance.getMetadata().put(DELETE_REQUEST_ID, deleteRequestId);
		ServiceInstanceLastOperation silo = new ServiceInstanceLastOperation(
				deleteRequestId, OperationState.IN_PROGRESS);
		instance.withLastOperation(silo);

		return instance;
	}

	public VrServiceInstance() {
		this(new CreateServiceInstanceRequest());
	}

	public VrServiceInstance(CreateServiceInstanceRequest request) {
		super(request);
	}

	public VrServiceInstance(DeleteServiceInstanceRequest request) {
		super(request);
	}

	public VrServiceInstance(UpdateServiceInstanceRequest request) {
		super(request);
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

		return getServiceInstanceLastOperation().getState().equals(
				OPERATION_STATE_IN_PROGRESS);
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
		return getServiceInstanceLastOperation().getState().equals(
				OPERATION_STATE_SUCCEEDED);
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
}
