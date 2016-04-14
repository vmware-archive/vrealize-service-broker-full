package org.cloudfoundry.community.servicebroker.vrealize.adapter;

import com.jayway.jsonpath.DocumentContext;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.VrServiceInstance;

import java.util.Map;

public interface Adaptor {

    Map<String, Object> getCredentials(VrServiceInstance instance);

    String getServiceType();

    void prepareRequest(DocumentContext ctx, VrServiceInstance instance);

}
