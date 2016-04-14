package org.cloudfoundry.community.servicebroker.vrealize.adapter;

import com.jayway.jsonpath.DocumentContext;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.VrServiceInstance;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;

import java.util.HashMap;
import java.util.Map;

public class MySqlAdapter implements Adaptor {

    private static final String SERVICE_TYPE = "mysql";

    //username is hard-coded by the blueprint and can't be edited
    private static final String DB_ROOT_USERNAME = "root";
    public static final String DB_ROOT_PASSWORD = "DB_ROOT_PASSWORD";
    public static final String DB_NAME = "DB_NAME";
    public static final String DB_PORT = "DB_PORT";

    /**
     * Creates a uri based on metadata in the specified service instance
     * in the format DB-TYPE://USERNAME:PASSWORD@HOSTNAME:PORT/NAME
     */
    @Override
    public Map<String, Object> getCredentials(VrServiceInstance instance) {

        Object dbType = instance.getServiceType();
        Object host = instance.getHost();

        Object pw = instance.getParameters().get(DB_ROOT_PASSWORD);
        Object port = instance.getParameters().get(DB_PORT);
        Object dbId = instance.getParameters().get(DB_NAME);

        String s = dbType + "://" + DB_ROOT_USERNAME + ":" + pw + "@" + host + ":" + port + "/" + dbId;

        if (dbType == null || pw == null || host == null
                || port == null || dbId == null) {
            throw new ServiceBrokerException(
                    "unable to construct connection uri from ServiceInstance: " + s);
        }

        Map<String, Object> credentials = new HashMap<String, Object>();
        credentials.put("uri", s);

        return credentials;
    }

    public String getServiceType() {
        return SERVICE_TYPE;
    }

    public void prepareRequest(DocumentContext ctx, VrServiceInstance instance) {
        ctx.set("$.data.MYSQL_DATABASE.data." + DB_NAME, instance.getParameters().get(DB_NAME));
        ctx.set("$.data.MYSQL_DATABASE.data." + DB_PORT, instance.getParameters().get(DB_PORT));
        ctx.set("$.data.MYSQL_DATABASE.data." + DB_ROOT_PASSWORD, instance.getParameters().get(DB_ROOT_PASSWORD));
    }
}