package org.cloudfoundry.community.servicebroker.vrealize;

import org.cloudfoundry.community.servicebroker.vrealize.domain.Creds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

@Configuration
@Profile("cloud")
public class CloudConfig {

	@Autowired
	private Environment env;

	@Bean
	Creds creds() {
		return new Creds(env.getProperty("VRA_USER_ID"),
				env.getProperty("VRA_USER_PASSWORD"),
				env.getProperty("VRA_TENANT"));
	}

	@Bean
	String serviceUri() {
		return env.getProperty("SERVICE_URI");
	}

}