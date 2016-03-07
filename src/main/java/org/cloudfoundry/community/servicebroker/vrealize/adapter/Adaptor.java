package org.cloudfoundry.community.servicebroker.vrealize.adapter;

import org.cloudfoundry.community.servicebroker.vrealize.persistance.VrServiceInstance;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;

import java.util.Map;

public interface Adaptor {

    Map<String, Object> getCredentials(VrServiceInstance instance)
            throws ServiceBrokerException;

    String getServiceType();

    Map<String, Object> toParameters(Map<String, Object> vrCustomKeyValues);

    boolean hasCredentials(VrServiceInstance instance);

}
