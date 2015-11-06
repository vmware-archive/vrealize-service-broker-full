package org.cloudfoundry.community.servicebroker.vrealize.service;

import org.apache.log4j.Logger;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceExistsException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceUpdateNotSupportedException;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.DeleteServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceLastOperation;
import org.cloudfoundry.community.servicebroker.model.UpdateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.cloudfoundry.community.servicebroker.vrealize.VraClient;
import org.cloudfoundry.community.servicebroker.vrealize.domain.Creds;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.VrServiceInstanceRepository;
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
	VrServiceInstanceRepository repository;

	@Autowired
	Creds creds;

	@Override
	public ServiceInstance getServiceInstance(String id) {

		if (id == null || getInstance(id) == null) {
			LOG.warn("service instance with id: " + id + " not found!");
			return null;
		}

		ServiceInstance si = getInstance(id);

		// check the last operation
		ServiceInstanceLastOperation silo = si
				.getServiceInstanceLastOperation();
		if (silo == null || silo.getState() == null) {
			LOG.error("ServiceInstance: " + id + " has no last operation.");
			deleteInstance(si);
			return null;
		}

		if (!si.isInProgress()) {
			return si;
		}

		// still in progress, let's check up on things...
		String currentRequestId = silo.getDescription();
		if (currentRequestId == null) {
			LOG.error("ServiceInstance: " + id + " last operation has no id.");
			deleteInstance(si);
			return null;
		}

		String state = si.getServiceInstanceLastOperation().getState();
		LOG.info("service instance id: " + id + " request id: "
				+ currentRequestId + " is in state: " + state);

		String token;
		try {
			token = tokenService.getToken();
		} catch (ServiceBrokerException e) {
			LOG.error("unable to get auth token.", e);
			return null;
		}

		LOG.info("checking on status of request id: " + currentRequestId);
		ServiceInstanceLastOperation status = vraClient.getRequestStatus(token,
				si);

		LOG.info("request: " + id + " status is: " + status.getState());

		si.withLastOperation(status);

		// if this was a create request and was successful, load metadata
		if (si.isCurrentOperationSuccessful() && si.isCurrentOperationCreate()) {
			vraClient.loadMetadata(token, si);
		}

		// if this is a delete request and was successful, remove the instance
		if (si.isCurrentOperationSuccessful() && si.isCurrentOperationDelete()) {
			deleteInstance(si);
		}

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
			throw new ServiceInstanceExistsException(repository.findOne(request
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
		JsonElement template = vraClient.getCreateRequestTemplate(token, sd);

		// customize the template
		JsonElement edited = vraClient.prepareCreateRequestTemplate(template,
				request.getServiceInstanceId());

		// request the request with the request
		JsonElement response = vraClient.postCreateRequest(token, edited, sd);

		LOG.debug("service request response: " + response.toString());

		String requestId = vraClient.getRequestId(response);
		ServiceInstance instance = new ServiceInstance(request);

		instance = saveInstance(instance);

		LOG.info("registered service instance: "
				+ instance.getServiceInstanceId() + " requestId: " + requestId);

		return instance;
	}

	@Override
	public ServiceInstance deleteServiceInstance(
			DeleteServiceInstanceRequest request) throws ServiceBrokerException {

		if (request == null || request.getServiceInstanceId() == null) {
			throw new ServiceBrokerException(
					"invalid DeleteServiceInstanceRequest object.");
		}

		ServiceInstance si = getInstance(request.getServiceInstanceId());
		if (si == null) {
			throw new ServiceBrokerException("Service instance: "
					+ request.getServiceInstanceId() + " not found.");
		}

		String token = tokenService.getToken();

		// get the delete request template from the resources
		JsonElement template = vraClient.getDeleteRequestTemplate(token, si.getServiceInstanceId());

		// customize the template
		JsonElement edited = vraClient.prepareDeleteRequestTemplate(template,
				si.getServiceInstanceId());

		// request the delete with the template
		JsonElement response = vraClient.postDeleteRequest(token, edited, si.getServiceInstanceId());

		LOG.debug("service request response: " + response.toString());

		String requestId = vraClient.getRequestId(response);

		// update si with new delete metadata
		si = ServiceInstance.delete(si, requestId);

		LOG.info("unregistering service instance: " + si.getServiceInstanceId()
				+ " requestId: " + requestId);

		return si;
	}

	@Override
	public ServiceInstance updateServiceInstance(
			UpdateServiceInstanceRequest request)
			throws ServiceInstanceUpdateNotSupportedException,
			ServiceBrokerException, ServiceInstanceDoesNotExistException {

		throw new ServiceInstanceUpdateNotSupportedException(
				"vRealize services are not updatable.");
	}

	private ServiceInstance getInstance(String id) {
		if (id == null) {
			return null;
		}
		return repository.findOne(id);
	}

	ServiceInstance deleteInstance(ServiceInstance instance) {
		if (instance == null || instance.getServiceInstanceId() == null) {
			return null;
		}
		repository.delete(instance.getServiceInstanceId());
		return instance;
	}

	ServiceInstance saveInstance(ServiceInstance instance) {
		return repository.save(instance);
	}
}