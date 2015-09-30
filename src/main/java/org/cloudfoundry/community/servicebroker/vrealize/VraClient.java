package org.cloudfoundry.community.servicebroker.vrealize;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.DeleteServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.Plan;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import com.google.gson.Gson;

import feign.FeignException;

public class VraClient {

	@Autowired
	private VraRepository vraRepository;

	@Autowired
	Gson gson;

	@Autowired
	Creds creds;

	@Autowired
	Catalog catalog;

	public Catalog getAllCatalogItems(String token)
			throws ServiceBrokerException {
		try {
			return gson.fromJson(
					vraRepository.getAllCatalogItems("Bearer " + token),
					Catalog.class);
		} catch (FeignException e) {
			throw new ServiceBrokerException("error retrieving catalog.", e);
		}
	}

	// TODO point this to a working api endpoint
	public Catalog getEntitledCatalogItems(String token)
			throws ServiceBrokerException {
		// return gson.fromJson(
		// vraRepository.getEntitledCatalogItems("Bearer " + token),
		// Catalog.class);
		return getAllCatalogItems(token);
	}

	private ServiceDefinition getServiceDefinition(String serviceDefinitionId)
			throws ServiceBrokerException {

		for (ServiceDefinition sd : catalog.getServiceDefinitions()) {
			if (serviceDefinitionId.equals(sd.getId())) {
				return sd;
			}
		}
		throw new ServiceBrokerException("service definition: "
				+ serviceDefinitionId + " not found in catalog.");
	}

	private Plan getPlan(ServiceDefinition serviceDefinition, String planId)
			throws ServiceBrokerException {

		for (Plan p : serviceDefinition.getPlans()) {
			if (planId.equals(p.getId())) {
				return p;
			}
		}
		throw new ServiceBrokerException("plan: " + planId
				+ " not found in service definition.");
	}

	public String createRequestPayload(CreateServiceInstanceRequest request)
			throws ServiceBrokerException {

		ServiceDefinition sd = getServiceDefinition(request
				.getServiceDefinitionId());

		Plan plan = getPlan(sd, request.getPlanId());

		// TODO get from entitlement response
		// String subtenantRef = sd.getMetadata().get("groupId").toString();
		String subtenantRef = "1234567879";

		return String.format(getContents("createRequest.json"), sd.getId(),
				creds.getTenant(), subtenantRef, creds.getUsername(),
				plan.getId());
	}

	public String deleteRequestPayload(DeleteServiceInstanceRequest request)
			throws ServiceBrokerException {

		return String.format(getContents("deleteRequest.json"),
				request.getServiceInstanceId(), "anActionId",
				creds.getTenant(), "aGroupId");
	}

	private String getContents(String fileName) throws ServiceBrokerException {
		try {
			URI u = new ClassPathResource(fileName).getURI();
			return new String(Files.readAllBytes(Paths.get(u)));
		} catch (IOException e) {
			throw new ServiceBrokerException("error reading template.", e);
		}
	}
}
