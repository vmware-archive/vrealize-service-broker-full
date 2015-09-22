package org.cloudfoundry.community.servicebroker.vrealize;

import org.cloudfoundry.community.servicebroker.model.BrokerApiVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;

@Configuration
public class Config {

	@Bean
	public VraClient vraClient() {
		return new VraClient();
	}

	@Bean
	public VraRepository vraRepository() {
		return Feign.builder().encoder(new GsonEncoder())
				.decoder(new GsonDecoder())
				.target(VraRepository.class, serviceUri());
	}

	@Bean
	public BrokerApiVersion brokerApiVersion() {
		return new BrokerApiVersion("2.4");
	}

	@Autowired
	private Environment env;

	@Bean
	public String serviceUri() {
		return env.getProperty("SERVICE_URI", "http://localhost:8080/hello");
	}

}