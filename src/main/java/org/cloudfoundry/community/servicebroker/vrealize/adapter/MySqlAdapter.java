package org.cloudfoundry.community.servicebroker.vrealize.adapter;

import java.util.HashMap;
import java.util.Map;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.vrealize.Constants;

public class MySqlAdapter implements Adaptor {

	public static final String SERVICE_TYPE = "mysql";

	/**
	 * Creates a uri based on metadata in the specified service instance
	 * 
	 * @param instance
	 *            the instance
	 * @return a uri in the format
	 *         DB-TYPE://USERNAME:PASSWORD@HOSTNAME:PORT/NAME
	 * @throws ServiceBrokerException
	 */
	@Override
	public Map<String, Object> getCredentials(ServiceInstance instance)
			throws ServiceBrokerException {

		Object dbType = instance.getParameters().get(
				Constants.SERVICE_TYPE);
		Object userId = instance.getParameters().get(Constants.USER_ID);
		Object pw = instance.getParameters().get(Constants.PASSWORD);
		Object host = instance.getParameters().get(Constants.HOST);
		Object port = instance.getParameters().get(Constants.PORT);
		Object dbId = instance.getParameters().get(Constants.DB_ID);

		if (dbType == null || userId == null || pw == null || host == null
				|| port == null || dbId == null) {
			throw new ServiceBrokerException(
					"unable to construct connection uri from ServiceInstance.");
		}

		StringBuffer sb = new StringBuffer();
		sb.append(dbType);
		sb.append("://");
		sb.append(userId);
		sb.append(":");
		sb.append(pw);
		sb.append("@");
		sb.append(host);
		sb.append(":");
		sb.append(port);
		sb.append("/");
		sb.append(dbId);

		Map<String, Object> credentials = new HashMap<String, Object>();
		credentials.put("uri", sb.toString());

		return credentials;
	}

	public Map<String, Object> toParameters(
			Map<String, Object> vrCustomKeyValues) {
		Map<String, Object> m = new HashMap<String, Object>();

		m.put(Constants.USER_ID, vrCustomKeyValues.get("mysql_user"));
		m.put(Constants.PASSWORD, vrCustomKeyValues.get("mysql_passwd"));
		m.put(Constants.DB_ID, vrCustomKeyValues.get("mysql_dbname"));
		m.put(Constants.HOST, vrCustomKeyValues.get("foo"));
		m.put(Constants.PORT, vrCustomKeyValues.get("mysql_port"));
		m.put(Constants.SERVICE_TYPE, getServiceType());

		return m;
	}

	public String getServiceType() {
		return SERVICE_TYPE;
	}

}
