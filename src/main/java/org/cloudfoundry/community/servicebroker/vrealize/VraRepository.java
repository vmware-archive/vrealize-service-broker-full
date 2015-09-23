package org.cloudfoundry.community.servicebroker.vrealize;

import java.util.Map;

import org.springframework.stereotype.Repository;

import com.vmware.cloudclient.domain.Creds;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

@Repository
public interface VraRepository {

	@Headers("Content-Type: application/json")
	@RequestLine("POST /identity/api/tokens")
	public Map<String, String> getToken(Creds creds);
	
	@Headers({"Content-Type: application/json", "Authorization: Bearer {token}"})
	@RequestLine("GET /identity/api/tokens/{token}")
	public Map<String, String> checkToken(@Param(value = "token") String token);
	
	// @RequestLine("GET /")
	// public List<Quote> findAll();
	//
	// @RequestLine("GET /symbols")
	// List<String> symbols();
	//
	// @RequestLine("GET /marketSummary")
	// MarketSummary marketSummary();
	//
	// @RequestLine("GET /topGainers")
	// List<Quote> topGainers();
	//
	// @RequestLine("GET /topLosers")
	// List<Quote> topLosers();
}
