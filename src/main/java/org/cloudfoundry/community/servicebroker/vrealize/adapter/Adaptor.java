package org.cloudfoundry.community.servicebroker.vrealize.adapter;

import java.util.Map;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.VrServiceInstance;

public interface Adaptor {

	Map<String, Object> getCredentials(VrServiceInstance instance)
			throws ServiceBrokerException;

	String getServiceType();

	Map<String, Object> toParameters(Map<String, Object> vrCustomKeyValues);

}
