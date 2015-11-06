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
import java.util.UUID;

import net.minidev.json.JSONArray;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.model.OperationState;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceLastOperation;
import org.cloudfoundry.community.servicebroker.vrealize.adapter.Adaptors;
import org.cloudfoundry.community.servicebroker.vrealize.domain.Creds;
import org.jboss.logging.Logger;
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
	
	private static final Logger LOG = Logger.getLogger(VraClient.class);
	
	@Autowired
	private VraRepository vraRepository;

	@Autowired
	Gson gson;

	@Autowired
	Creds creds;

	@Autowired
	String serviceUri;

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
			String requestId) {

		if (token == null || body == null || requestId == null) {
			return null;
		}
		
		JsonElement requestResources = getRequestResources(token, requestId);
		String deleteLink = getDeleteLink(requestResources);
		return vraRepository.postRequest("Bearer " + token, pathFromLink(deleteLink), body);

//		return vraRepository.postRequest("Bearer " + token, si.getMetadata()
//				.get(Constants.DELETE_LINK).toString(), body);

	}

	public JsonObject prepareCreateRequestTemplate(JsonElement template) throws ServiceBrokerException {
		JsonObject jo = removeFields(template.getAsJsonObject(),
				getContents("fieldsToFilter.txt"));
		
		//create a unique id to tag the request so we can correlate it later.
		//we can't use the requestId here, because it has not been created yet.
		
		String id = UUID.randomUUID().toString();
		LOG.info("submitting request to vR with description: " + id);

		jo.addProperty("description", id);
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
			ServiceInstance si) {
		if (token == null || si == null
				|| si.getServiceInstanceLastOperation() == null) {
			return new ServiceInstanceLastOperation(
					"Unable to get request status: invalid request.",
					OperationState.FAILED);
		}

		String requestId = si.getServiceInstanceId();
		if (requestId == null) {
			return new ServiceInstanceLastOperation(
					"Unable to get requestId from last operation.",
					OperationState.FAILED);
		}

		return getRequestStatus(token, requestId);
	}

	public ServiceInstanceLastOperation getRequestStatus(String token,
			String requestId) {
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

		if (Constants.SUCCESSFUL.equals(vrStatus)) {
			return OperationState.SUCCEEDED;
		}

		if (Constants.UNSUBMITTED.equals(vrStatus) || Constants.SUBMITTED.equals(vrStatus)
				|| Constants.PENDING_PRE_APPROVAL.equals(vrStatus)
				|| Constants.IN_PROGRESS.equals(vrStatus)
				|| Constants.PENDING_POST_APPROVAL.equals(vrStatus)) {
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

	public Map<String, Object> getParameters(JsonElement requestResponse)
			throws ServiceBrokerException {
		return Adaptors.getParameters(getCustomValues(requestResponse));
	}

	public Map<String, Object> getCustomValues(JsonElement requestResponse) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		if (requestResponse == null) {
			return parameters;
		}

		ReadContext ctx = JsonPath.parse(requestResponse.toString());
		JSONArray ja = ctx
				.read("$.requestData.entries[*].value.values.entries[*]");

		if (ja == null) {
			return parameters;
		}

		for (int i = 0; i < ja.size(); i++) {
			String key = ctx
					.read("$.requestData.entries[*].value.values.entries[" + i
							+ "].key");

			Object value = ctx
					.read("$.requestData.entries[*].value.values.entries[" + i
							+ "].value.value");

			parameters.put(key, value);
		}
		return parameters;
	}

	public JsonElement getDeleteRequestTemplate(String token,
			String requestId) {
		if (token == null || requestId == null) {
			return null;
		}
		
		JsonElement requestResources = getRequestResources(token, requestId);
		String deleteTemplateLink = getDeleteTemplateLink(requestResources);
		return vraRepository.getRequest("Bearer " + token, pathFromLink(deleteTemplateLink));
	}

	public JsonElement getRequestResources(String token, String requestId) {
		return vraRepository.getRequestResources("Bearer " + token,
				requestId);
	}
	
	private String getDeleteTemplateLink(JsonElement requestResources) {
		ReadContext ctx = JsonPath.parse(requestResources.toString());
		return ctx.read("$.content.[0].links[?(@.rel == 'GET Template: {com.vmware.csp.component.iaas.proxy.provider@resource.action.name.virtual.Destroy}')].href.[0]").toString();
	}
	
	private String getDeleteLink(JsonElement requestResources) {
		ReadContext ctx = JsonPath.parse(requestResources.toString());
		return ctx.read("$.content.[0].links[?(@.rel == 'POST: {com.vmware.csp.component.iaas.proxy.provider@resource.action.name.virtual.Destroy}')].href.[0]").toString();
	}
	
	private String pathFromLink(String link) {
		return link.substring(serviceUri.length() + 1);
	}

	public Map<String, String> getDeleteLinks(JsonElement resources) {
		Map<String, String> map = new HashMap<String, String>();
		ReadContext ctx = JsonPath.parse(resources.toString());
		

		JSONArray o = ctx
				.read("$.content.[0].links[?(@.rel == 'GET Template: {com.vmware.csp.component.cafe.composition@resource.action.deployment.destroy.name}')].href");
		map.put(Constants.DELETE_TEMPLATE_LINK, o.get(0).toString());

		o = ctx.read("$.content.[0].links[?(@.rel == 'POST: {com.vmware.csp.component.cafe.composition@resource.action.deployment.destroy.name}')].href");
		map.put(Constants.DELETE_LINK, o.get(0).toString());

		return map;
	}

//	public ServiceInstance loadMetadata(String token,
//			ServiceInstance instance) {
//		JsonElement je = getRequestResources(token, instance);
//		Map<String, String> links = getDeleteLinks(je);
//		instance.getMetadata().putAll(links);
//
//		return instance;
//	}
}
