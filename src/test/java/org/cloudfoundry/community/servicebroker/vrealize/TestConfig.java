package org.cloudfoundry.community.servicebroker.vrealize;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfig {

	@Bean
	VrServiceInstanceService vrServiceInstanceService() {
		return new VrServiceInstanceService();
	}
}