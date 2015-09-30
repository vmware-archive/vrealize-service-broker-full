package org.cloudfoundry.community.servicebroker.vrealize;

import org.cloudfoundry.community.servicebroker.vrealize.domain.Creds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.slf4j.Slf4jLogger;

@Configuration
@Profile("cloud")
public class CloudConfig {

	@Bean
	public VraRepository vraRepository() {
		return Feign.builder().encoder(new GsonEncoder())
				.decoder(new GsonDecoder()).logger(new Slf4jLogger())
				.target(VraRepository.class, env.getProperty("SERVICE_URI"));
	}

	@Autowired
	private Environment env;

	@Bean
	Creds creds() {
		return new Creds(env.getProperty("VRA_USER_ID"),
				env.getProperty("VRA_USER_PASSWORD"),
				env.getProperty("VRA_TENANT"));
	}

}