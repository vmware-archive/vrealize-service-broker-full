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

		// TODO assumes a mysql jdbc connection. Make this generic!
		Map<String, Object> credentials = new HashMap<String, Object>();
		credentials.put("uri", getConnectionString(si));

		ServiceInstanceBinding binding = new ServiceInstanceBinding(bindingId,
				serviceInstanceId, credentials, null, request.getAppGuid());

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

	// TODO generalize this!
	private String getConnectionString(VrServiceInstance si)
			throws ServiceBrokerException {

		// returns a string in the format:
		// DB-TYPE://USERNAME:PASSWORD@HOSTNAME:PORT/NAME
		Object dbType = si.getParameters().get(VrServiceInstance.SERVICE_TYPE);
		Object userId = si.getParameters().get(VrServiceInstance.USER_ID);
		Object pw = si.getParameters().get(VrServiceInstance.PASSWORD);
		Object host = si.getParameters().get(VrServiceInstance.HOST);
		Object port = si.getParameters().get(VrServiceInstance.PORT);
		Object dbId = si.getParameters().get(VrServiceInstance.DB_ID);

		if (dbType == null || userId == null || pw == null || host == null
				|| port == null || dbId == null) {
			throw new ServiceBrokerException(
					"unable to construct connection uri from ServiceInstance.");
		}

		StringBuffer sb = new StringBuffer();
		sb.append(dbType);
		sb.append("://");
		sb.append(userId);
		sb.append(":");
		sb.append(pw);
		sb.append("@");
		sb.append(host);
		sb.append(":");
		sb.append(port);
		sb.append("/");
		sb.append(dbId);

		return sb.toString();
	}
}
