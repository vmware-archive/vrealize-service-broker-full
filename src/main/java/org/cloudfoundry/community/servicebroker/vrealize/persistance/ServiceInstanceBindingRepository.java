package org.cloudfoundry.community.servicebroker.vrealize.persistance;

import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ServiceInstanceBindingRepository extends
		MongoRepository<ServiceInstanceBinding, String> {
}