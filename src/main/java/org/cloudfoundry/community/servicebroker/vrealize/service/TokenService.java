package org.cloudfoundry.community.servicebroker.vrealize.service;

import java.util.Map;

import org.apache.log4j.Logger;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.vrealize.VraRepository;
import org.cloudfoundry.community.servicebroker.vrealize.domain.Creds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
			ResponseEntity<Map<String, String>> m = vraRepository
					.getToken(creds);
			if (!m.getStatusCode().equals(HttpStatus.OK)) {
				throw new ServiceBrokerException(m.getStatusCode().toString());
			}

			if (m.getBody().containsKey("id")) {
				return m.getBody().get("id");
			} else {
				throw new ServiceBrokerException(
						"unable to get token from response.");
			}
		} catch (FeignException e) {
			LOG.error(e);
			throw new ServiceBrokerException(e);
		}
	}
}
