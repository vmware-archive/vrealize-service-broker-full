package org.cloudfoundry.community.servicebroker.vrealize;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CatalogConfig {

	@Autowired
	VraClient vraClient;

	@Autowired
	Creds creds;

	@Bean
	public Catalog catalog() throws ServiceBrokerException {
		return vraClient.getEntitledCatalogItems(vraClient.getToken(creds));
	}
}