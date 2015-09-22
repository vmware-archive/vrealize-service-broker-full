package org.cloudfoundry.community.servicebroker.vrealize;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.community.servicebroker.model.Plan;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

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
		JsonArray content = json.getAsJsonObject().get("content").getAsJsonArray();
		JsonObject m2 = content.get(0).getAsJsonObject();

		String name = m2.get("name").getAsString();
		String description = m2.get("description").getAsString();
		
		List<Plan> plans = new ArrayList<Plan>();
		plans.add( gson.fromJson(m2, Plan.class));

		ServiceDefinition sd = new ServiceDefinition(name, name, description,
				true, plans);

		Map<String, Object> sdMetadata = new HashMap<String, Object>();
		sdMetadata.put("displayName", name);
		sdMetadata.put("longDescription", description);
		sdMetadata.put("providerDisplayName", "vRealize");
		sd.setMetadata(sdMetadata);
		sd.setPlanUpdateable(false);

		List<String> tags = new ArrayList<String>();
		tags.add(name);
		tags.add("vRealize");
		sd.setTags(tags);

		return sd;
	}
}
