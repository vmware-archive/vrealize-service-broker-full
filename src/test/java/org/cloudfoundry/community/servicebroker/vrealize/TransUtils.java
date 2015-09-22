package org.cloudfoundry.community.servicebroker.vrealize;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.community.servicebroker.model.Plan;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class TransUtils {

	static String getJson(String fileName) throws IOException {
		return new String(Files.readAllBytes(Paths.get(new ClassPathResource(
				fileName).getURI())));
	}

	public static List<ServiceDefinition> getSDs(Gson gson)
			throws JsonSyntaxException, IOException {
		Map<Object, Object> m = gson.fromJson(getJson("catalog.json"),
				TransUtils.mapType.getType());

		List<Object> content = (List<Object>) m.get("content");

		Map<String, Object> m2 = (Map<String, Object>) content.get(0);
		for (Object o : m2.keySet()) {
			System.out.println(o.getClass() + ": key " + o.toString()
					+ ": val " + m2.get(o));
		}

		String id = m2.get("catalogItemId").toString();
		String name = m2.get("name").toString();
		String description = m2.get("description").toString();

		Map<String, Object> costsMap = new HashMap<String, Object>();
		Map<String, Object> amount = new HashMap<String, Object>();
		amount.put("usd", new Double(0.0));
		costsMap.put("amount", amount);
		costsMap.put("unit", "MONTHLY");
		List<Object> costs = new ArrayList<Object>();
		costs.add(costsMap);

		List<String> bullets = new ArrayList<String>();
		bullets.add(name);
		bullets.add("0 MB Storage (not enforced)");
		bullets.add("10 concurrent connections (not enforced)");

		Map<String, Object> planMetadata = new HashMap<String, Object>();
		planMetadata.put("costs", costs);
		planMetadata.put("bullets", bullets);
		// planMetadata.put("context", "en");

		List<Map<String, String>> o = (List<Map<String, String>>) m2
				.get("links");
		for (int i = 0; i < o.size(); i++) {
			Map<String, String> m3 = o.get(i);
			planMetadata.putAll(m3);
		}

		// String id, String name, String description, Map<String,Object>
		// metadata, boolean free
		// System.out.println(content.get("catalogItemId"));
		Plan plan = new Plan(id, name, description, planMetadata, true);

		List<Plan> plans = new ArrayList<Plan>();
		plans.add(plan);

		// String id, String name, String description, boolean bindable,
		// List<Plan> plans
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

		List<ServiceDefinition> sds = new ArrayList<ServiceDefinition>();
		sds.add(sd);

		return sds;
		//
		// Catalog c = new Catalog(sds);
		// assertNotNull(c);
	}

	public static ParameterizedTypeReference<Map<Object, Object>> mapType = new ParameterizedTypeReference<Map<Object, Object>>() {
	};

}
