package org.cloudfoundry.community.servicebroker.vrealize.service;

import org.apache.log4j.Logger;
import org.cloudfoundry.community.servicebroker.vrealize.VraClient;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.ServiceInstanceBindingRepository;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.VrServiceInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.springframework.cloud.servicebroker.model.*;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.stereotype.Service;

@Service
public class VrServiceInstanceBindingService implements
        ServiceInstanceBindingService {

    private static final Logger LOG = Logger
            .getLogger(VrServiceInstanceBindingService.class);

    @Autowired
    private VraClient vraClient;

    @Autowired
    VrServiceInstanceService serviceInstanceService;

    @Autowired
    ServiceInstanceBindingRepository repository;

    @Override
    public CreateServiceInstanceBindingResponse createServiceInstanceBinding(
            CreateServiceInstanceBindingRequest request)
            throws ServiceInstanceBindingExistsException,
            ServiceBrokerException {

        String bindingId = request.getBindingId();

        ServiceInstanceBinding sib = repository.findOne(bindingId);
        if (sib != null) {
            throw new ServiceInstanceBindingExistsException(request.getServiceInstanceId(), bindingId);
        }

        String serviceInstanceId = request.getServiceInstanceId();
        VrServiceInstance si = (VrServiceInstance) serviceInstanceService
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

        // do we have all the info we need to create credentials?
        if (!si.hasCredentials()) {
            vraClient.loadCredentials(si);
            serviceInstanceService.saveInstance(si);
        }

        ServiceInstanceBinding binding = new ServiceInstanceBinding(bindingId,
                serviceInstanceId, si.getCredentials(), null,
                request.getAppGuid());

        LOG.info("saving binding: " + binding.getId());

        repository.save(binding);

        return new CreateServiceInstanceAppBindingResponse().withCredentials(si.getCredentials());
    }

    @Override
    public void deleteServiceInstanceBinding(
            DeleteServiceInstanceBindingRequest request)
            throws ServiceBrokerException {

        ServiceInstanceBinding binding = repository.findOne(request
                .getBindingId());

        if (binding == null) {
            throw new ServiceBrokerException("binding with id: "
                    + request.getBindingId() + " does not exist.");
        }

        LOG.info("deleting binding for service instance: "
                + request.getBindingId() + " service instance: "
                + request.getServiceInstanceId());

        repository.delete(binding);
    }
}
