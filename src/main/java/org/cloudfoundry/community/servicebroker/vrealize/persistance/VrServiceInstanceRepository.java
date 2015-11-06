package org.cloudfoundry.community.servicebroker.vrealize.persistance;

import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface VrServiceInstanceRepository extends
		MongoRepository<ServiceInstance, String> {
}