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
