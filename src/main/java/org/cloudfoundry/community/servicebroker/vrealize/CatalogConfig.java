package org.cloudfoundry.community.servicebroker.vrealize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.cloudfoundry.community.servicebroker.model.Plan;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CatalogConfig {

	@Bean
	public Catalog catalog() {
		Plan english = new Plan("english", "english",
				"English language hello plan",
				getPlanMetadata(), true);
		
		List<Plan> plans = new ArrayList<Plan>();
		plans.add(english);
		ServiceDefinition sd = new ServiceDefinition("hello", "hello-service",
				"A multi-language hello service", true, plans);
		
		sd.setMetadata(getServiceDefinitionMetadata());
		sd.setTags(getTags());
		sd.setPlanUpdateable(false);
		
		List<ServiceDefinition> sds = new ArrayList<ServiceDefinition>();
		sds.add(sd);
		
		return new Catalog(sds);
		
		// //English
		// return new Catalog(
		// Arrays.asList(new ServiceDefinition("hello", "hello-service",
		// "A multi-language hello service", true, false, Arrays
		// .asList(new Plan("english", "english",
		// "English language hello plan",
		// getPlanMetadata(), true)), getTags(),
		// getServiceDefinitionMetadata(), null, null)));
	}

	/* Used by Pivotal CF console */

	private Map<String, Object> getServiceDefinitionMetadata() {
		Map<String, Object> sdMetadata = new HashMap<String, Object>();
		sdMetadata.put("displayName", "hello");
		sdMetadata.put("longDescription", "Hello Service in English");
		sdMetadata.put("providerDisplayName", "Pivotal");
		return sdMetadata;
	}

	private Map<String, Object> getPlanMetadata() {
		Map<String, Object> planMetadata = new HashMap<String, Object>();
		planMetadata.put("costs", getCosts());
		planMetadata.put("bullets", getBullets());
		planMetadata.put("context", "en");
		return planMetadata;
	}
	
	private List<String> getTags() {
		List<String> l = new ArrayList<String>();
		l.add("hello");
		l.add("vRealize");
		return l;
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> getCosts() {
		Map<String, Object> costsMap = new HashMap<String, Object>();

		Map<String, Object> amount = new HashMap<String, Object>();
		amount.put("usd", new Double(0.0));

		costsMap.put("amount", amount);
		costsMap.put("unit", "MONTHLY");

		return Arrays.asList(costsMap);
	}

	private List<String> getBullets() {
		return Arrays.asList("Shared Hello server",
				"0 MB Storage (not enforced)",
				"10 concurrent connections (not enforced)");
	}

}