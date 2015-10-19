package org.cloudfoundry.community.servicebroker.vrealize.domain;

import java.util.HashMap;
import java.util.Map;

import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.DeleteServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.OperationState;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceLastOperation;
import org.cloudfoundry.community.servicebroker.model.UpdateServiceInstanceRequest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class VrServiceInstance extends ServiceInstance {

	public enum VrKey {
		CREATE_REQUEST_ID, DELETE_REQUEST_ID, CREATE_TEMPLATE_LINK, DELETE_TEMPLATE_LINK, DELETE_LINK, RESOURCES_LINK
	}

	@JsonSerialize
	@JsonProperty("parameters")
	private final Map<String, Object> parameters = new HashMap<String, Object>();

	@JsonSerialize
	@JsonProperty("metadata")
	private final Map<Enum<?>, String> metadata = new HashMap<Enum<?>, String>();

	public static VrServiceInstance create(
			CreateServiceInstanceRequest request, String createRequestId) {
		VrServiceInstance instance = new VrServiceInstance(request);
		ServiceInstanceLastOperation silo = new ServiceInstanceLastOperation(
				createRequestId, OperationState.IN_PROGRESS);
		instance.withLastOperation(silo);
		instance.getMetadata().put(VrServiceInstance.VrKey.CREATE_REQUEST_ID,
				createRequestId);
		instance.withAsync(true);

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
		instance.getMetadata().put(VrServiceInstance.VrKey.DELETE_REQUEST_ID,
				deleteRequestId);
		ServiceInstanceLastOperation silo = new ServiceInstanceLastOperation(
				deleteRequestId, OperationState.IN_PROGRESS);
		instance.withLastOperation(silo);

		return instance;
	}

	private VrServiceInstance(CreateServiceInstanceRequest request) {
		super(request);
	}

	private VrServiceInstance(DeleteServiceInstanceRequest request) {
		super(request);
	}

	private VrServiceInstance(UpdateServiceInstanceRequest request) {
		super(request);
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public Map<Enum<?>, String> getMetadata() {
		return metadata;
	}

	public boolean isInProgress() {
		if (getServiceInstanceLastOperation() == null
				|| getServiceInstanceLastOperation().getState() == null) {
			return false;
		}

		return getServiceInstanceLastOperation().getState().equals(
				"in progress");
	}

	public boolean isCurrentOperationDelete() {
		return getMetadata().containsKey(VrKey.DELETE_REQUEST_ID);
	}

	public boolean isCurrentOperationCreate() {
		return getMetadata().containsKey(VrKey.CREATE_REQUEST_ID)
				&& !isCurrentOperationDelete();
	}

	public boolean isCurrentOperationSuccessful() {
		if (getServiceInstanceLastOperation() == null
				|| getServiceInstanceLastOperation().getState() == null) {
			return false;
		}
		return getServiceInstanceLastOperation().getState().equals("succeeded");
	}

	public String getCurrentOperationRequestId() {
		if (getServiceInstanceLastOperation() == null) {
			return null;
		}
		return getServiceInstanceLastOperation().getDescription();
	}

	public String getCreateRequestId() {
		return getMetadata().get(VrKey.CREATE_REQUEST_ID);
	}
}
