package org.cloudfoundry.community.servicebroker.vrealize;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minidev.json.JSONArray;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.model.OperationState;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceLastOperation;
import org.cloudfoundry.community.servicebroker.vrealize.domain.Creds;
import org.cloudfoundry.community.servicebroker.vrealize.domain.VrServiceInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

@Service
public class VraClient {

	@Autowired
	private VraRepository vraRepository;

	@Autowired
	Gson gson;

	@Autowired
	Creds creds;

	@Autowired
	String serviceUri;

	// note: assumes that there is only 1 plan for a vR service definition
	public JsonElement getRequestTemplate(String token, ServiceDefinition sd) {
		if (token == null || sd == null) {
			return null;
		}

		return vraRepository.getRequest("Bearer " + token,
				getGetRequestPath(sd));
	}

	private String getGetRequestPath(ServiceDefinition sd) {
		Map<String, Object> meta = sd.getPlans().get(0).getMetadata();
		String fullUri = meta.get("GET: Request Template").toString();
		return fullUri.substring(serviceUri.length() + 1);
	}

	private String getPostRequestPath(ServiceDefinition sd) {
		Map<String, Object> meta = sd.getPlans().get(0).getMetadata();
		String fullUri = meta.get("POST: Submit Request").toString();
		return fullUri.substring(serviceUri.length() + 1);
	}

	public JsonElement postRequest(String token, JsonElement request,
			ServiceDefinition sd) {

		if (token == null || sd == null || sd == null) {
			return null;
		}

		return vraRepository.postRequest("Bearer " + token,
				getPostRequestPath(sd), request);

	}

	public JsonObject prepareRequestTemplate(JsonElement template,
			String serviceInstanceId) throws ServiceBrokerException {
		JsonObject jo = removeFields(template.getAsJsonObject(),
				getContents("fieldsToFilter.txt"));

		jo.addProperty("description", "PCF service broker request.");
		jo.addProperty("reasons", serviceInstanceId);

		return jo;
	}

	private JsonObject removeFields(JsonObject json, List<String> fields) {
		if (json == null || fields == null) {
			return null;
		}
		Set<Entry<String, JsonElement>> cs = new HashSet<Entry<String, JsonElement>>();
		cs.addAll(json.entrySet());
		Iterator<Entry<String, JsonElement>> i = cs.iterator();
		while (i.hasNext()) {
			Entry<String, JsonElement> entry = i.next();
			if (fields.contains(entry.getKey())) {
				json.remove(entry.getKey());
			} else {
				if (entry.getValue().isJsonObject()) {
					removeFields(entry.getValue().getAsJsonObject(), fields);
				}
			}
		}
		return json;
	}

	private List<String> getContents(String fileName)
			throws ServiceBrokerException {
		try {
			URI u = new ClassPathResource(fileName).getURI();
			return Files.readAllLines((Paths.get(u)));
		} catch (IOException e) {
			throw new ServiceBrokerException("error reading template.", e);
		}
	}

	public ServiceInstanceLastOperation getRequestStatus(String token,
			VrServiceInstance si) {
		if (token == null || si == null || si.getvRRequestId() == null) {
			return new ServiceInstanceLastOperation(
					"Unable to get request status: invalid request.",
					OperationState.FAILED);
		}

		JsonElement je = vraRepository.getRequestStatus("Bearer " + token,
				si.getvRRequestId());
		if (je == null) {
			return new ServiceInstanceLastOperation(
					"Unable to get request status: nothing returned from vR service.",
					OperationState.FAILED);
		}

		return getLastOperation(je);
	}

	ServiceInstanceLastOperation getLastOperation(JsonElement jsonElement) {
		JsonElement je = jsonElement.getAsJsonObject().get("state");
		if (je == null) {
			return new ServiceInstanceLastOperation(
					"Unable to determine state of request.",
					OperationState.FAILED);
		}

		String vRstatus = je.getAsString();
		if ("SUCCESSFUL".equals(vRstatus)) {
			return new ServiceInstanceLastOperation("Request succeeded.",
					OperationState.SUCCEEDED);
		}

		if ("UNSUBMITTED".equals(vRstatus) || "SUBMITTED".equals(vRstatus)
				|| "PENDING_PRE_APPROVAL".equals(vRstatus)
				|| "IN_PROGRESS".equals(vRstatus)
				|| "PENDING_POST_APPROVAL".equals(vRstatus)) {
			return new ServiceInstanceLastOperation("Request in progress: "
					+ vRstatus + ".", OperationState.IN_PROGRESS);
		}

		return new ServiceInstanceLastOperation("Request status: " + vRstatus,
				OperationState.FAILED);
	}

	public String getRequestId(JsonElement requestResponse) {
		if (requestResponse == null) {
			return null;
		}

		JsonElement je = requestResponse.getAsJsonObject().get("id");
		if (je == null) {
			return null;
		}

		return je.getAsString();
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getParameters(JsonElement requestResponse) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		if (requestResponse == null) {
			return parameters;
		}

		// There must be a better way....
		ReadContext ctx = JsonPath.parse(requestResponse.toString());
		JSONArray ja = ctx
				.read("$.requestData.entries[*].value.values.entries.value.items[?(@.classId == 'Infrastructure.CustomProperty')].values[*]");

		if (ja == null) {
			return parameters;
		}

		Type typeOfMapOfStringObject = new TypeToken<Map<String, Object>>() {
		}.getType();

		// avert your gaze....
		Iterator<Object> it = ja.iterator();
		while (it.hasNext()) {
			Map<String, Object> m = gson.fromJson(it.next().toString(),
					typeOfMapOfStringObject);
			List<Map<String, Object>> a = (List<Map<String, Object>>) m
					.get("entries");

			Object value = null;
			String key = null;
			for (Map<String, Object> o : a) {
				if (o.get("key").equals("value")) {
					value = ((Map<String, Object>) o.get("value")).get("value");
				}
				if (o.get("key").equals("id")) {
					key = ((Map<String, Object>) o.get("value")).get("value")
							.toString();
				}
			}
			parameters.put(key, value);
		}
		return parameters;
	}
}
