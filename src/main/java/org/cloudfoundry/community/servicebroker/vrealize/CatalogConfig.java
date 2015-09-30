package org.cloudfoundry.community.servicebroker.vrealize;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.gson.Gson;

import feign.FeignException;

@Configuration
public class CatalogConfig {

	@Autowired
	TokenService tokenService;

	@Autowired
	private VraRepository vraRepository;

	@Autowired
	Gson gson;

	@Bean
	public Catalog catalog() throws ServiceBrokerException {
		String token = tokenService.getToken();

		try {
			return gson.fromJson(
					vraRepository.getAllCatalogItems("Bearer " + token),
					Catalog.class);
		} catch (FeignException e) {
			throw new ServiceBrokerException("error retrieving catalog.", e);
		}
	}
}