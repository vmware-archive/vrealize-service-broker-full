package org.cloudfoundry.community.servicebroker.vrealize.service;

import org.apache.log4j.Logger;
import org.cloudfoundry.community.servicebroker.vrealize.VraClient;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.LastOperation;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.VrServiceInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceExistsException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceUpdateNotSupportedException;
import org.springframework.cloud.servicebroker.model.*;
import org.springframework.cloud.servicebroker.service.ServiceInstanceService;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class VrServiceInstanceService implements ServiceInstanceService {

    private static final Logger LOG = Logger
            .getLogger(VrServiceInstanceService.class);

    public static final String OBJECT_ID = "VrServiceInstance";

    @Autowired
    VraClient vraClient;

    @Autowired
    CatalogService catalogService;

    @Resource(name = "siTemplate")
    private HashOperations<String, String, VrServiceInstance> repository;

    VrServiceInstance getServiceInstance(String id) {

        if (id == null || getInstance(id) == null) {
            LOG.warn("service instance with id: " + id + " not found!");
            return null;
        }

        VrServiceInstance instance = getInstance(id);

        // check the last operation
        LastOperation lo = instance
                .getServiceInstanceLastOperation();
        if (lo == null || lo.getState() == null) {
            LOG.error("ServiceInstance: " + id + " has no last operation.");
            deleteInstance(instance);
            return null;
        }

        // if the instance is not in progress just return it.
        if (!instance.isInProgress()) {
            return instance;
        }

        // if still in progress, let's check up on things...
        String currentRequestId = lo.getDescription();
        if (currentRequestId == null) {
            LOG.error("ServiceInstance: " + id + " last operation has no id.");
            deleteInstance(instance);
            return null;
        }

        OperationState state = instance.getServiceInstanceLastOperation().getState();
        LOG.info("service instance id: " + id + " request id: "
                + currentRequestId + " is in state: " + state);

        LOG.info("checking on status of request id: " + currentRequestId);
        GetLastServiceOperationResponse status;
        try {
            status = vraClient.getRequestStatus(instance);
            LOG.info("request: " + id + " status is: " + status.getState());
        } catch (ServiceBrokerException e) {
            LOG.error("unable to get status of request: " + id, e);
            return null;
        }

        instance.withLastOperation(LastOperation.fromResponse(status));

        // if this is a delete request and was successful, remove the instance
        if (instance.isCurrentOperationSuccessful()
                && instance.isCurrentOperationDelete()) {
            deleteInstance(instance);
        }

        // otherwise save the instance with the new last operation
        return saveInstance(instance);
    }

    @Override
    public CreateServiceInstanceResponse createServiceInstance(CreateServiceInstanceRequest request)
            throws ServiceInstanceExistsException, ServiceBrokerException {

        if (getInstance(request.getServiceInstanceId()) != null) {
            throw new ServiceInstanceExistsException(request.getServiceInstanceId(), request.getServiceDefinitionId());
        }

        ServiceDefinition sd = catalogService.getServiceDefinition(request
                .getServiceDefinitionId());

        if (sd == null) {
            throw new ServiceBrokerException(
                    "Unable to find service definition with id: "
                            + request.getServiceDefinitionId());
        }

        LOG.info("creating service instance: " + request.getServiceInstanceId()
                + " service definition: " + request.getServiceDefinitionId());

        VrServiceInstance instance = vraClient.createInstance(request, sd);

        instance = saveInstance(instance);

        LOG.info("registered service instance: "
                + instance.getId()
                + " requestId: "
                + instance.getMetadata().get(
                VrServiceInstance.CREATE_REQUEST_ID));

        return new CreateServiceInstanceResponse().withAsync(true);
    }

    @Override
    public GetLastServiceOperationResponse getLastOperation(GetLastServiceOperationRequest request) {
        VrServiceInstance si = getServiceInstance(request.getServiceInstanceId());
        if (si == null) {
            throw new ServiceInstanceDoesNotExistException(request.getServiceInstanceId());
        }

        return si.getServiceInstanceLastOperation().toResponse();
    }

    @Override
    public DeleteServiceInstanceResponse deleteServiceInstance(DeleteServiceInstanceRequest request) throws ServiceBrokerException {

        VrServiceInstance instance = getInstance(request.getServiceInstanceId());
        if (instance == null) {
            throw new ServiceBrokerException("Service instance: "
                    + request.getServiceInstanceId() + " not found.");
        }

        LOG.info("deleting service instance: " + request.getServiceInstanceId());

        instance = deleteInstance(instance);

        LOG.info("unregistering service instance: "
                + instance.getId()
                + " requestId: "
                + instance.getMetadata().get(
                VrServiceInstance.DELETE_REQUEST_ID));

        return new DeleteServiceInstanceResponse().withAsync(true);
    }

    @Override
    public UpdateServiceInstanceResponse updateServiceInstance(UpdateServiceInstanceRequest request) throws ServiceInstanceUpdateNotSupportedException,
            ServiceBrokerException, ServiceInstanceDoesNotExistException {

        throw new ServiceInstanceUpdateNotSupportedException(
                "vRealize services are not updatable.");
    }

    private VrServiceInstance getInstance(String id) {
        if (id == null) {
            return null;
        }
        return repository.get(OBJECT_ID, id);
    }

    VrServiceInstance deleteInstance(VrServiceInstance instance) {
        repository.delete(OBJECT_ID, instance.getId());
        return instance;
    }

    VrServiceInstance saveInstance(VrServiceInstance instance) {
        repository.put(OBJECT_ID, instance.getId(), instance);
        return instance;
    }
}