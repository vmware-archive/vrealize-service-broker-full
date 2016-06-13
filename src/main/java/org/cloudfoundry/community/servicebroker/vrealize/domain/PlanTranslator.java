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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlanTranslator implements JsonDeserializer<Plan> {

    public Plan deserialize(JsonElement json, Type typeOfT,
                            JsonDeserializationContext context) throws JsonParseException {

        return getPlan(json);
    }

    private Plan getPlan(JsonElement json) {
        JsonObject jo = json.getAsJsonObject();
        String id = jo.get("catalogItemId").getAsString();
        String name = jo.get("name").getAsString();
        String description = jo.get("description").getAsString();
        return new Plan(id, name, description, getMetadata(jo, name), true);
    }

    private Map<String, Object> getCosts() {
        Map<String, Object> costsMap = new HashMap<String, Object>();
        Map<String, Object> amount = new HashMap<String, Object>();
        amount.put("usd", 0.0D);
        costsMap.put("amount", amount);
        costsMap.put("unit", "MONTHLY");

        return costsMap;
    }

    private List<String> getBullets(String name) {
        List<String> bullets = new ArrayList<String>();
        bullets.add(name);
        bullets.add("0 MB Storage (not enforced)");
        bullets.add("10 concurrent connections (not enforced)");

        return bullets;
    }

    private Map<String, Object> getMetadata(JsonObject json, String name) {
        Map<String, Object> planMetadata = new HashMap<String, Object>();
        planMetadata.put("costs", getCosts());
        planMetadata.put("bullets", getBullets(name));

        JsonElement je = json.get("links");
        if (je != null) {
            JsonArray o = je.getAsJsonArray();
            for (int i = 0; i < o.size(); i++) {
                JsonElement rel = o.get(i).getAsJsonObject().get("rel");
                JsonElement href = o.get(i).getAsJsonObject().get("href");
                planMetadata.put(rel.getAsString(), href.getAsString());
            }
        }

        return planMetadata;
    }
}
