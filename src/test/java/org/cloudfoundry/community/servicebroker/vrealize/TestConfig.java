package org.cloudfoundry.community.servicebroker.vrealize;

import java.net.UnknownHostException;

import org.cloudfoundry.community.servicebroker.vrealize.domain.Creds;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.VrServiceInstanceRepository;
import org.cloudfoundry.community.servicebroker.vrealize.service.VrServiceInstanceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;

@Configuration
@EnableMongoRepositories(basePackages = "org.cloudfoundry.community.servicebroker.vrealize.persistance", includeFilters = @ComponentScan.Filter(value = { VrServiceInstanceRepository.class }, type = FilterType.ASSIGNABLE_TYPE))
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

	public static final String DB_NAME = "test-mongo-db";

	public @Bean MongoTemplate mongoTemplate(Mongo mongo)
			throws UnknownHostException {
		return new MongoTemplate(mongo, DB_NAME);
	}

	public @Bean MongoClient mongoClient() throws UnknownHostException {
		return new MongoClient("localhost");
	}

}