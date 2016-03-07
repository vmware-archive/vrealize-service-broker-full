package org.cloudfoundry.community.servicebroker.vrealize.persistance;

import com.google.gson.Gson;
import org.cloudfoundry.community.servicebroker.vrealize.Application;
import org.cloudfoundry.community.servicebroker.vrealize.TestConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.servicebroker.model.OperationState;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
public class VrServiceInstanceRepositoryTest {

    @Autowired
    Gson gson;

    @Autowired
    VrServiceInstanceRepository repository;

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
        assertEquals("anID", si2.getServiceInstanceId());

        VrServiceInstance si3 = repository.findOne("anID");
        assertNotNull(si3);
        assertEquals("anID", si3.getServiceInstanceId());
        assertEquals(TestConfig.SD_ID, si3.getServiceDefinitionId());
        assertNotNull(si3.getServiceInstanceLastOperation());
        assertEquals(OperationState.IN_PROGRESS, si3.getServiceInstanceLastOperation()
                .getState());

        //System.out.println(gson.toJson(si3));

        repository.delete(si3.getId());

        assertEquals(0, repository.count());
    }

    private VrServiceInstance getInstance() {
        VrServiceInstance si = TestConfig.getServiceInstance();
        si.getMetadata().put(VrServiceInstance.CREATE_REQUEST_ID, "12345");
        si.getParameters().put(VrServiceInstance.HOST, "192.168.0.1");
        return si;
    }
}