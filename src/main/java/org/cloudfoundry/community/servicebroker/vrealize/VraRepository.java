/**
 * vrealize-service-broker
 * <p>
 * Copyright (c) 2015-Present Pivotal Software, Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * limitations under the License.
 */

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
