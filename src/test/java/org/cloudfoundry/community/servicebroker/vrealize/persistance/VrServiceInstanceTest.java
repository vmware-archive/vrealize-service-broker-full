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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.servicebroker.model.OperationState;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
public class VrServiceInstanceTest {

    @Test
    public void testStates() {
        VrServiceInstance si = TestConfig.getServiceInstance();

        assertTrue(si.isCurrentOperationCreate());
        assertFalse(si.isCurrentOperationDelete());
        assertFalse(si.isCurrentOperationSuccessful());
        assertTrue(si.isInProgress());
        assertEquals("aRequestId", si.getServiceInstanceLastOperation()
                .getDescription());

        // failed create request
        update(si, OperationState.FAILED);

        assertTrue(si.isCurrentOperationCreate());
        assertFalse(si.isCurrentOperationDelete());
        assertFalse(si.isCurrentOperationSuccessful());
        assertFalse(si.isInProgress());
        assertEquals("aRequestId", si.getServiceInstanceLastOperation()
                .getDescription());

        // succeeded create request
        update(si, OperationState.SUCCEEDED);

        assertTrue(si.isCurrentOperationCreate());
        assertFalse(si.isCurrentOperationDelete());
        assertTrue(si.isCurrentOperationSuccessful());
        assertFalse(si.isInProgress());
        assertEquals("aRequestId", si.getServiceInstanceLastOperation()
                .getDescription());

        // new delete request added to existing si
        si = delete(si, "23456");

        assertFalse(si.isCurrentOperationCreate());
        assertTrue(si.isCurrentOperationDelete());
        assertFalse(si.isCurrentOperationSuccessful());
        assertTrue(si.isInProgress());
        assertEquals("23456", si.getServiceInstanceLastOperation()
                .getDescription());
        assertEquals("23456",
                si.getMetadata().get(VrServiceInstance.DELETE_REQUEST_ID));
        assertEquals("aRequestId",
                si.getMetadata().get(VrServiceInstance.CREATE_REQUEST_ID));

        // delete failed
        si = update(si, OperationState.FAILED);

        assertFalse(si.isCurrentOperationCreate());
        assertTrue(si.isCurrentOperationDelete());
        assertFalse(si.isCurrentOperationSuccessful());
        assertFalse(si.isInProgress());
        assertEquals("23456", si.getServiceInstanceLastOperation()
                .getDescription());
        assertEquals("23456",
                si.getMetadata().get(VrServiceInstance.DELETE_REQUEST_ID));
        assertEquals("aRequestId",
                si.getMetadata().get(VrServiceInstance.CREATE_REQUEST_ID));

        // delete succeeded
        si = update(si, OperationState.SUCCEEDED);

        assertFalse(si.isCurrentOperationCreate());
        assertTrue(si.isCurrentOperationDelete());
        assertTrue(si.isCurrentOperationSuccessful());
        assertFalse(si.isInProgress());
        assertEquals("23456", si.getServiceInstanceLastOperation()
                .getDescription());
        assertEquals("23456",
                si.getMetadata().get(VrServiceInstance.DELETE_REQUEST_ID));
        assertEquals("aRequestId",
                si.getMetadata().get(VrServiceInstance.CREATE_REQUEST_ID));

        // System.out.println(gson.toJson(si));
    }

    private VrServiceInstance update(VrServiceInstance instance,
                                     OperationState state) {
        LastOperation lo = new LastOperation(state, instance.getServiceInstanceLastOperation().getDescription(), false);
        instance.withLastOperation(lo);
        return instance;
    }

    private VrServiceInstance delete(VrServiceInstance instance,
                                     String deleteRequestId) {
        instance.getMetadata().put(VrServiceInstance.DELETE_REQUEST_ID, deleteRequestId);
        LastOperation lo = new LastOperation(OperationState.IN_PROGRESS, deleteRequestId, true);
        instance.withLastOperation(lo);

        return instance;
    }
}
