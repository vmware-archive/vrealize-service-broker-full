package org.cloudfoundry.community.servicebroker.vrealize;

import java.util.Map;

import org.apache.log4j.Logger;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.cloudfoundry.community.servicebroker.model.Plan;
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

	public Catalog getAllCatalogItems(String token)
			throws ServiceBrokerException {
		try {
			return gson.fromJson(
					vraRepository.getAllCatalogItems("Bearer " + token),
					Catalog.class);
		} catch (FeignException e) {
			throw new ServiceBrokerException("error retrieving catalog.", e);
		}
	}

	// TODO point this to a working api endpoint
	public Catalog getEntitledCatalogItems(String token)
			throws ServiceBrokerException {
		// return gson.fromJson(
		// vraRepository.getEntitledCatalogItems("Bearer " + token),
		// Catalog.class);
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

	public ServiceDefinition getServiceDefinition(Catalog catalog,
			String serviceDefinitionId) throws ServiceBrokerException {

		for (ServiceDefinition sd : catalog.getServiceDefinitions()) {
			if (serviceDefinitionId.equals(sd.getId())) {
				return sd;
			}
		}
		throw new ServiceBrokerException("service definition: "
				+ serviceDefinitionId + " not found in catalog.");
	}

	public Plan getPlan(ServiceDefinition serviceDefinition, String planId)
			throws ServiceBrokerException {

		for (Plan p : serviceDefinition.getPlans()) {
			if (planId.equals(p.getId())) {
				return p;
			}
		}
		throw new ServiceBrokerException("plan: " + planId
				+ " not found in service definition.");
	}
}
