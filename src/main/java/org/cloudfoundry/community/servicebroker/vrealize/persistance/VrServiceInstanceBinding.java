package org.cloudfoundry.community.servicebroker.vrealize.persistance;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jgordon on 3/11/16.
 */
public class VrServiceInstanceBinding implements Serializable {

    private String id;

    private String serviceInstanceId;

    private Map<String, Object> credentials = new HashMap<String, Object>();

    private String syslogDrainUrl;

    private Map<String, Object> bindResource;

    public VrServiceInstanceBinding(String id, String serviceInstanceId, Map<String, Object> credentials, String syslogDrainUrl, Map<String, Object> bindResource) {
        this.id = id;
        this.serviceInstanceId = serviceInstanceId;
        setCredentials(credentials);
        this.syslogDrainUrl = syslogDrainUrl;
        this.bindResource = bindResource;
    }

    public String getId() {
        return id;
    }

    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public Map<String, Object> getCredentials() {
        return credentials;
    }

    private void setCredentials(Map<String, Object> credentials) {
        if (credentials == null) {
            this.credentials = new HashMap<String, Object>();
        } else {
            this.credentials = credentials;
        }
    }

    public String getSyslogDrainUrl() {
        return syslogDrainUrl;
    }

    public Map<String, Object> getBindResource() {
        return bindResource;
    }
}
