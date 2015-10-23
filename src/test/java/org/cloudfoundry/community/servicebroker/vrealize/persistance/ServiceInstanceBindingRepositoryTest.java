package org.cloudfoundry.community.servicebroker.vrealize.persistance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.vrealize.Application;
import org.cloudfoundry.community.servicebroker.vrealize.TestConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.Gson;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
public class ServiceInstanceBindingRepositoryTest {

	@Autowired
	Gson gson;

	@Autowired
	ServiceInstanceBindingRepository repository;

	@Autowired
	VrServiceInstanceRepository siRepository;

	@Before
	public void setup() {
		repository.deleteAll();
		siRepository.deleteAll();
	}

	@After
	public void teardown() {
		repository.deleteAll();
		siRepository.deleteAll();
	}

	@Test
	public void instanceInsertedSuccessfully() throws Exception {
		ServiceInstanceBinding sib = TestConfig.getServiceInstanceBinding();
		assertEquals(0, repository.count());

		repository.save(sib);
		assertEquals(1, repository.count());

		// are service instances in the same collection?
		assertEquals(0, siRepository.count());
		siRepository.save(TestConfig.getServiceInstance());
		assertEquals(1, siRepository.count());
		assertEquals(1, repository.count());
	}

	@Test
	public void instanceDeletedSuccessfully() throws Exception {
		ServiceInstanceBinding sib = TestConfig.getServiceInstanceBinding();
		assertEquals(0, repository.count());

		sib = repository.save(sib);
		assertEquals(1, repository.count());

		List<ServiceInstanceBinding> l = repository.findAll();
		assertEquals(1, l.size());

		ServiceInstanceBinding sib2 = repository.findOne(sib.getId());
		assertNotNull(sib2);
		assertEquals("anID", sib2.getServiceInstanceId());

		ServiceInstanceBinding sib3 = repository.findOne("98765");
		assertNotNull(sib3);
		assertEquals("anID", sib3.getServiceInstanceId());
		assertEquals("98765", sib3.getId());
		assertNotNull(sib3.getCredentials());

		// System.out.println(gson.toJson(sib3));

		repository.delete(sib3.getId());

		assertEquals(0, repository.count());
	}
}