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
