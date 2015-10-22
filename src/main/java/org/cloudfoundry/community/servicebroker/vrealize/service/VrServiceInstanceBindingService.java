package org.cloudfoundry.community.servicebroker.vrealize.service;

import java.util.HashMap;
import java.util.Map;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceBindingService;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.cloudfoundry.community.servicebroker.vrealize.VraClient;
import org.cloudfoundry.community.servicebroker.vrealize.adapter.Adaptors;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.VrServiceInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VrServiceInstanceBindingService implements
		ServiceInstanceBindingService {

	@Autowired
	private VraClient vraClient;

	@Autowired
	ServiceInstanceService serviceInstanceService;

	private static final Map<String, ServiceInstanceBinding> BINDINGS = new HashMap<String, ServiceInstanceBinding>();

	@Override
	public ServiceInstanceBinding createServiceInstanceBinding(
			CreateServiceInstanceBindingRequest request)
			throws ServiceInstanceBindingExistsException,
			ServiceBrokerException {

		if (request == null) {
			throw new ServiceBrokerException("invalid binding request.");
		}

		String bindingId = request.getBindingId();
		if (bindingId != null && BINDINGS.containsKey(bindingId)) {
			throw new ServiceInstanceBindingExistsException(
					BINDINGS.get(bindingId));
		}

		String serviceInstanceId = request.getServiceInstanceId();
		VrServiceInstance si = (VrServiceInstance) serviceInstanceService
				.getServiceInstance(serviceInstanceId);

		// not supposed to happen per the spec, but better check...
		if (si.isInProgress()) {
			throw new ServiceBrokerException(
					"ServiceInstance operation is still in progress.");
		}

		ServiceInstanceBinding binding = new ServiceInstanceBinding(bindingId,
				serviceInstanceId, Adaptors.getCredentials(si), null,
				request.getAppGuid());

		saveBinding(binding);
		return binding;
	}

	@Override
	public ServiceInstanceBinding deleteServiceInstanceBinding(
			DeleteServiceInstanceBindingRequest request)
			throws ServiceBrokerException {

		if (request == null || request.getBindingId() == null) {
			throw new ServiceBrokerException(
					"invalid DeleteServiceInstanceRequest object.");
		}

		ServiceInstanceBinding binding = getBinding(request.getBindingId());
		if (binding == null) {
			throw new ServiceBrokerException("binding with id: "
					+ request.getBindingId() + " does not exist.");
		}

		deleteBinding(binding);
		return binding;
	}

	private ServiceInstanceBinding getBinding(String id) {
		return BINDINGS.get(id);
	}

	private ServiceInstanceBinding saveBinding(ServiceInstanceBinding binding) {
		BINDINGS.put(binding.getId(), binding);
		return binding;
	}

	private ServiceInstanceBinding deleteBinding(ServiceInstanceBinding binding) {
		return BINDINGS.remove(binding.getId());
	}

}
