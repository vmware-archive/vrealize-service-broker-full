package org.cloudfoundry.community.servicebroker.vrealize.domain;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.community.servicebroker.model.Plan;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class PlanTranslator implements JsonDeserializer<Plan> {

	public Plan deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {

		return getPlan(json);
	}

	private Plan getPlan(JsonElement json) {
		JsonObject m2 = json.getAsJsonObject();
		String id = m2.get("catalogItemId").getAsString();
		String name = m2.get("name").getAsString();
		String description = m2.get("description").getAsString();
		return new Plan(id, name, description, getMetadata(m2, name), true);
	}

	private Map<String, Object> getCosts() {
		Map<String, Object> costsMap = new HashMap<String, Object>();
		Map<String, Object> amount = new HashMap<String, Object>();
		amount.put("usd", new Double(0.0));
		costsMap.put("amount", amount);
		costsMap.put("unit", "MONTHLY");
		List<Object> costs = new ArrayList<Object>();
		costs.add(costsMap);

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

		JsonArray o = json.get("links").getAsJsonArray();
		for (int i = 0; i < o.size(); i++) {
			JsonElement rel = o.get(i).getAsJsonObject().get("rel");
			JsonElement href = o.get(i).getAsJsonObject().get("href");
			planMetadata.put(rel.getAsString(), href.getAsString());
		}

		return planMetadata;
	}
}
