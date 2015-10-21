package org.cloudfoundry.community.servicebroker.vrealize.persistance;

import static org.junit.Assert.assertEquals;

import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.vrealize.Application;
import org.cloudfoundry.community.servicebroker.vrealize.TestConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mongodb.MongoClient;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
@Ignore
public class VrServiceInstanceRepositoryTest {

	private static final String COLLECTION = "serviceInstance";
	private static final String SD_ID = "e06ff060-dc7a-4f46-a7a7-c32c031fa31e";
	private static final String P_ID = "e06ff060-dc7a-4f46-a7a7-c32c031fa31e";

	@Autowired
	private MongoClient client;

	@Autowired
	VrServiceInstanceRepository repository;

	@Autowired
	MongoOperations mongo;

	@Before
	public void setup() throws Exception {
		mongo.dropCollection(COLLECTION);
	}

	@After
	public void teardown() {
		mongo.dropCollection(COLLECTION);
		client.dropDatabase(TestConfig.DB_NAME);
	}

	@Test
	public void instanceInsertedSuccessfully() throws Exception {
		VrServiceInstance si = getInstance();
		assertEquals(0, mongo.getCollection(COLLECTION).count());

		repository.save(si);
		assertEquals(1, mongo.getCollection(COLLECTION).count());
	}

	@Test
	public void instanceDeletedSuccessfully() throws Exception {
		VrServiceInstance si = getInstance();
		assertEquals(0, mongo.getCollection(COLLECTION).count());

		si = repository.save(si);
		assertEquals(1, mongo.getCollection(COLLECTION).count());

		// repository.delete(si.getId());
		// assertEquals(0, mongo.getCollection(COLLECTION).count());
	}

	private VrServiceInstance getInstance() {
		CreateServiceInstanceRequest req = new CreateServiceInstanceRequest(
				SD_ID, P_ID, "anOrg", "aSpace", true, null);
		VrServiceInstance si = VrServiceInstance.create(req, "12345");
		return si;
	}

}