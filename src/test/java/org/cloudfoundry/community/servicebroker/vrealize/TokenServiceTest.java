package org.cloudfoundry.community.servicebroker.vrealize;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
public class TokenServiceTest {

	@Autowired
	TokenService tokenService;

	@Test
	public void testGetToken() throws ServiceBrokerException {
		String s = tokenService.getToken();
		// System.out.println(s);
		assertNotNull(s);
	}

	@Ignore
	@Test
	public void testCheckToken() throws ServiceBrokerException {
		assertTrue(tokenService.checkToken(tokenService.getToken()));
		assertFalse(tokenService.checkToken("foo"));
	}
}
