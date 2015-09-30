package org.cloudfoundry.community.servicebroker.vrealize;

import java.util.Map;

import org.apache.log4j.Logger;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import feign.FeignException;

@Component
public class TokenService {

	private static final Logger LOG = Logger.getLogger(TokenService.class);

	@Autowired
	Creds creds;

	@Autowired
	private VraRepository vraRepository;

	public String getToken() throws ServiceBrokerException {
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
}
