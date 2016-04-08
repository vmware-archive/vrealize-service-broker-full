package org.cloudfoundry.community.servicebroker.vrealize.adapter;

import org.cloudfoundry.community.servicebroker.vrealize.persistance.VrServiceInstance;

import java.util.Map;

interface Adaptor {

    Map<String, Object> getCredentials(VrServiceInstance instance);

    String getServiceType();

    Map<String, Object> toParameters(Map<String, Object> vrCustomKeyValues);

    boolean hasCredentials(VrServiceInstance instance);

}
