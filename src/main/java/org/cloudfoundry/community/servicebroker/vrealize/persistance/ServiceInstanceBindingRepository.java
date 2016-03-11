package org.cloudfoundry.community.servicebroker.vrealize.persistance;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ServiceInstanceBindingRepository extends
        MongoRepository<VrServiceInstanceBinding, String> {
}