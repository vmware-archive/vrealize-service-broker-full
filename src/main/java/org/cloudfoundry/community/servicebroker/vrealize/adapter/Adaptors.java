package org.cloudfoundry.community.servicebroker.vrealize.adapter;

import org.cloudfoundry.community.servicebroker.vrealize.persistance.VrServiceInstance;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Adaptors {

    private static final List<Adaptor> ADAPTORS = new ArrayList<Adaptor>();

    public static Map<String, Object> getCredentials(VrServiceInstance instance)
            throws ServiceBrokerException {
        return getAdaptor(instance).getCredentials(instance);
    }

    public static Map<String, Object> getParameters(
            Map<String, Object> vrCustomKeyValues)
            throws ServiceBrokerException {
        Object serviceType = vrCustomKeyValues
                .get(VrServiceInstance.SERVICE_TYPE);

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

    private static Adaptor getAdaptor(VrServiceInstance instance)
            throws ServiceBrokerException {

        if (instance == null || instance.getParameters() == null) {
            throw new ServiceBrokerException("invalid service instance.");
        }

        Object type = instance.getParameters().get(
                VrServiceInstance.SERVICE_TYPE);

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

    private static Adaptor getAdaptor(String type) {
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

    public static boolean hasCredentials(VrServiceInstance instance) {
        if (instance == null
                || instance.getParameters() == null
                || instance.getParameters().get(VrServiceInstance.SERVICE_TYPE) == null) {
            return false;
        }
        Adaptor adaptor = getAdaptor(instance.getParameters()
                .get(VrServiceInstance.SERVICE_TYPE).toString());

        return adaptor != null && adaptor.hasCredentials(instance);
    }
}
