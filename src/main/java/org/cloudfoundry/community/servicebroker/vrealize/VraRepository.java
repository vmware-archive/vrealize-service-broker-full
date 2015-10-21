package org.cloudfoundry.community.servicebroker.vrealize;

import java.util.Map;

import org.cloudfoundry.community.servicebroker.vrealize.domain.Creds;

import com.google.gson.JsonElement;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface VraRepository {

	@Headers("Content-Type: application/json")
	@RequestLine("POST /identity/api/tokens")
	public Map<String, String> getToken(Creds creds);

	@RequestLine("HEAD /identity/api/tokens/{token}")
	public Map<String, String> checkToken(@Param("token") String token);

	@Headers({ "Content-Type: application/json", "Authorization: {token}" })
	@RequestLine("GET /catalog-service/api/consumer/entitledCatalogItemViews")
	public JsonElement getEntitledCatalogItems(@Param("token") String token);

	@Headers({ "Content-Type: application/json", "Authorization: {token}" })
	@RequestLine("GET /{path}")
	public JsonElement getRequest(@Param("token") String token,
			@Param("path") String path);

	@Headers({ "Content-Type: application/json", "Authorization: {token}" })
	@RequestLine("POST /{path}")
	public JsonElement postRequest(@Param("token") String token,
			@Param("path") String path, JsonElement request);

	@Headers({ "Content-Type: application/json", "Authorization: {token}" })
	@RequestLine("GET /catalog-service/api/consumer/requests/{requestId}")
	public JsonElement getRequestStatus(@Param("token") String token,
			@Param("requestId") String requestId);

	@Headers({ "Content-Type: application/json", "Authorization: {token}" })
	@RequestLine("GET /catalog-service/api/consumer/requests/{requestId}/resourceViews")
	public JsonElement getRequestResources(@Param("token") String token,
			@Param("requestId") String requestId);
}
