package org.cloudfoundry.community.servicebroker.vrealize;

import java.util.HashMap;
import java.util.Map;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceExistsException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceUpdateNotSupportedException;
import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.DeleteServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.UpdateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VrServiceInstanceService implements ServiceInstanceService {

	@Autowired
	VraClient vraClient;

	private static final Map<String, ServiceInstance> INSTANCES = new HashMap<String, ServiceInstance>();

	@Override
	public ServiceInstance getServiceInstance(String id) {
		return getInstance(id);
	}

	@Override
	public ServiceInstance createServiceInstance(
			CreateServiceInstanceRequest request)
			throws ServiceInstanceExistsException, ServiceBrokerException {

		if (request == null) {
			throw new ServiceBrokerException(
					"invalid CreateServiceInstanceRequest object.");
		}

		if (request.getServiceInstanceId() != null
				&& getInstance(request.getServiceInstanceId()) != null) {
			throw new ServiceInstanceExistsException(INSTANCES.get(request
					.getServiceInstanceId()));
		}

		Creds creds = Creds.fromMap(request.getParameters());
		String token = vraClient.getToken(creds);

		// save token for later use
		request.getParameters().put("token", token);

		// get vR catalog item for this request
		Catalog catalog = vraClient.getEntitledCatalogItems(token);

		// submit request for service

		// poll for response to request

		// create and register service instance

		ServiceInstance instance = new ServiceInstance(request);
		INSTANCES.put(request.getServiceInstanceId(), instance);

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
			throw new ServiceBrokerException(
					"Service instance does not exist: "
							+ request.getServiceInstanceId());
		}

		INSTANCES.remove(request.getServiceInstanceId());
		return i;
	}

	@Override
	public ServiceInstance updateServiceInstance(
			UpdateServiceInstanceRequest request)
			throws ServiceInstanceUpdateNotSupportedException,
			ServiceBrokerException, ServiceInstanceDoesNotExistException {
		return updateInstance(request);
	}

	public ServiceInstance updateInstance(UpdateServiceInstanceRequest request) {
		// not supported yet
		return null;
	}

	public ServiceInstance getInstance(String id) {
		if (id == null) {
			return null;
		}
		return INSTANCES.get(id);
	}
}