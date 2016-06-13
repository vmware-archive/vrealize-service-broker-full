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
import org.cloudfoundry.community.servicebroker.vrealize.service.VrServiceInstanceBindingService;
import org.cloudfoundry.community.servicebroker.vrealize.service.VrServiceInstanceService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
public class ServiceInstanceBindingRepositoryTest {

    @Resource(name = "sibTemplate")
    private HashOperations<String, String, VrServiceInstanceBinding> repository;

    @Resource(name = "siTemplate")
    private HashOperations<String, String, VrServiceInstance> siRepository;

    @Before
    public void setup() {
        repository.delete(VrServiceInstanceBindingService.OBJECT_ID, repository.entries(VrServiceInstanceBindingService.OBJECT_ID));
        repository.delete(VrServiceInstanceService.OBJECT_ID, repository.entries(VrServiceInstanceService.OBJECT_ID));
    }

    @After
    public void teardown() {
        repository.delete(VrServiceInstanceBindingService.OBJECT_ID, repository.entries(VrServiceInstanceBindingService.OBJECT_ID));
        repository.delete(VrServiceInstanceService.OBJECT_ID, repository.entries(VrServiceInstanceService.OBJECT_ID));
    }

    @Test
    public void instanceInsertedSuccessfully() throws Exception {
        VrServiceInstanceBinding sib = TestConfig.getServiceInstanceBinding();
        assertEquals(0, repository.entries(VrServiceInstanceBindingService.OBJECT_ID).size());

        repository.put(VrServiceInstanceBindingService.OBJECT_ID, sib.getId(), sib);
        assertEquals(1, repository.entries(VrServiceInstanceBindingService.OBJECT_ID).size());
    }

    @Test
    public void instanceDeletedSuccessfully() throws Exception {
        VrServiceInstanceBinding sib = TestConfig.getServiceInstanceBinding();
        assertEquals(0, repository.entries(VrServiceInstanceBindingService.OBJECT_ID).size());

        repository.put(VrServiceInstanceBindingService.OBJECT_ID, sib.getId(), sib);
        assertEquals(1, repository.entries(VrServiceInstanceBindingService.OBJECT_ID).size());

        Map<String, VrServiceInstanceBinding> m = repository.entries(VrServiceInstanceBindingService.OBJECT_ID);
        assertEquals(1, m.size());

        VrServiceInstanceBinding sib2 = repository.get(VrServiceInstanceBindingService.OBJECT_ID, sib.getId());
        assertNotNull(sib2);
        assertEquals("anID", sib2.getServiceInstanceId());

        VrServiceInstanceBinding sib3 = repository.get(VrServiceInstanceBindingService.OBJECT_ID, "98765");
        assertNotNull(sib3);
        assertEquals("anID", sib3.getServiceInstanceId());
        assertEquals("98765", sib3.getId());
        assertNotNull(sib3.getCredentials());

        // System.out.println(gson.toJson(sib3));

        repository.delete(VrServiceInstanceBindingService.OBJECT_ID, sib3.getId());

        assertEquals(0, repository.entries(VrServiceInstanceBindingService.OBJECT_ID).size());
    }
}