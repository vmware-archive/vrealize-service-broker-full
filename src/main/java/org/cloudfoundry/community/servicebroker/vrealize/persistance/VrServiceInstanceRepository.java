package org.cloudfoundry.community.servicebroker.vrealize.persistance;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface VrServiceInstanceRepository extends
		MongoRepository<VrServiceInstance, String> {
}