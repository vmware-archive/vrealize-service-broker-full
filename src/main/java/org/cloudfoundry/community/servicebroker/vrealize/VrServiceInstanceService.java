package org.cloudfoundry.community.servicebroker.vrealize;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceExistsException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceUpdateNotSupportedException;
import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.DeleteServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.Plan;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.UpdateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VrServiceInstanceService implements ServiceInstanceService {

	private static final Logger LOG = Logger
			.getLogger(VrServiceInstanceService.class);

	@Autowired
	VraClient vraClient;

	@Autowired
	Catalog catalog;

	@Autowired
	Creds creds;

	private static final Map<String, ServiceInstance> INSTANCES = new HashMap<String, ServiceInstance>();

	@Override
	public ServiceInstance getServiceInstance(String id) {
		return getInstance(id);
	}

	@Override
	public ServiceInstance createServiceInstance(
			CreateServiceInstanceRequest request)
			throws ServiceInstanceExistsException, ServiceBrokerException {

		if (request == null || request.getServiceDefinitionId() == null) {
			throw new ServiceBrokerException(
					"invalid CreateServiceInstanceRequest object.");
		}

		if (request.getServiceInstanceId() != null
				&& getInstance(request.getServiceInstanceId()) != null) {
			throw new ServiceInstanceExistsException(INSTANCES.get(request
					.getServiceInstanceId()));
		}

		// find the service description in the catalog
		ServiceDefinition sd = vraClient.getServiceDefinition(catalog,
				request.getServiceDefinitionId());

		// find the plan in the sd
		Plan plan = vraClient.getPlan(sd, request.getPlanId());

		// submit request for service
		String id = sd.getId();
		String tenantRef = creds.getTenant();

		// TODO get from entitlement response
		// String subtenantRef = sd.getMetadata().get("groupId").toString();
		String subtenantRef = null;

		String bindingId = plan.getId();

		// create and register service instance
		String token = vraClient.getToken(creds);

		// TODO get some actual id from the vr response
		request.withServiceInstanceId(UUID.randomUUID().toString());

		// TODO submit and poll for response to request
		LOG.info("request submitted, catId: " + id + " tenant: " + tenantRef
				+ " groupId: " + subtenantRef + " planId: " + bindingId);

		ServiceInstance instance = new ServiceInstance(request);

		INSTANCES.put(request.getServiceInstanceId(), instance);

		return instance;
	}

	@Override
	public ServiceInstance deleteServiceInstance(
			DeleteServiceInstanceRequest request) throws ServiceBrokerException {

		if (request == null || request.getServiceInstanceId() == null) {
			throw new ServiceBrokerException(
					"invalid DeleteServiceInstanceRequest object.");
		}

		ServiceInstance i = getInstance(request.getServiceInstanceId());
		if (i == null) {
			throw new ServiceBrokerException(
					"Service instance does not exist: "
							+ request.getServiceInstanceId());
		}

		INSTANCES.remove(request.getServiceInstanceId());
		return i;
	}

	@Override
	public ServiceInstance updateServiceInstance(
			UpdateServiceInstanceRequest request)
			throws ServiceInstanceUpdateNotSupportedException,
			ServiceBrokerException, ServiceInstanceDoesNotExistException {

		// TODO not implemented yet
		return null;
	}

	private ServiceInstance getInstance(String id) {
		if (id == null) {
			return null;
		}
		return INSTANCES.get(id);
	}
}