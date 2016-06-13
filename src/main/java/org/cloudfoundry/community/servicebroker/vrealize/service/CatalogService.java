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

import org.apache.log4j.Logger;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.Catalog;
import org.springframework.cloud.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.vrealize.VraClient;
import org.cloudfoundry.community.servicebroker.vrealize.VraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

@Service
public class CatalogService implements
        org.springframework.cloud.servicebroker.service.CatalogService {

    private static final Logger LOG = Logger.getLogger(CatalogService.class);

    @Autowired
    TokenService tokenService;

    @Autowired
    VraRepository vraRepository;

    @Autowired
    VraClient vraClient;

    @Autowired
    Gson gson;

    @Override
    public Catalog getCatalog() {
        try {
            String token = tokenService.getToken();
            return gson.fromJson(
                    vraRepository.getEntitledCatalogItems("Bearer " + token).getBody(),
                    Catalog.class);
        } catch (Exception e) {
            LOG.error("Error retrieving catalog.", e);
            throw new ServiceBrokerException("Unable to retrieve catalog.", e);
        }
    }

    @Override
    public ServiceDefinition getServiceDefinition(String id) {
        if (id == null) {
            return null;
        }

        for (ServiceDefinition sd : getCatalog().getServiceDefinitions()) {
            if (sd.getId().equals(id)) {
                return sd;
            }
        }
        return null;
    }

}
