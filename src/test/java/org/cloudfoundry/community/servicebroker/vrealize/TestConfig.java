package org.cloudfoundry.community.servicebroker.vrealize;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.slf4j.Slf4jLogger;

@Configuration
public class TestConfig {

	@Bean
	VrServiceInstanceService vrServiceInstanceService() {
		return new VrServiceInstanceService();
	}

	@Bean
	Creds creds() {
		return new Creds("vdude01@vra.lab", "P1v0t4l!", "LAB");
	}

	@Bean
	public VraRepository vraRepository() {
		return Feign.builder().encoder(new GsonEncoder())
				.decoder(new GsonDecoder()).logger(new Slf4jLogger())
				.target(VraRepository.class, "https://vra.vra.lab");
	}
}