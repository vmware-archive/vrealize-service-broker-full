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

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.vrealize.VraRepository;
import org.cloudfoundry.community.servicebroker.vrealize.domain.Creds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import feign.FeignException;

@Component
public class TokenService {

    private static final Logger LOG = Logger.getLogger(TokenService.class);

    @Autowired
    Creds creds;

    @Autowired
    private VraRepository vraRepository;

    public String getToken() {
        try {
            ResponseEntity<Map<String, String>> m = vraRepository
                    .getToken(creds);
            if (!m.getStatusCode().equals(HttpStatus.OK)) {
                throw new ServiceBrokerException(m.getStatusCode().toString());
            }

            if (m.getBody().containsKey("id")) {
                return m.getBody().get("id");
            } else {
                throw new ServiceBrokerException(
                        "unable to get token from response.");
            }
        } catch (FeignException e) {
            LOG.error(e);
            throw new ServiceBrokerException("Unable to retrieve token.", e);
        }
    }
}
