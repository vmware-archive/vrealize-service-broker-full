package org.cloudfoundry.community.servicebroker.vrealize;

import org.cloudfoundry.community.servicebroker.vrealize.domain.Creds;
import org.cloudfoundry.community.servicebroker.vrealize.service.VrServiceInstanceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfig {

	@Bean
	VrServiceInstanceService vrServiceInstanceService() {
		return new VrServiceInstanceService();
	}

	@Bean
	Creds creds() {
		return new Creds("vdude01@vra.lab", "P1v0t4l!", "lab");
	}

	@Bean
	String serviceUri() {
		return "https://vra.vra.lab";
	}
}