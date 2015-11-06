package org.cloudfoundry.community.servicebroker.vrealize;

import static org.junit.Assert.assertNotNull;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.vrealize.service.TokenService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
public class VraRepsitoryTest {

	@Autowired
	TokenService tokenService;

	@Autowired
	Gson gson;

	@Autowired
	VraRepository repo;
	
	@Autowired
	VraClient client;

	@Test
	public void testGetResources() throws ServiceBrokerException {
		String token = tokenService.getToken();
		assertNotNull(token);

		JsonElement template = client.getRequestResources(token, TestConfig.R_ID);
		assertNotNull(template);
		System.out.println(template);
	}
}
