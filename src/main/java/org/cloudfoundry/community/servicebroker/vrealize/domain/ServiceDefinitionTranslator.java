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

package org.cloudfoundry.community.servicebroker.vrealize.domain;

import com.google.gson.*;
import org.springframework.cloud.servicebroker.model.Plan;
import org.springframework.cloud.servicebroker.model.ServiceDefinition;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceDefinitionTranslator implements
        JsonDeserializer<ServiceDefinition> {

    private Gson gson;

    public ServiceDefinition deserialize(JsonElement json, Type typeOfT,
                                         JsonDeserializationContext context) throws JsonParseException {
        return getSD(json);
    }

    public Gson getGson() {
        return gson;
    }

    public void setGson(Gson gson) {
        this.gson = gson;
    }

    private ServiceDefinition getSD(JsonElement json) {
        JsonObject jo = json.getAsJsonObject();

        String id = jo.get("catalogItemId").getAsString();
        String name = jo.get("name").getAsString();
        String description = jo.get("description").getAsString();

        List<Plan> plans = new ArrayList<Plan>();
        plans.add(gson.fromJson(jo, Plan.class));

        Map<String, Object> sdMetadata = new HashMap<String, Object>();
        sdMetadata.put("displayName", name);
        sdMetadata.put("longDescription", description);
        sdMetadata.put("providerDisplayName", "vRealize");

        List<String> tags = new ArrayList<String>();
        tags.add(name);
        tags.add("vRealize");

        return new ServiceDefinition(id, name, description,
                true, false, plans, tags, sdMetadata, null, null);
    }
}
