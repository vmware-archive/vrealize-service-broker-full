package org.cloudfoundry.community.servicebroker.vrealize;

import java.util.Map;

import org.apache.log4j.Logger;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;

import feign.FeignException;

public class VraClient {

	private static final Logger LOG = Logger.getLogger(VraClient.class);

	@Autowired
	private VraRepository vraRepository;

	@Autowired
	Gson gson;

	public Catalog getAllCatalogItems(String token) {
		return gson.fromJson(
				vraRepository.getAllCatalogItems("Bearer " + token),
				Catalog.class);
	}

	//TODO point this to a working api endpoint
	public Catalog getEntitledCatalogItems(String token) {
//		return gson.fromJson(
//				vraRepository.getEntitledCatalogItems("Bearer " + token),
//				Catalog.class);
		return getAllCatalogItems(token);
	}

	public String getToken(Creds creds) throws ServiceBrokerException {
		if (creds == null) {
			throw new ServiceBrokerException("mising credentials.");
		}

		try {
			Map<String, String> m = vraRepository.getToken(creds);
			if (m.containsKey("id")) {
				return m.get("id");
			} else {
				throw new ServiceBrokerException(
						"unable to get token from response.");
			}
		} catch (FeignException e) {
			LOG.error(e);
			throw new ServiceBrokerException(e);
		}
	}

	public boolean checkToken(String token) {
		Map<String, String> resp = null;

		try {
			resp = vraRepository.checkToken(token);

		} catch (FeignException e) {
			LOG.warn(e);
			return false;
		}

		if (resp == null) {
			return false;
		}

		return true;
	}

	// public String getToken(CreateServiceInstanceBindingRequest request)
	// throws ServiceBrokerException {
	// if (request == null || request.getParameters() == null) {
	// throw new ServiceBrokerException("invalid request");
	// }
	//
	// // already have a token?
	// Object o = request.getParameters().get("token");
	// if (o != null) {
	// // check it
	// checkToken(o.toString());
	// return o.toString();
	// }
	//
	// // no existing token, try to get one.
	// Object c = request.getParameters().get("credentials");
	// if (c != null) {
	// String token = getToken((Creds) request.getParameters().get(
	// "credentials"));
	// request.getParameters().put("token", token);
	// request.getParameters().remove("credentials");
	// return token;
	//
	// }
	//
	// throw new ServiceBrokerException("missing credentials.");
	// }

	public ServiceDefinition getServiceDefinition(Catalog catalog,
			String serviceDefinitionId) {
		if (catalog == null || serviceDefinitionId == null) {
			return null;
		}

		for (ServiceDefinition sd : catalog.getServiceDefinitions()) {
			if (serviceDefinitionId.equals(sd.getId())) {
				return sd;
			}
		}
		return null;
	}
}
