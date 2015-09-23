package org.cloudfoundry.community.servicebroker.vrealize;

import java.util.HashMap;
import java.util.Map;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceExistsException;
import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.DeleteServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.Plan;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.model.UpdateServiceInstanceRequest;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class VraClient {

	@Autowired
	private VraRepository vraRepository;

	@Autowired
	private VraCatalogRepo vraCatalogRepo;

	@Autowired
	Gson gson;

	@Autowired
	private String serviceUri;

	private static final Map<String, ServiceInstanceBinding> BINDINGS = new HashMap<String, ServiceInstanceBinding>();

	private static final Map<String, ServiceInstance> INSTANCES = new HashMap<String, ServiceInstance>();

	public ServiceInstanceBinding createBinding(
			CreateServiceInstanceBindingRequest request)
			throws ServiceBrokerException,
			ServiceInstanceBindingExistsException {
		if (request == null || request.getServiceDefinitionId() == null
				|| request.getPlanId() == null
				|| request.getBindingId() == null) {
			throw new ServiceBrokerException(
					"invalid CreateServiceInstanceBindingRequest object.");
		}

		ServiceInstanceBinding binding = BINDINGS.get(request.getBindingId());
		if (binding != null) {
			throw new ServiceInstanceBindingExistsException(binding);
		}

		ServiceDefinition serviceDefinition = null;
		Plan plan = null;

		for (ServiceDefinition def : getCatalog().getServiceDefinitions()) {
			if (def.getId().equals(request.getServiceDefinitionId())) {
				serviceDefinition = def;
				for (Plan p : def.getPlans()) {
					if (p.getId().equals(request.getPlanId())) {
						plan = p;
					}
				}
			}
		}

		if (serviceDefinition == null) {
			throw new ServiceBrokerException("service "
					+ request.getServiceDefinitionId()
					+ " not supported by this broker.");
		}

		if (plan == null) {
			throw new ServiceBrokerException("service plan "
					+ request.getPlanId() + " not supported by this broker.");
		}

		Map<String, Object> credentials = new HashMap<String, Object>();
		credentials.put("uri",
				serviceUri + "/" + plan.getMetadata().get("context"));

		binding = new ServiceInstanceBinding(request.getBindingId(),
				serviceDefinition.getId(), credentials, null,
				request.getAppGuid());

		BINDINGS.put(binding.getId(), binding);

		return binding;
	}

	public ServiceInstanceBinding deleteBinding(
			DeleteServiceInstanceBindingRequest request)
			throws ServiceBrokerException, ServiceInstanceDoesNotExistException {

		if (request == null || request.getBindingId() == null) {
			throw new ServiceBrokerException(
					"invalid DeleteServiceInstanceRequest object.");
		}

		ServiceInstanceBinding i = getBinding(request.getBindingId());
		if (i == null) {
			throw new ServiceInstanceDoesNotExistException(
					request.getBindingId());
		}

		BINDINGS.remove(request.getBindingId());
		return i;
	}

	public ServiceInstance createInstance(CreateServiceInstanceRequest request)
			throws ServiceBrokerException, ServiceInstanceExistsException {
		if (request == null || request.getServiceInstanceId() == null) {
			throw new ServiceBrokerException(
					"invalid CreateServiceInstanceRequest object.");
		}

		ServiceInstance instance = getInstance(request.getServiceInstanceId());
		if (instance != null) {
			throw new ServiceInstanceExistsException(INSTANCES.get(request
					.getServiceInstanceId()));
		}

		instance = new ServiceInstance(request);
		INSTANCES.put(request.getServiceInstanceId(), instance);

		return instance;
	}

	public ServiceInstance deleteInstance(DeleteServiceInstanceRequest request)
			throws ServiceInstanceDoesNotExistException, ServiceBrokerException {

		if (request == null || request.getServiceInstanceId() == null) {
			throw new ServiceBrokerException(
					"invalid DeleteServiceInstanceRequest object.");
		}

		ServiceInstance i = getInstance(request.getServiceInstanceId());
		if (i == null) {
			throw new ServiceInstanceDoesNotExistException(
					request.getServiceInstanceId());
		}

		INSTANCES.remove(request.getServiceInstanceId());
		return i;
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

	private ServiceInstanceBinding getBinding(String id) {
		if (id == null) {
			return null;
		}
		return BINDINGS.get(id);
	}

	private Catalog getCatalog() {
		return gson.fromJson(
				gson.fromJson(vraCatalogRepo.getCatalog(), JsonElement.class),
				Catalog.class);
	}
}
