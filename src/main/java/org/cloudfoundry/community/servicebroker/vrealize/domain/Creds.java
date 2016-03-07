package org.cloudfoundry.community.servicebroker.vrealize.domain;

import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;

import java.util.HashMap;
import java.util.Map;

public class Creds {

    private String username;
    private String password;
    private String tenant;

    public Creds(String username, String password, String tenant) {
        super();
        setUsername(username);
        setPassword(password);
        setTenant(tenant);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("username", getUsername());
        map.put("password", getPassword());
        map.put("tenant", getTenant());

        return map;
    }

    public static Creds fromMap(Map<String, Object> map)
            throws ServiceBrokerException {

        if (map == null || !map.containsKey("tenant")
                || !map.containsKey("username") || !map.containsKey("password")) {
            throw new ServiceBrokerException("missing vR credentials.");
        }

        return new Creds(map.get("username").toString(), map.get("password")
                .toString(), map.get("tenant").toString());
    }

}
