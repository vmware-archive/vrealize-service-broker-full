package org.cloudfoundry.community.servicebroker.vrealize.domain;

import java.util.HashMap;
import java.util.Map;

import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.DeleteServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.UpdateServiceInstanceRequest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class VrServiceInstance extends ServiceInstance {

	@JsonSerialize
	@JsonProperty("vr_request_id")
	private String vRRequestId;

	@JsonSerialize
	@JsonProperty("parameters")
	private final Map<String, Object> parameters = new HashMap<String, Object>();

	public VrServiceInstance(CreateServiceInstanceRequest request) {
		super(request);
	}

	public VrServiceInstance(DeleteServiceInstanceRequest request) {
		super(request);
	}

	public VrServiceInstance(UpdateServiceInstanceRequest request) {
		super(request);
	}

	public String getvRRequestId() {
		return vRRequestId;
	}

	public void setvRRequestId(String vRRequestId) {
		this.vRRequestId = vRRequestId;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void addParameter(String key, Object value) {
		getParameters().put(key, value);
	}

}
