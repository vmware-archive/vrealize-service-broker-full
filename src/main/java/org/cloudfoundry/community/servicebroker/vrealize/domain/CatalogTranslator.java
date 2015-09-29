package org.cloudfoundry.community.servicebroker.vrealize.domain;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class CatalogTranslator implements JsonDeserializer<Catalog> {

	private Gson gson;

	public Catalog deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {

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
