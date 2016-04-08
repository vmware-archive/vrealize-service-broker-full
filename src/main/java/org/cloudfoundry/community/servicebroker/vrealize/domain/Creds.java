package org.cloudfoundry.community.servicebroker.vrealize.domain;

public class Creds {

    String username;
    String password;
    String tenant;

    public Creds(String username, String password, String tenant) {
        super();
        this.username = username;
        this.password = password;
        this.tenant = tenant;
    }

}
