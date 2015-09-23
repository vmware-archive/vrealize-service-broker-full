package org.cloudfoundry.community.servicebroker.vrealize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.vmware.cloudclient.domain.Creds;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
public class VraRepositoryTest {

	@Autowired
	private VraRepository repo;

	@Autowired
	private VraCatalogRepo catRepo;

	@Test
	public void testGetToken() {
		Creds creds = new Creds();
		creds.Password = "secret";
		creds.Tenant = "tester";
		creds.Username = "chester";
		Map<String, String> resp = repo.getToken(creds);
		assertNotNull(resp.get("expires"));
		assertNotNull(resp.get("id"));
		assertEquals("tester", resp.get("tenant"));
	}

	@Test
	public void testCheckToken() {
		assertNull(repo.checkToken("token"));
	}

	@Test
	public void testGetCatalog() {
		String s = catRepo.getCatalog();
		assertNotNull(s);
		assertEquals("nsumerEnti", s.subSequence(70, 80));
	}
}
