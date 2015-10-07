package org.cloudfoundry.community.servicebroker.vrealize;

import java.util.Map;

import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.vrealize.domain.Creds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

@Service
public class VraClient {

	@Autowired
	private VraRepository vraRepository;

	@Autowired
	Gson gson;

	@Autowired
	Creds creds;

	@Autowired
	Catalog catalog;

	@Autowired
	String serviceUri;

	public ServiceDefinition getEntitledCatalogItem(String id) {
		if (id == null) {
			return null;
		}

		for (ServiceDefinition sd : catalog.getServiceDefinitions()) {
			if (sd.getId().equals(id)) {
				return sd;
			}
		}
		return null;
	}

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
		String fullUri = meta.get("POST: Request Template").toString();
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

	// private ServiceDefinition getServiceDefinition(String
	// serviceDefinitionId)
	// throws ServiceBrokerException {
	//
	// for (ServiceDefinition sd : catalog.getServiceDefinitions()) {
	// if (serviceDefinitionId.equals(sd.getId())) {
	// return sd;
	// }
	// }
	// throw new ServiceBrokerException("service definition: "
	// + serviceDefinitionId + " not found in catalog.");
	// }

	// private Plan getPlan(ServiceDefinition serviceDefinition, String planId)
	// throws ServiceBrokerException {
	//
	// for (Plan p : serviceDefinition.getPlans()) {
	// if (planId.equals(p.getId())) {
	// return p;
	// }
	// }
	// throw new ServiceBrokerException("plan: " + planId
	// + " not found in service definition.");
	// }

	// public String createRequestPayload(CreateServiceInstanceRequest request)
	// throws ServiceBrokerException {
	//
	// ServiceDefinition sd = getServiceDefinition(request
	// .getServiceDefinitionId());
	//
	// Plan plan = getPlan(sd, request.getPlanId());
	//
	// // TODO get from entitlement response
	// // String subtenantRef = sd.getMetadata().get("groupId").toString();
	// String subtenantRef = "1234567879";
	//
	// return String.format(getContents("createRequest.json"), sd.getId(),
	// creds.getTenant(), subtenantRef, creds.getUsername(),
	// plan.getId());
	// }
	//
	// public String deleteRequestPayload(DeleteServiceInstanceRequest request)
	// throws ServiceBrokerException {
	//
	// return String.format(getContents("deleteRequest.json"),
	// request.getServiceInstanceId(), "anActionId",
	// creds.getTenant(), "aGroupId");
	// }
	//
	// private String getContents(String fileName) throws ServiceBrokerException
	// {
	// try {
	// URI u = new ClassPathResource(fileName).getURI();
	// return new String(Files.readAllBytes(Paths.get(u)));
	// } catch (IOException e) {
	// throw new ServiceBrokerException("error reading template.", e);
	// }
	// }
}
