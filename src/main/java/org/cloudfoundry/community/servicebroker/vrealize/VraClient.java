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

import org.apache.log4j.Logger;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.OperationState;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceLastOperation;
import org.cloudfoundry.community.servicebroker.vrealize.adapter.Adaptors;
import org.cloudfoundry.community.servicebroker.vrealize.domain.Creds;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.VrServiceInstance;
import org.cloudfoundry.community.servicebroker.vrealize.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

@Service
public class VraClient {

	private static final Logger LOG = Logger.getLogger(VraClient.class);

	public static final String SUCCESSFUL = "SUCCESSFUL";
	public static final String UNSUBMITTED = "UNSUBMITTED";
	public static final String SUBMITTED = "SUBMITTED";
	public static final String PENDING_PRE_APPROVAL = "PENDING_PRE_APPROVAL";
	public static final String IN_PROGRESS = "IN_PROGRESS";
	public static final String PENDING_POST_APPROVAL = "PENDING_POST_APPROVAL";

	@Autowired
	private VraRepository vraRepository;

	@Autowired
	Gson gson;

	@Autowired
	Creds creds;

	@Autowired
	String serviceUri;

	@Autowired
	TokenService tokenService;

	public VrServiceInstance createInstance(
			CreateServiceInstanceRequest request, ServiceDefinition sd)
			throws ServiceBrokerException {
		String token = tokenService.getToken();

		// get a template for the request
		JsonElement template = getCreateRequestTemplate(token, sd);

		// customize the template
		JsonElement edited = prepareCreateRequestTemplate(template,
				request.getServiceInstanceId());

		// request the request with the request
		ResponseEntity<JsonElement> response = postCreateRequest(token, edited,
				sd);

		LOG.info("service request response: " + response.toString());

		String location = getLocation(response);
		String requestId = getRequestId(response);

		VrServiceInstance instance = VrServiceInstance.create(request);
		instance.getMetadata().put(VrServiceInstance.LOCATION, location);
		instance.getMetadata().put(VrServiceInstance.CREATE_REQUEST_ID,
				requestId);

		ServiceInstanceLastOperation silo = new ServiceInstanceLastOperation(
				requestId, OperationState.IN_PROGRESS);
		instance.withLastOperation(silo);

		return instance;
	}

	public void loadCredentials(VrServiceInstance instance)
			throws ServiceBrokerException {
		String token = tokenService.getToken();

		String locationPath = pathFromLink(instance.getLocation().toString());
		JsonElement requestResponse = vraRepository.getRequest(
				"Bearer " + token, locationPath).getBody();

		instance.getParameters().putAll(
				getParametersFromCreateResponse(requestResponse));

		JsonElement resourcesResponse = getRequestResources(token, instance
				.getCreateRequestId().toString());

		instance.getParameters().putAll(
				getParametersFromResourceResponse(resourcesResponse));
	}

	VrServiceInstance loadDataFromResourceResponse(String token,
			VrServiceInstance instance) throws ServiceBrokerException {
		JsonElement resources = getRequestResources(token, instance
				.getCreateRequestId().toString());

		Map<String, String> links = getDeleteLinks(resources);
		instance.getMetadata().putAll(links);

		return instance;
	}

	public VrServiceInstance deleteInstance(VrServiceInstance instance)
			throws ServiceBrokerException {
		String token = tokenService.getToken();

		String deleteTemplateLink = instance.getMetadata()
				.get(VrServiceInstance.DELETE_TEMPLATE_LINK).toString();
		String deleteTemplatePath = pathFromLink(deleteTemplateLink);
		JsonElement template = vraRepository.getRequest("Bearer " + token,
				deleteTemplatePath).getBody();

		// customize the template
		JsonElement edited = prepareDeleteRequestTemplate(template,
				instance.getServiceInstanceId());

		String deleteLink = instance.getMetadata()
				.get(VrServiceInstance.DELETE_LINK).toString();
		String deletePath = pathFromLink(deleteLink);
		ResponseEntity<JsonElement> response = vraRepository.postRequest(
				"Bearer " + token, deletePath, edited);

		LOG.debug("service request response: " + response.toString());

		String requestId = getRequestId(response);

		// update instance with new delete metadata
		VrServiceInstance.delete(instance, requestId);
		return instance;
	}

	private String getLocation(ResponseEntity<JsonElement> response) {
		return response.getHeaders().getLocation().toString();
	}

