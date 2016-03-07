package org.cloudfoundry.community.servicebroker.vrealize.service;

import org.cloudfoundry.community.servicebroker.vrealize.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
public class TokenServiceTest {

    @Autowired
    TokenService tokenService;

    @Test
    public void testGetToken() throws ServiceBrokerException {
        String s = tokenService.getToken();
        System.out.println(s);
        assertNotNull(s);
    }
}
