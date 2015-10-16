package org.cloudfoundry.community.servicebroker.vrealize.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceExistsException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceUpdateNotSupportedException;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.DeleteServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.OperationState;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceLastOperation;
import org.cloudfoundry.community.servicebroker.model.UpdateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.cloudfoundry.community.servicebroker.vrealize.VraClient;
import org.cloudfoundry.community.servicebroker.vrealize.domain.Creds;
import org.cloudfoundry.community.servicebroker.vrealize.domain.VrServiceInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;

@Service
public class VrServiceInstanceService implements ServiceInstanceService {

	private static final Logger LOG = Logger
			.getLogger(VrServiceInstanceService.class);

	@Autowired
	VraClient vraClient;

	@Autowired
	TokenService tokenService;

	@Autowired
	CatalogService catalogService;

	@Autowired
	Creds creds;

	private static final Map<String, VrServiceInstance> INSTANCES = new HashMap<String, VrServiceInstance>();

	@Override
	public ServiceInstance getServiceInstance(String id) {
		if (id == null || getInstance(id) == null) {
			LOG.warn("service instance with id: " + id + " not found!");
			return null;
		}

		VrServiceInstance si = getInstance(id);
		String state = si.getServiceInstanceLastOperation().getState();
		LOG.info("service instance with id: " + id + " is in state: " + state);

		// this method is polled via cloud controller to see if the async create
		// request is complete
		if (!si.getServiceInstanceLastOperation().getState()
				.equals(OperationState.IN_PROGRESS)) {
			return si;
		}

		// still in progress? check to see how we're doing.
		String token;
		try {
			token = tokenService.getToken();
		} catch (ServiceBrokerException e) {
			LOG.error("unable to get auth token.", e);
			return null;
		}

		LOG.info("checking on status of request id: " + si.getvRRequestId());
		ServiceInstanceLastOperation status = vraClient.getRequestStatus(token,
				si);

		LOG.info("request: " + id + " status is: " + status.getState());

		si.withLastOperation(status);

		return si;
	}

	@Override
	public ServiceInstance createServiceInstance(
			CreateServiceInstanceRequest request)
			throws ServiceInstanceExistsException, ServiceBrokerException {

		if (request == null || request.getServiceDefinitionId() == null) {
			throw new ServiceBrokerException(
					"invalid CreateServiceInstanceRequest object.");
		}

		if (request.getServiceInstanceId() != null
				&& getInstance(request.getServiceInstanceId()) != null) {
			throw new ServiceInstanceExistsException(INSTANCES.get(request
					.getServiceInstanceId()));
		}

		ServiceDefinition sd = catalogService.getServiceDefinition(request
				.getServiceDefinitionId());

		if (sd == null) {
			throw new ServiceBrokerException(
					"Unable to find service definition with id: "
							+ request.getServiceDefinitionId());
		}

		String token = tokenService.getToken();

		// get a template for the request
		JsonElement template = vraClient.getRequestTemplate(token, sd);

		// customize the template
		JsonElement edited = vraClient.prepareRequestTemplate(template,
				request.getServiceInstanceId());

		// request the request with the request
		JsonElement response = vraClient.postRequest(token, edited, sd);

		LOG.debug("service request response: " + response.toString());

		VrServiceInstance instance = new VrServiceInstance(request);

		// add vr request id so we can correlate later
		instance.setvRRequestId(vraClient.getRequestId(response));

		// get information from response and add to service instance for later
		instance.getParameters().putAll(vraClient.getParameters(response));

		// set the last operation, since this is an async request
		instance.withLastOperation(new ServiceInstanceLastOperation(
				"vR Request submitted.", OperationState.IN_PROGRESS));
		instance.withAsync(true);

		INSTANCES.put(request.getServiceInstanceId(), instance);
		LOG.info("registered service instance: "
				+ instance.getServiceInstanceId() + " requestId: "
				+ instance.getvRRequestId());

		return instance;
	}

	@Override
	public ServiceInstance deleteServiceInstance(
			DeleteServiceInstanceRequest request) throws ServiceBrokerException {

		if (request == null || request.getServiceInstanceId() == null) {
			throw new ServiceBrokerException(
					"invalid DeleteServiceInstanceRequest object.");
		}

		ServiceInstance i = getInstance(request.getServiceInstanceId());
		if (i == null) {
			return null;
		}

		// String requestPayload = vraClient.deleteRequestPayload(request);
		// LOG.info("request submitted with payload: \n" + requestPayload);

		INSTANCES.remove(request.getServiceInstanceId());
		LOG.info("unregistered service instance: " + i.getServiceInstanceId());

		return i;
	}

	@Override
	public ServiceInstance updateServiceInstance(
			UpdateServiceInstanceRequest request)
			throws ServiceInstanceUpdateNotSupportedException,
			ServiceBrokerException, ServiceInstanceDoesNotExistException {

		throw new ServiceInstanceUpdateNotSupportedException(
				"vRealize services are not updatable.");
	}

	private VrServiceInstance getInstance(String id) {
		if (id == null) {
			return null;
		}
		return INSTANCES.get(id);
	}
}