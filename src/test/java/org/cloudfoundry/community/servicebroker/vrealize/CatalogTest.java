package org.cloudfoundry.community.servicebroker.vrealize;

import static org.junit.Assert.assertNotNull;

import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
public class CatalogTest {

	@Test
	public void testCatalog() throws Exception {

		GsonBuilder builder = new GsonBuilder();
		CatalogTranslator catalogTranslator = new CatalogTranslator();
		builder.registerTypeAdapter(Catalog.class, catalogTranslator);
		
		// builder.registerTypeAdapter(Author.class, new AuthorTrnaslator());
		builder.setPrettyPrinting();
		Gson gson = builder.create();
		catalogTranslator.setGson(gson);
		
		JsonElement je = gson.fromJson(TransUtils.getJson("catalog.json"), JsonElement.class);
				
		Catalog c = gson.fromJson(je, Catalog.class);

//		Map<Object, Object> m = gson.fromJson(getJson("catalog.json"),
//				mapType.getType());
//		assertNotNull(m);
//
//		List<Object> content = (List<Object>) m.get("content");
//		assertNotNull(content);
//		assertNotNull(content.get(0));
//
//		Map<String, Object> m2 = (Map<String, Object>) content.get(0);
//		for (Object o : m2.keySet()) {
//			System.out.println(o.getClass() + ": key " + o.toString()
//					+ ": val " + m2.get(o));
//		}
//
//		String id = m2.get("catalogItemId").toString();
//		String name = m2.get("name").toString();
//		String description = m2.get("description").toString();
//
//		Map<String, Object> costsMap = new HashMap<String, Object>();
//		Map<String, Object> amount = new HashMap<String, Object>();
//		amount.put("usd", new Double(0.0));
//		costsMap.put("amount", amount);
//		costsMap.put("unit", "MONTHLY");
//		List<Object> costs = new ArrayList<Object>();
//		costs.add(costsMap);
//
//		List<String> bullets = new ArrayList<String>();
//		bullets.add(name);
//		bullets.add("0 MB Storage (not enforced)");
//		bullets.add("10 concurrent connections (not enforced)");
//
//		Map<String, Object> planMetadata = new HashMap<String, Object>();
//		planMetadata.put("costs", costs);
//		planMetadata.put("bullets", bullets);
////		planMetadata.put("context", "en");
//
//		List<Map<String, String>> o = (List<Map<String, String>>) m2
//				.get("links");
//		for (int i = 0; i < o.size(); i++) {
//			Map<String, String> m3 = o.get(i);
//			planMetadata.putAll(m3);
//		}
//
//		// String id, String name, String description, Map<String,Object>
//		// metadata, boolean free
//		// System.out.println(content.get("catalogItemId"));
//		Plan plan = new Plan(id, name, description, planMetadata, true);
//		assertNotNull(plan);
//
//		List<Plan> plans = new ArrayList<Plan>();
//		plans.add(plan);
//
//		// String id, String name, String description, boolean bindable,
//		// List<Plan> plans
//		ServiceDefinition sd = new ServiceDefinition(name, name, description,
//				true, plans);
//
//		Map<String, Object> sdMetadata = new HashMap<String, Object>();
//		sdMetadata.put("displayName", name);
//		sdMetadata.put("longDescription", description);
//		sdMetadata.put("providerDisplayName", "vRealize");
//		sd.setMetadata(sdMetadata);
//		sd.setPlanUpdateable(false);
//
//		List<String> tags = new ArrayList<String>();
//		tags.add(name);
//		tags.add("vRealize");
//		sd.setTags(tags);
//		
//		List<ServiceDefinition> sds = new ArrayList<ServiceDefinition>();
//		sds.add(sd);
//		
//		Catalog c = new Catalog(sds);
		assertNotNull(c);
		
		System.out.println(gson.toJson(c));

//		fail();
	}

}
