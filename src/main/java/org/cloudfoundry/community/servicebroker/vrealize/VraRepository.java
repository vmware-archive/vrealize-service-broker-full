package org.cloudfoundry.community.servicebroker.vrealize;

import java.util.Map;

import org.springframework.stereotype.Repository;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

@Repository
public interface VraRepository {

	@Headers("Content-Type: application/json")
	@RequestLine("POST /identity/api/tokens")
	public Map<String, String> getToken(Map<String, String> creds);

	@RequestLine("HEAD /identity/api/tokens/{token}")
	public Map<String, String> checkToken(@Param("token") String token);

	@Headers({ "Content-Type: application/json", "Authorization: {token}" })
	@RequestLine("GET /catalog-service/api/consumer/entitledCatalogItems")
	public Map<String, Object> getCatalog(@Param("token") String token);

	@Headers({ "Content-Type: application/json", "Authorization: {token}" })
	@RequestLine("GET /service/api/consumer/entitledCatalogItems/{catalogId}/requests/template")
	public Map<String, Object> getCatalogItem(@Param("token") String token,
			@Param("catalogId") String catalogId);

}