	JsonElement getCreateRequestTemplate(String token, ServiceDefinition sd) {
		if (token == null || sd == null) {
			return null;
		}

		return vraRepository.getRequest("Bearer " + token,
				getGetRequestPath(sd)).getBody();
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

	ResponseEntity<JsonElement> postCreateRequest(String token,
			JsonElement body, ServiceDefinition sd) {

		if (token == null || body == null || sd == null) {
			return null;
		}

		return vraRepository.postRequest("Bearer " + token,
				getCreateRequestPath(sd), body);

	}

	JsonObject prepareCreateRequestTemplate(JsonElement template,
			String serviceInstanceId) throws ServiceBrokerException {
		JsonObject jo = removeFields(template.getAsJsonObject(),
				getContents("fieldsToFilter.txt"));

		LOG.info("submitting request to vR for serviceInstance: "
				+ serviceInstanceId);

		jo.addProperty("description", serviceInstanceId);
		jo.addProperty("reasons", "CF service broker request.");

		return jo;
	}

	JsonObject prepareDeleteRequestTemplate(JsonElement template,
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

	public ServiceInstanceLastOperation getRequestStatus(VrServiceInstance si)
			throws ServiceBrokerException {
		if (si == null || si.getServiceInstanceLastOperation() == null) {
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

		return getRequestStatus(tokenService.getToken(), requestId);
	}

	ServiceInstanceLastOperation getRequestStatus(String token, String requestId) {
		JsonElement je = vraRepository.getRequestStatus("Bearer " + token,
				requestId).getBody();
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

		if (SUCCESSFUL.equals(vrStatus)) {
			return OperationState.SUCCEEDED;
		}

		if (UNSUBMITTED.equals(vrStatus) || SUBMITTED.equals(vrStatus)
				|| PENDING_PRE_APPROVAL.equals(vrStatus)
				|| IN_PROGRESS.equals(vrStatus)
				|| PENDING_POST_APPROVAL.equals(vrStatus)) {
			return OperationState.IN_PROGRESS;
		}

		return OperationState.FAILED;

	}

	String getRequestId(ResponseEntity<JsonElement> requestResponse) {
		if (requestResponse == null) {
			return null;
		}

		JsonElement je = requestResponse.getBody().getAsJsonObject().get("id");
		if (je == null) {
			return null;
		}

		return je.getAsString();
	}

	private String getHostIP(JsonElement requestResponse) {
		if (requestResponse == null) {
			return null;
		}

		ReadContext ctx = JsonPath.parse(requestResponse.toString());
		Object o = ctx.read("$.content[*].data.ip_address[0]");

		return o.toString();
	}

	Map<String, Object> getParametersFromCreateResponse(JsonElement response)
			throws ServiceBrokerException {

		// get the custom parameters from the request info
		Map<String, Object> m = Adaptors
				.getParameters(getCustomValues(response));

		return m;
	}

	Map<String, Object> getParametersFromResourceResponse(
			JsonElement resourceViewResponse) throws ServiceBrokerException {

		Map<String, Object> m = new HashMap<String, Object>();

		// get the host from the resources view
		String host = getHostIP(resourceViewResponse);
		m.put(VrServiceInstance.HOST, host);

		return m;
	}

	private Map<String, Object> getCustomValues(JsonElement requestResponse) {
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

	private JsonElement getRequestResources(String token, String requestId) {
		return vraRepository.getRequestResources("Bearer " + token, requestId)
				.getBody();
	}

	private String pathFromLink(String link) {
		return link.substring(serviceUri.length() + 1);
	}

	Map<String, String> getDeleteLinks(JsonElement resources) {
		Map<String, String> map = new HashMap<String, String>();
		ReadContext ctx = JsonPath.parse(resources.toString());

		JSONArray o = ctx
				.read("$.content.[0].links[?(@.rel == 'GET Template: {com.vmware.csp.component.cafe.composition@resource.action.deployment.destroy.name}')].href");
		map.put(VrServiceInstance.DELETE_TEMPLATE_LINK, o.get(0).toString());

		o = ctx.read("$.content.[0].links[?(@.rel == 'POST: {com.vmware.csp.component.cafe.composition@resource.action.deployment.destroy.name}')].href");
		map.put(VrServiceInstance.DELETE_LINK, o.get(0).toString());

		return map;
	}
}
