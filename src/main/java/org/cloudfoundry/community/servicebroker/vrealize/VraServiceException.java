package org.cloudfoundry.community.servicebroker.vrealize;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;

public class VraServiceException extends ServiceBrokerException {

	private static final long serialVersionUID = 1;

	public VraServiceException(String message) {
		super(message);
	}

}
