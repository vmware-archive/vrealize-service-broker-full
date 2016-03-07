package org.cloudfoundry.community.servicebroker.vrealize;

import com.google.gson.JsonElement;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.cloudfoundry.community.servicebroker.vrealize.domain.Creds;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface VraRepository {

    @Headers("Content-Type: application/json")
    @RequestLine("POST /identity/api/tokens")
    ResponseEntity<Map<String, String>> getToken(Creds creds);

    @Headers({"Content-Type: application/json", "Authorization: {token}"})
    @RequestLine("GET /catalog-service/api/consumer/entitledCatalogItemViews")
    ResponseEntity<JsonElement> getEntitledCatalogItems(
            @Param("token") String token);

    @Headers({"Content-Type: application/json", "Authorization: {token}"})
    @RequestLine("GET /{path}")
    ResponseEntity<JsonElement> getRequest(@Param("token") String token,
                                           @Param("path") String path);

    @Headers({"Content-Type: application/json", "Authorization: {token}"})
    @RequestLine("POST /{path}")
    ResponseEntity<JsonElement> postRequest(
            @Param("token") String token, @Param("path") String path,
            JsonElement request);

    @Headers({"Content-Type: application/json", "Authorization: {token}"})
    @RequestLine("GET /catalog-service/api/consumer/requests/{requestId}")
    ResponseEntity<JsonElement> getRequestStatus(
            @Param("token") String token, @Param("requestId") String requestId);

    @Headers({"Content-Type: application/json", "Authorization: {token}"})
    @RequestLine("GET /catalog-service/api/consumer/requests/{requestId}/resourceViews")
    ResponseEntity<JsonElement> getRequestResources(
            @Param("token") String token, @Param("requestId") String requestId);
}
