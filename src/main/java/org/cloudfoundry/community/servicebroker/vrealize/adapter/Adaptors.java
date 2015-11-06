package org.cloudfoundry.community.servicebroker.vrealize.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.vrealize.Constants;
import org.cloudfoundry.community.servicebroker.vrealize.VraClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Adaptors {
	
	@Autowired
	VraClient client;

	private final List<Adaptor> adaptors = new ArrayList<Adaptor>();

	public Map<String, Object> getCredentials(ServiceInstance instance)
			throws ServiceBrokerException {
		return getAdaptor(instance).getCredentials(instance);
	}

	public Map<String, Object> getParameters(
			Map<String, Object> vrCustomKeyValues)
			throws ServiceBrokerException {
		Object serviceType = vrCustomKeyValues.get(
				Constants.SERVICE_TYPE);

		if (serviceType == null) {
			throw new ServiceBrokerException(
					"SERVICE_TYPE not found in vR response.");
		}

		Adaptor adaptor = getAdaptor(serviceType.toString());
		if (adaptor == null) {
			throw new ServiceBrokerException(
					"adaptor not found for service type: " + serviceType);
		}

		return adaptor.toParameters(vrCustomKeyValues);
	}

	private Adaptor getAdaptor(ServiceInstance instance)
			throws ServiceBrokerException {

		if (instance == null) {
			throw new ServiceBrokerException("invalid service instance.");
		}
		
		Map parms = client.getParameters(instance.getServiceInstanceId());

		Object type = parms.get(
				Constants.SERVICE_TYPE);

		if (type == null) {
			throw new ServiceBrokerException("service type not set.");
		}

		Adaptor adaptor = getAdaptor(type.toString());

		if (adaptor == null) {
			throw new ServiceBrokerException("adaptor for service type: "
					+ type + " not found.");
		}

		return adaptor;
	}

	private Adaptor getAdaptor(String type) {
		if (adaptors.isEmpty()) {
			initAdaptors();
		}

		for (Adaptor adaptor : adaptors) {
			if (adaptor.getServiceType().equals(type)) {
				return adaptor;
			}
		}
		return null;
	}

	// TODO figure out some clever way to make this more adaptable
	private void initAdaptors() {
		adaptors.add(new MySqlAdapter());
	}
}
