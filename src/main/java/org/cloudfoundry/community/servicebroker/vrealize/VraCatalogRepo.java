package org.cloudfoundry.community.servicebroker.vrealize;

import org.springframework.stereotype.Repository;

import feign.Headers;
import feign.RequestLine;

@Repository
public interface VraCatalogRepo {

	@Headers({ "Content-Type: application/json",
			"Authorization: Bearer {token}" })
	@RequestLine("GET /catalog-service/api/consumer/entitledCatalogItemViews")
	public String getCatalog();
}
