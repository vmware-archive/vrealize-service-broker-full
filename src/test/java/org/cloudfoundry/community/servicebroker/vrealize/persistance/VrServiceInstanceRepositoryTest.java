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

package org.cloudfoundry.community.servicebroker.vrealize.persistance;

import org.cloudfoundry.community.servicebroker.vrealize.Application;
import org.cloudfoundry.community.servicebroker.vrealize.TestConfig;
import org.cloudfoundry.community.servicebroker.vrealize.service.VrServiceInstanceService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.servicebroker.model.OperationState;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
public class VrServiceInstanceRepositoryTest {

    @Resource(name = "siTemplate")
    private HashOperations<String, String, VrServiceInstance> repository;

    @Before
    public void setup() {
        Set<String> keys = repository.keys(VrServiceInstanceService.OBJECT_ID);
        for (String key : keys) {
            repository.delete(VrServiceInstanceService.OBJECT_ID, key);
        }
    }

    @After
    public void teardown() {
        Set<String> keys = repository.keys(VrServiceInstanceService.OBJECT_ID);
        for (String key : keys) {
            repository.delete(VrServiceInstanceService.OBJECT_ID, key);
        }
    }

    @Test
    public void instanceInsertedSuccessfully() throws Exception {
        VrServiceInstance si = getInstance();
        assertEquals(0, repository.entries(VrServiceInstanceService.OBJECT_ID).size());

        repository.put(VrServiceInstanceService.OBJECT_ID, si.getId(), si);
        assertEquals(1, repository.entries(VrServiceInstanceService.OBJECT_ID).size());
    }

    @Test
    public void instanceDeletedSuccessfully() throws Exception {
        VrServiceInstance si = getInstance();
        assertEquals(0, repository.entries(VrServiceInstanceService.OBJECT_ID).size());

        repository.put(VrServiceInstanceService.OBJECT_ID, si.getId(), si);
        assertEquals(1, repository.entries(VrServiceInstanceService.OBJECT_ID).size());

        VrServiceInstance si2 = repository.get(VrServiceInstanceService.OBJECT_ID, si.getId());
        assertNotNull(si2);
        assertEquals("anID", si2.getId());

        VrServiceInstance si3 = repository.get(VrServiceInstanceService.OBJECT_ID, "anID");
        assertNotNull(si3);
        assertEquals("anID", si3.getId());
        assertEquals(TestConfig.SD_ID, si3.getServiceDefinitionId());
        assertNotNull(si3.getServiceInstanceLastOperation());
        assertEquals(OperationState.IN_PROGRESS, si3.getServiceInstanceLastOperation()
                .getState());

        //System.out.println(gson.toJson(si3));

        repository.delete(VrServiceInstanceService.OBJECT_ID, si3.getId());

        assertEquals(0, repository.entries(VrServiceInstanceService.OBJECT_ID).size());
    }

    private VrServiceInstance getInstance() {
        VrServiceInstance si = TestConfig.getServiceInstance();
        si.getMetadata().put(VrServiceInstance.CREATE_REQUEST_ID, "12345");
        si.getParameters().put(VrServiceInstance.HOST, "192.168.0.1");
        return si;
    }
}