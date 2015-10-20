package org.cloudfoundry.community.servicebroker.vrealize;

import java.io.IOException;
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

	// TODO: assumes that there is only 1 plan for a vR service definition
	public JsonElement getCreateRequestTemplate(String token,
			ServiceDefinition sd) {
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

	private String getCreateRequestPath(ServiceDefinition sd) {
		Map<String, Object> meta = sd.getPlans().get(0).getMetadata();
		String fullUri = meta.get("POST: Submit Request").toString();
		return fullUri.substring(serviceUri.length() + 1);
	}

	public JsonElement postCreateRequest(String token, JsonElement body,
			ServiceDefinition sd) {

		if (token == null || body == null || sd == null) {
			return null;
		}

		return vraRepository.postRequest("Bearer " + token,
				getCreateRequestPath(sd), body);

	}

	public JsonElement postDeleteRequest(String token, JsonElement body,
			VrServiceInstance si) {

		if (token == null || body == null || si == null) {
			return null;
		}

		return vraRepository.postRequest("Bearer " + token, si.getMetadata()
				.get(VrServiceInstance.MetatdataKeys.DELETE_LINK), body);

	}

	public JsonObject prepareCreateRequestTemplate(JsonElement template,
			String serviceInstanceId) throws ServiceBrokerException {
		JsonObject jo = removeFields(template.getAsJsonObject(),
				getContents("fieldsToFilter.txt"));

		jo.addProperty("description", serviceInstanceId);
		jo.addProperty("reasons", "CF service broker request.");

		return jo;
	}

	public JsonObject prepareDeleteRequestTemplate(JsonElement template,
			String serviceInstanceId) throws ServiceBrokerException {

		JsonObject jo = template.getAsJsonObject();
		jo.addProperty("description", serviceInstanceId);
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
		if (token == null || si == null
				|| si.getServiceInstanceLastOperation() == null) {
			return new ServiceInstanceLastOperation(
					"Unable to get request status: invalid request.",
					OperationState.FAILED);
		}

		String requestId = si.getCurrentOperationRequestId();
		if (requestId == null) {
			return new ServiceInstanceLastOperation(
					"Unable to get requestId from last operation.",
					OperationState.FAILED);
		}

		JsonElement je = vraRepository.getRequestStatus("Bearer " + token,
				requestId);
		if (je == null) {
			return new ServiceInstanceLastOperation(
					"Unable to get request status: nothing returned from vR service.",
					OperationState.FAILED);
		}

		return getLastOperation(je);
	}

	ServiceInstanceLastOperation getLastOperation(JsonElement jsonElement) {
		JsonObject jo = jsonElement.getAsJsonObject();
		JsonElement state = jo.get("state");
		JsonElement id = jo.get("id");

		if (id == null) {
			return new ServiceInstanceLastOperation(
					"Unable to determine id of request.", OperationState.FAILED);
		}

		if (state == null) {
			return new ServiceInstanceLastOperation(
					"Unable to determine id of request: " + id,
					OperationState.FAILED);
		}

		return new ServiceInstanceLastOperation(id.getAsString(),
				vrStatusToOperationState(state.getAsString()));
	}

	private OperationState vrStatusToOperationState(String vrStatus) {
		if (vrStatus == null) {
			return OperationState.FAILED;
		}

		if ("SUCCESSFUL".equals(vrStatus)) {
			return OperationState.SUCCEEDED;
		}

		if ("UNSUBMITTED".equals(vrStatus) || "SUBMITTED".equals(vrStatus)
				|| "PENDING_PRE_APPROVAL".equals(vrStatus)
				|| "IN_PROGRESS".equals(vrStatus)
				|| "PENDING_POST_APPROVAL".equals(vrStatus)) {
			return OperationState.IN_PROGRESS;
		}

		return OperationState.FAILED;

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

	public Map<Enum<VrServiceInstance.ParameterKeys>, Object> getParameters(
			JsonElement requestResponse) {
		Map<Enum<VrServiceInstance.ParameterKeys>, Object> m = new HashMap<Enum<VrServiceInstance.ParameterKeys>, Object>();
		Map<String, Object> keyValues = getCustomValues(requestResponse);

		// TODO: this works for mysql, make it generalized!
		m.put(VrServiceInstance.ParameterKeys.USER_ID,
				keyValues.get("mysql_user"));
		m.put(VrServiceInstance.ParameterKeys.PASSWORD,
				keyValues.get("mysql_passwd"));
		m.put(VrServiceInstance.ParameterKeys.DB_ID,
				keyValues.get("mysql_dbname"));
		m.put(VrServiceInstance.ParameterKeys.HOST_IP, keyValues.get("foo"));
		m.put(VrServiceInstance.ParameterKeys.PORT, keyValues.get("mysql_port"));

		return m;

	}

	private Map<String, Object> getCustomValues(JsonElement requestResponse) {
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

		for (int i = 0; i < ja.size(); i++) {
			String o = ctx
					.read("$.requestData.entries[*].value.values.entries.value.items[?(@.classId == 'Infrastructure.CustomProperty')].values["
							+ i + "].entries[*]").toString();
			System.out.println(o);

			ReadContext ctx2 = JsonPath.parse(o);
			Object value = ((JSONArray) ctx2
					.read("$.[?(@.key == 'value')].value.value")).get(0);
			String key = ((JSONArray) ctx2
					.read("$.[?(@.key == 'id')].value.value")).get(0)
					.toString();
			parameters.put(key, value);
		}
		return parameters;
	}

	public JsonElement getDeleteRequestTemplate(String token,
			VrServiceInstance si) {
		if (token == null || si == null) {
			return null;
		}

		String path = si.getMetadata()
				.get(VrServiceInstance.MetatdataKeys.DELETE_TEMPLATE_LINK)
				.substring(serviceUri.length() + 1);
		return vraRepository.getRequest("Bearer " + token, path);
	}

	public JsonElement getRequestResources(String token, VrServiceInstance si) {
		return vraRepository.getRequestResources("Bearer " + token,
				si.getCreateRequestId());
	}

	public Map<Enum<VrServiceInstance.MetatdataKeys>, String> getDeleteLinks(
			JsonElement resources) {
		Map<Enum<VrServiceInstance.MetatdataKeys>, String> map = new HashMap<Enum<VrServiceInstance.MetatdataKeys>, String>();
		ReadContext ctx = JsonPath.parse(resources.toString());

		JSONArray o = ctx
				.read("$.content.[0].links[?(@.rel == 'GET Template: {com.vmware.csp.component.cafe.composition@resource.action.deployment.destroy.name}')].href");
		map.put(VrServiceInstance.MetatdataKeys.DELETE_TEMPLATE_LINK, o.get(0)
				.toString());

		o = ctx.read("$.content.[0].links[?(@.rel == 'POST: {com.vmware.csp.component.cafe.composition@resource.action.deployment.destroy.name}')].href");
		map.put(VrServiceInstance.MetatdataKeys.DELETE_LINK, o.get(0)
				.toString());

		return map;
	}

	public void loadMetadata(String token, VrServiceInstance instance) {
		JsonElement je = getRequestResources(token, instance);
		Map<Enum<VrServiceInstance.MetatdataKeys>, String> links = getDeleteLinks(je);
		instance.getMetadata().putAll(links);
	}
}
