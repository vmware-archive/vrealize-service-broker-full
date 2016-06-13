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

import com.google.gson.Gson;
import org.cloudfoundry.community.servicebroker.vrealize.Application;
import org.cloudfoundry.community.servicebroker.vrealize.TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.Catalog;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
public class CatalogServiceTest {

    @Autowired
    TokenService tokenService;

    @Autowired
    CatalogService catalogService;

    @Autowired
    Gson gson;

    @Test
    public void testGetEntitledCatalog() throws ServiceBrokerException {
        Catalog catalog = catalogService.getCatalog();
        assertNotNull(catalog);
        assertTrue(catalog.getServiceDefinitions().size() > 0);
    }

    @Test
    public void testGetEntitledCatalogItem() throws ServiceBrokerException {
        assertNull(catalogService.getServiceDefinition(null));
        assertNull(catalogService.getServiceDefinition(""));
        assertNotNull(catalogService.getServiceDefinition(TestConfig.SD_ID));
    }
}
