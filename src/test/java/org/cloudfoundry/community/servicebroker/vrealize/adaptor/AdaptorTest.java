/**
 * vrealize-service-broker
 * <p>
 * Copyright (c) 2015-Present Pivotal Software, Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * limitations under the License.
 */

package org.cloudfoundry.community.servicebroker.vrealize.adaptor;

import org.cloudfoundry.community.servicebroker.vrealize.Application;
import org.cloudfoundry.community.servicebroker.vrealize.TestConfig;
import org.cloudfoundry.community.servicebroker.vrealize.adapter.Adaptors;
import org.cloudfoundry.community.servicebroker.vrealize.adapter.MySqlAdapter;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.VrServiceInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
public class AdaptorTest {

    @Test
    public void testGetCredentials() throws Exception {
        VrServiceInstance si = TestConfig.getServiceInstance();

        CreateServiceInstanceRequest request = request();
        for (String s : request.getParameters().keySet()) {
            si.getParameters().put(s, request.getParameters().get(s));
        }

        //add the service type and host in: this data comes later after the request completes
        si.getMetadata().put(VrServiceInstance.SERVICE_TYPE, "mysql");
        si.getMetadata().put(VrServiceInstance.HOST, "192.168.0.1");

        Map<String, Object> creds = Adaptors.getCredentials(si);
        assertNotNull(creds);
        assertEquals("mysql://root:tiger@192.168.0.1:1234/testDb",
                creds.get(VrServiceInstance.URI));

    }

    private CreateServiceInstanceRequest request() {
        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put(MySqlAdapter.DB_NAME, "testDb");
        parms.put(MySqlAdapter.DB_ROOT_PASSWORD, "tiger");
        parms.put(MySqlAdapter.DB_PORT, "1234");
        return new CreateServiceInstanceRequest(TestConfig.SD_ID, "Pid", "OrgId", "SpaceId", parms);
    }
}
