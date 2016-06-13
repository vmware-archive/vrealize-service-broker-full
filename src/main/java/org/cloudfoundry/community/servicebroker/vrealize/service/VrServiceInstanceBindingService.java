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
import org.cloudfoundry.community.servicebroker.vrealize.VraClient;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.VrServiceInstance;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.VrServiceInstanceBinding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class VrServiceInstanceBindingService implements
        ServiceInstanceBindingService {

    private static final Logger LOG = Logger
            .getLogger(VrServiceInstanceBindingService.class);

    public static final String OBJECT_ID = "VrServiceInstanceBinding";

    @Autowired
    private VraClient vraClient;

    @Autowired
    VrServiceInstanceService serviceInstanceService;

    @Resource(name = "sibTemplate")
    private HashOperations<String, String, VrServiceInstanceBinding> repository;

    @Override
    public CreateServiceInstanceBindingResponse createServiceInstanceBinding(
            CreateServiceInstanceBindingRequest request)
            throws ServiceInstanceBindingExistsException,
            ServiceBrokerException {

        String bindingId = request.getBindingId();

        VrServiceInstanceBinding sib = repository.get(OBJECT_ID, bindingId);
        if (sib != null) {
            throw new ServiceInstanceBindingExistsException(request.getServiceInstanceId(), bindingId);
        }

        String serviceInstanceId = request.getServiceInstanceId();
        VrServiceInstance si = serviceInstanceService
                .getServiceInstance(serviceInstanceId);

        if (si == null) {
            throw new ServiceBrokerException("service instance for binding: "
                    + bindingId + " is missing.");
        }

        // not supposed to happen per the spec, but better check...
        if (si.isInProgress()) {
            throw new ServiceBrokerException(
                    "ServiceInstance operation is still in progress.");
        }

        LOG.info("creating binding for service instance: "
                + request.getServiceInstanceId() + " service: "
                + request.getServiceInstanceId());

        VrServiceInstanceBinding binding = new VrServiceInstanceBinding(bindingId,
                serviceInstanceId, si.getCredentials(), null,
                request.getBindResource());

        LOG.info("saving binding: " + binding.getId());

        repository.put(OBJECT_ID, binding.getId(), binding);

        return new CreateServiceInstanceAppBindingResponse().withCredentials(si.getCredentials());
    }

    @Override
    public void deleteServiceInstanceBinding(
            DeleteServiceInstanceBindingRequest request) {

        VrServiceInstanceBinding binding = repository.get(OBJECT_ID, request
                .getBindingId());

        if (binding == null) {
            throw new ServiceBrokerException("binding with id: "
                    + request.getBindingId() + " does not exist.");
        }

        LOG.info("deleting binding for service instance: "
                + request.getBindingId() + " service instance: "
                + request.getServiceInstanceId());

        repository.delete(OBJECT_ID, binding.getId());
    }
}
