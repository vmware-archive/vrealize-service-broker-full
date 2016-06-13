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

package org.cloudfoundry.community.servicebroker.vrealize.service;

import org.cloudfoundry.community.servicebroker.vrealize.Application;
import org.cloudfoundry.community.servicebroker.vrealize.TestConfig;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.LastOperation;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.VrServiceInstance;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.VrServiceInstanceBinding;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.springframework.cloud.servicebroker.model.OperationState;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
public class VrServiceInstanceBindingServiceTest {

    @Autowired
    @InjectMocks
    VrServiceInstanceBindingService vrServiceInstanceBindingService;

    @Mock
    VrServiceInstanceService vrServiceInstanceService;

    @Resource(name = "sibTemplate")
    private HashOperations<String, String, VrServiceInstanceBinding> repo;

    @Autowired
    @Resource(name = "sibTemplate")
    private RedisTemplate<String, VrServiceInstanceBinding> template;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        VrServiceInstance si = TestConfig.getServiceInstance();
        LastOperation lo = new LastOperation(OperationState.SUCCEEDED, "anOp", false);
        si.withLastOperation(lo);

        when(vrServiceInstanceService.getServiceInstance(Matchers.anyString()))
                .thenReturn(si);

        when(
                vrServiceInstanceService
                        .saveInstance(any(VrServiceInstance.class)))
                .thenReturn(si);

        when(
                vrServiceInstanceService
                        .deleteInstance(any(VrServiceInstance.class)))
                .thenReturn(si);

        Set<String> keys = repo.keys(VrServiceInstanceBindingService.OBJECT_ID);
        for (String key : keys) {
            repo.delete(VrServiceInstanceBindingService.OBJECT_ID, key);
        }
    }

    @After
    public void cleanUp() throws Exception {
        Set<String> keys = repo.keys(VrServiceInstanceBindingService.OBJECT_ID);
        for (String key : keys) {
            repo.delete(VrServiceInstanceBindingService.OBJECT_ID, key);
        }
    }

    @Test
    public void testBinding() throws ServiceBrokerException,
            ServiceInstanceBindingExistsException {

        VrServiceInstanceBinding b = TestConfig.getServiceInstanceBinding();
        assertNotNull(b);
        Map<String, Object> m = b.getCredentials();
        assertNotNull(m);
        assertEquals("mysql://root:secret@aHost:1234/aDB",
                m.get(VrServiceInstance.URI));
        assertNotNull(b.getId());
        assertEquals("anID", b.getServiceInstanceId());
    }
}
