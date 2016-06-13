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
import org.apache.log4j.Logger;
import org.springframework.cloud.servicebroker.model.Catalog;
import org.springframework.cloud.servicebroker.model.ServiceDefinition;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CatalogTranslator implements JsonDeserializer<Catalog> {

    private static final Logger LOG = Logger.getLogger(CatalogTranslator.class);

    private Gson gson;

    public Catalog deserialize(JsonElement json, Type typeOfT,
                               JsonDeserializationContext context) throws JsonParseException {

        LOG.debug("processing catalog json: " + json);

        List<ServiceDefinition> sds = new ArrayList<ServiceDefinition>();
        JsonArray content = json.getAsJsonObject().get("content")
                .getAsJsonArray();
        for (int i = 0; i < content.size(); i++) {
            sds.add(gson.fromJson(content.get(i), ServiceDefinition.class));
        }
        return new Catalog(sds);
    }

    public Gson getGson() {
        return gson;
    }

    public void setGson(Gson gson) {
        this.gson = gson;
    }
}
