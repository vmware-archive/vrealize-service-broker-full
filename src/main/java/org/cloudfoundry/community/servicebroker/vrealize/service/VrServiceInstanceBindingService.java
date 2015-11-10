package org.cloudfoundry.community.servicebroker.vrealize.service;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceBindingService;
import org.cloudfoundry.community.servicebroker.vrealize.VraClient;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.ServiceInstanceBindingRepository;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.VrServiceInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VrServiceInstanceBindingService implements
		ServiceInstanceBindingService {

	@Autowired
	private VraClient vraClient;

	@Autowired
	VrServiceInstanceService serviceInstanceService;

	@Autowired
	ServiceInstanceBindingRepository repository;

	@Override
	public ServiceInstanceBinding createServiceInstanceBinding(
			CreateServiceInstanceBindingRequest request)
			throws ServiceInstanceBindingExistsException,
			ServiceBrokerException {

		String bindingId = request.getBindingId();
		if (bindingId == null) {
			throw new ServiceBrokerException("no bindingId in request.");
		}

		ServiceInstanceBinding sib = repository.findOne(bindingId);
		if (sib != null) {
			throw new ServiceInstanceBindingExistsException(sib);
		}

		String serviceInstanceId = request.getServiceInstanceId();
		VrServiceInstance si = (VrServiceInstance) serviceInstanceService
				.getServiceInstance(serviceInstanceId);

		if (si == null) {
			throw new ServiceBrokerException("service instance for binding: "
					+ bindingId + " is missing.");
		}

		// not supposed to happen per the spec, but better check...
		if (si.isInProgress()) {
			throw new ServiceBrokerException(
					"ServiceInstance operation is still in progress.");
		}

		// do we have all the info we need to create credentials?
		if (!si.hasCredentials()) {
			vraClient.loadCredentials(si);
			serviceInstanceService.saveInstance(si);
		}

		ServiceInstanceBinding binding = new ServiceInstanceBinding(bindingId,
				serviceInstanceId, si.getCredentials(), null,
				request.getAppGuid());

		return repository.save(binding);
	}

	@Override
	public ServiceInstanceBinding deleteServiceInstanceBinding(
			DeleteServiceInstanceBindingRequest request)
			throws ServiceBrokerException {

		ServiceInstanceBinding binding = repository.findOne(request
				.getBindingId());

		if (binding == null) {
			throw new ServiceBrokerException("binding with id: "
					+ request.getBindingId() + " does not exist.");
		}

		repository.delete(binding);
		return binding;
	}
}
