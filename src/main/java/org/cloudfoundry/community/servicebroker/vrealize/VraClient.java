package org.cloudfoundry.community.servicebroker.vrealize;

import java.util.Map;

import org.apache.log4j.Logger;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.springframework.beans.factory.annotation.Autowired;

import feign.FeignException;

public class VraClient {

	private static final Logger LOG = Logger.getLogger(VraClient.class);

	@Autowired
	private VraRepository vraRepository;

	// @Autowired
	// private VraCatalogRepository vraCatalogRepo;

	// @Autowired
	// Gson gson;

	// @Autowired
	// private String serviceUri;

	// public Catalog getCatalog() {
	// return gson.fromJson(
	// gson.fromJson(vraCatalogRepo.getCatalog(), JsonElement.class),
	// Catalog.class);
	// }

	public Map<String, Object> getAllCatalogItems(String token) {
		return vraRepository.getAllCatalogItems("Bearer " + token);
	}

	public Map<String, Object> getEntitledCatalogItems(String token) {
		return vraRepository.getEntitledCatalogItems("Bearer " + token);
	}

	public String getToken(Map<String, String> creds)
			throws ServiceBrokerException {
		if (creds == null || creds.size() < 3) {
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
}
