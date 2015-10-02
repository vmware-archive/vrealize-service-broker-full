package org.cloudfoundry.community.servicebroker.vrealize.service;

import java.util.HashMap;
import java.util.Map;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.Plan;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceBindingService;
import org.cloudfoundry.community.servicebroker.vrealize.VraClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VrServiceInstanceBindingService implements
		ServiceInstanceBindingService {

	@Autowired
	private VraClient vraClient;

	private static final Map<String, ServiceInstanceBinding> BINDINGS = new HashMap<String, ServiceInstanceBinding>();

	@Override
	public ServiceInstanceBinding createServiceInstanceBinding(
			CreateServiceInstanceBindingRequest request)
			throws ServiceInstanceBindingExistsException,
			ServiceBrokerException {

		if (request == null) {
			throw new ServiceBrokerException("invalid binding request.");
		}

		if (request.getBindingId() != null
				&& BINDINGS.containsKey(request.getBindingId())) {
			throw new ServiceInstanceBindingExistsException(
					BINDINGS.get(request.getBindingId()));
		}

		/*
		 * get some info about the service from the service instance
		 * 
		 * TODO, speak with vR folks, what is returned when a vm is created? Can
		 * we get back some connection info?
		 * 
		 * add "custom properties" to the blueprints?
		 * 
		 * Then return these via "resourceData" on API calls.
		 * 
		 * Can it be generic depending on the blueprint? for mysql, need to
		 * create a connection string?
		 * 
		 * spring connector uses a URI connector for this.
		 * 
		 * spring.datasource.url=jdbc:mysql://localhost/test
		 * spring.datasource.username=dbuser spring.datasource.password=dbpass
		 * spring.datasource.driver-class-name=com.mysql.jdbc.Driver
		 */

		/*
		 * do something like this....
		 * 
		 * request.getServiceInstanceId();
		 * 
		 * String database = serviceInstance.getId(); String username =
		 * bindingId; // TODO Password Generator String password = "password";
		 * 
		 * // TODO check if user already exists in the DB
		 * 
		 * mongo.createUser(database, username, password);
		 * 
		 * Map<String,Object> credentials = new HashMap<String,Object>();
		 * credentials.put("uri", mongo.getConnectionString(database, username,
		 * password));
		 * 
		 * binding = new ServiceInstanceBinding(bindingId,
		 * serviceInstance.getId(), credentials, null, appGuid);
		 * repository.save(binding);
		 * 
		 * return binding;
		 * 
		 * // look at request and make sure we have a valid token //String token
		 * = vraClient.getToken(request).get("id").toString();
		 */

		return createBinding(request, "foo");
	}

	@Override
	public ServiceInstanceBinding deleteServiceInstanceBinding(
			DeleteServiceInstanceBindingRequest request)
			throws ServiceBrokerException {
		try {
			return deleteBinding(request);
		} catch (ServiceInstanceDoesNotExistException e) {
			throw new ServiceBrokerException(e);
		}
	}

	private ServiceInstanceBinding createBinding(
			CreateServiceInstanceBindingRequest request, String authToken)
			throws ServiceBrokerException,
			ServiceInstanceBindingExistsException {

		if (request == null || request.getServiceDefinitionId() == null
				|| request.getPlanId() == null
				|| request.getParameters() == null) {
			throw new ServiceBrokerException(
					"invalid CreateServiceInstanceBindingRequest object.");
		}

		if (authToken == null) {
			throw new ServiceBrokerException("missing token.");
		}
		//
		// if (!tokenValid(authToken)) {
		// throw new ServiceBrokerException("invalid or expired token.");
		// }

		ServiceInstanceBinding binding = BINDINGS.get(request.getBindingId());
		if (binding != null) {
			throw new ServiceInstanceBindingExistsException(binding);
		}

		ServiceDefinition serviceDefinition = null;
		Plan plan = null;

		// for (ServiceDefinition def :
		// vraClient.getCatalog().getServiceDefinitions()) {
		// if (def.getId().equals(request.getServiceDefinitionId())) {
		// serviceDefinition = def;
		// for (Plan p : def.getPlans()) {
		// if (p.getId().equals(request.getPlanId())) {
		// plan = p;
		// }
		// }
		// }
		// }

		if (serviceDefinition == null) {
			throw new ServiceBrokerException("service "
					+ request.getServiceDefinitionId()
					+ " not supported by this broker.");
		}

		if (plan == null) {
			throw new ServiceBrokerException("service plan "
					+ request.getPlanId() + " not supported by this broker.");
		}

		Map<String, Object> credentials = new HashMap<String, Object>();
		credentials.put("uri", "foo" + "/" + plan.getMetadata().get("context"));

		binding = new ServiceInstanceBinding(request.getBindingId(),
				serviceDefinition.getId(), credentials, null,
				request.getAppGuid());

		BINDINGS.put(binding.getId(), binding);

		return binding;
	}

	private ServiceInstanceBinding deleteBinding(
			DeleteServiceInstanceBindingRequest request)
			throws ServiceBrokerException, ServiceInstanceDoesNotExistException {

		if (request == null || request.getBindingId() == null) {
			throw new ServiceBrokerException(
					"invalid DeleteServiceInstanceRequest object.");
		}

		ServiceInstanceBinding i = getBinding(request.getBindingId());
		if (i == null) {
			throw new ServiceInstanceDoesNotExistException(
					request.getBindingId());
		}

		BINDINGS.remove(request.getBindingId());
		return i;
	}

	private ServiceInstanceBinding getBinding(String id) {
		if (id == null) {
			return null;
		}
		return BINDINGS.get(id);
	}
}
