package org.cloudfoundry.community.servicebroker.vrealize.adaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.cloudfoundry.community.servicebroker.vrealize.Application;
import org.cloudfoundry.community.servicebroker.vrealize.TestConfig;
import org.cloudfoundry.community.servicebroker.vrealize.adapter.Adaptors;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.VrServiceInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
public class AdaptorTest {

	@Test
	public void testGetCredentials() throws Exception {
		VrServiceInstance si = TestConfig.getServiceInstance();
		si.getParameters().put(VrServiceInstance.DB_ID, "testDb");
		si.getParameters().put(VrServiceInstance.HOST, "192.168.0.1");
		si.getParameters().put(VrServiceInstance.PASSWORD, "tiger");
		si.getParameters().put(VrServiceInstance.PORT, "1234");
		si.getParameters().put(VrServiceInstance.SERVICE_TYPE, "mysql");
		si.getParameters().put(VrServiceInstance.USER_ID, "scott");

		Map<String, Object> creds = Adaptors.getCredentials(si);
		assertNotNull(creds);
		assertEquals("mysql://scott:tiger@192.168.0.1:1234/testDb",
				creds.get(VrServiceInstance.URI));

	}
}
