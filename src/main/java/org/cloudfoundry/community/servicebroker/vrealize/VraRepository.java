package org.cloudfoundry.community.servicebroker.vrealize;

import java.util.Map;

import org.cloudfoundry.community.servicebroker.vrealize.domain.Creds;
import org.springframework.http.ResponseEntity;

import com.google.gson.JsonElement;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface VraRepository {

	@Headers("Content-Type: application/json")
	@RequestLine("POST /identity/api/tokens")
	public ResponseEntity<Map<String, String>> getToken(Creds creds);

	@Headers({ "Content-Type: application/json", "Authorization: {token}" })
	@RequestLine("GET /catalog-service/api/consumer/entitledCatalogItemViews")
	public ResponseEntity<JsonElement> getEntitledCatalogItems(
			@Param("token") String token);

	@Headers({ "Content-Type: application/json", "Authorization: {token}" })
	@RequestLine("GET /{path}")
	public ResponseEntity<JsonElement> getRequest(@Param("token") String token,
			@Param("path") String path);

	@Headers({ "Content-Type: application/json", "Authorization: {token}" })
	@RequestLine("POST /{path}")
	public ResponseEntity<JsonElement> postRequest(
			@Param("token") String token, @Param("path") String path,
			JsonElement request);

	@Headers({ "Content-Type: application/json", "Authorization: {token}" })
	@RequestLine("GET /catalog-service/api/consumer/requests/{requestId}")
	public ResponseEntity<JsonElement> getRequestStatus(
			@Param("token") String token, @Param("requestId") String requestId);

	@Headers({ "Content-Type: application/json", "Authorization: {token}" })
	@RequestLine("GET /catalog-service/api/consumer/requests/{requestId}/resourceViews")
	public ResponseEntity<JsonElement> getRequestResources(
			@Param("token") String token, @Param("requestId") String requestId);
}
