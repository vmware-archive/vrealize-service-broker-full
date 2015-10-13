package org.cloudfoundry.community.servicebroker.vrealize.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceExistsException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceUpdateNotSupportedException;
import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.DeleteServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.UpdateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.cloudfoundry.community.servicebroker.vrealize.VraClient;
import org.cloudfoundry.community.servicebroker.vrealize.domain.Creds;
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
	Catalog catalog;

	@Autowired
	Creds creds;

	private static final Map<String, ServiceInstance> INSTANCES = new HashMap<String, ServiceInstance>();

	@Override
	public ServiceInstance getServiceInstance(String id) {
		return getInstance(id);
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

		ServiceDefinition sd = vraClient.getEntitledCatalogItem(request
				.getServiceDefinitionId());
		if (sd == null) {
			throw new ServiceBrokerException(request.getServiceDefinitionId());
		}

		String token = tokenService.getToken();

		// get a template for the request
		JsonElement template = vraClient.getRequestTemplate(token, sd);

		// edit the template
		JsonElement edited = vraClient.prepareRequest(template);

		// request the request with the request
		// JsonElement response = vraClient.postRequest(token, edited, sd);

		// TODO get some actual id from the vr response
		request.withServiceInstanceId(UUID.randomUUID().toString());

		// TODO submit and poll for response to request
		// LOG.info("request submitted with payload: \n" + requestPayload);

		ServiceInstance instance = new ServiceInstance(request);

		INSTANCES.put(request.getServiceInstanceId(), instance);
		LOG.info("registered service instance: "
				+ instance.getServiceInstanceId());

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

	private ServiceInstance getInstance(String id) {
		if (id == null) {
			return null;
		}
		return INSTANCES.get(id);
	}
}