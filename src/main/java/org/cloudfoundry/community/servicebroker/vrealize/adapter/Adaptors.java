package org.cloudfoundry.community.servicebroker.vrealize.adapter;

import org.cloudfoundry.community.servicebroker.vrealize.persistance.VrServiceInstance;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Adaptors {

    private static final List<Adaptor> ADAPTORS = new ArrayList<Adaptor>();

    public static Map<String, Object> getCredentials(VrServiceInstance instance) {
        return getAdaptor(instance).getCredentials(instance);
    }

    private static Adaptor getAdaptor(VrServiceInstance instance) {
        if (instance == null || instance.getParameters() == null) {
            throw new ServiceBrokerException("invalid service instance.");
        }

        Object type = instance.getServiceType();

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

    public static Adaptor getAdaptor(String type) {
        if (ADAPTORS.isEmpty()) {
            initAdaptors();
        }

        for (Adaptor adaptor : ADAPTORS) {
            if (adaptor.getServiceType().equals(type)) {
                return adaptor;
            }
        }
        return null;
    }

    // TODO figure out some clever way to make this more adaptable
    private static void initAdaptors() {
        ADAPTORS.add(new MySqlAdapter());
    }

}
