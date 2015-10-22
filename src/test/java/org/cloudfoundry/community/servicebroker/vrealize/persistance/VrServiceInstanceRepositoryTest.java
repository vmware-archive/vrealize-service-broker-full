package org.cloudfoundry.community.servicebroker.vrealize.persistance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.vrealize.Application;
import org.cloudfoundry.community.servicebroker.vrealize.TestConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.Gson;
import com.mongodb.MongoClient;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
public class VrServiceInstanceRepositoryTest {

	@Autowired
	Gson gson;

	@Autowired
	private MongoClient client;

	@Autowired
	VrServiceInstanceRepository repository;

	@Autowired
	MongoOperations mongo;

	@Before
	public void setup() {
		repository.deleteAll();
	}

	@After
	public void teardown() {
		repository.deleteAll();
	}

	@Test
	public void instanceInsertedSuccessfully() throws Exception {
		VrServiceInstance si = getInstance();
		assertEquals(0, repository.count());

		repository.save(si);
		assertEquals(1, repository.count());
	}

	@Test
	public void instanceDeletedSuccessfully() throws Exception {
		VrServiceInstance si = getInstance();
		assertEquals(0, repository.count());

		si = repository.save(si);
		assertEquals(1, repository.count());

		List<VrServiceInstance> l = repository.findAll();
		assertEquals(1, l.size());

		VrServiceInstance si2 = repository.findOne(si.getId());
		assertNotNull(si2);
		assertEquals("98765", si2.getServiceInstanceId());

		VrServiceInstance si3 = repository.findOne("98765");
		assertNotNull(si3);
		assertEquals("98765", si3.getServiceInstanceId());
		assertEquals(TestConfig.SD_ID, si3.getServiceDefinitionId());
		assertNotNull(si3.getServiceInstanceLastOperation());
		assertEquals("in progress", si3.getServiceInstanceLastOperation()
				.getState());

		System.out.println(gson.toJson(si3));

		repository.delete(si3.getId());

		assertEquals(0, repository.count());
	}

	private VrServiceInstance getInstance() {
		CreateServiceInstanceRequest req = new CreateServiceInstanceRequest(
				TestConfig.SD_ID, TestConfig.P_ID, "anOrg", "aSpace", true,
				null);
		req.withServiceInstanceId("98765");
		VrServiceInstance si = VrServiceInstance.create(req, "12345");
		si.getMetadata().put(VrServiceInstance.CREATE_REQUEST_ID, "12345");
		si.getParameters().put(VrServiceInstance.HOST, "192.168.0.1");
		return si;
	}
}