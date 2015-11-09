package org.cloudfoundry.community.servicebroker.vrealize.service;

import org.apache.log4j.Logger;
import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.vrealize.VraClient;
import org.cloudfoundry.community.servicebroker.vrealize.VraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

@Service
public class CatalogService implements
		org.cloudfoundry.community.servicebroker.service.CatalogService {

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
			return null;
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
