package org.cloudfoundry.community.servicebroker.vrealize;

import static org.junit.Assert.fail;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.vrealize.service.TokenService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
public class UseCaseTest {

	@Autowired
	private VraClient client;

	@Autowired
	TokenService tokenService;

	@Test
	public void testUseCase() throws ServiceBrokerException {
		// get a token

		// get the catalog

		// ask for a request template for an item in catalog

		// submit the request

		// poll for response

		// ask to bind to the service

		// unbind from the service

		// delete the service
		fail();
	}
}
