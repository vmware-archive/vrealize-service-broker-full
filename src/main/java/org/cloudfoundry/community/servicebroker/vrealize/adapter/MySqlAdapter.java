package org.cloudfoundry.community.servicebroker.vrealize.adapter;

import org.cloudfoundry.community.servicebroker.vrealize.persistance.VrServiceInstance;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;

import java.util.HashMap;
import java.util.Map;

class MySqlAdapter implements Adaptor {

    private static final String SERVICE_TYPE = "mysql";

    /**
     * Creates a uri based on metadata in the specified service instance
     *
     * @param instance the instance
     * @return a uri in the format
     * DB-TYPE://USERNAME:PASSWORD@HOSTNAME:PORT/NAME
     * @throws ServiceBrokerException
     */
    @Override
    public Map<String, Object> getCredentials(VrServiceInstance instance) {

        Object dbType = instance.getParameters().get(
                VrServiceInstance.SERVICE_TYPE);
        Object userId = instance.getParameters().get(VrServiceInstance.USER_ID);
        Object pw = instance.getParameters().get(VrServiceInstance.PASSWORD);
        Object host = instance.getParameters().get(VrServiceInstance.HOST);
        Object port = instance.getParameters().get(VrServiceInstance.PORT);
        Object dbId = instance.getParameters().get(VrServiceInstance.DB_ID);

        if (dbType == null || userId == null || pw == null || host == null
                || port == null || dbId == null) {
            throw new ServiceBrokerException(
                    "unable to construct connection uri from ServiceInstance.");
        }

        String s = dbType + "://" + userId + ":" + pw + "@" + host + ":" + port + "/" + dbId;

        Map<String, Object> credentials = new HashMap<String, Object>();
        credentials.put("uri", s);

        return credentials;
    }

    public Map<String, Object> toParameters(
            Map<String, Object> vrCustomKeyValues) {
        Map<String, Object> m = new HashMap<String, Object>();

        m.put(VrServiceInstance.USER_ID, vrCustomKeyValues.get("mysql_user"));
        m.put(VrServiceInstance.PASSWORD, vrCustomKeyValues.get("mysql_passwd"));
        m.put(VrServiceInstance.DB_ID, vrCustomKeyValues.get("mysql_dbname"));
        m.put(VrServiceInstance.HOST, vrCustomKeyValues.get("foo"));
        m.put(VrServiceInstance.PORT, vrCustomKeyValues.get("mysql_port"));
        m.put(VrServiceInstance.SERVICE_TYPE, getServiceType());

        return m;
    }

    public String getServiceType() {
        return SERVICE_TYPE;
    }

    public boolean hasCredentials(VrServiceInstance instance) {
        try {
            getCredentials(instance);
            return true;
        } catch (ServiceBrokerException e) {
            return false;
        }
    }

}
