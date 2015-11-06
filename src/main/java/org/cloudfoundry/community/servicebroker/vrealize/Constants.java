package org.cloudfoundry.community.servicebroker.vrealize;

public interface Constants {

	// metatdata keys
	public static final String CREATE_REQUEST_ID = "CREATE_REQUEST_ID";
	public static final String DELETE_REQUEST_ID = "DELETE_REQUEST_ID";
	public static final String CREATE_TEMPLATE_LINK = "CREATE_TEMPLATE_LINK";
	public static final String DELETE_TEMPLATE_LINK = "DELETE_TEMPLATE_LINK";
	public static final String DELETE_LINK = "DELETE_LINK";
	public static final String RESOURCES_LINK = "RESOURCES_LINK";

	// parameter keys
	public static final String USER_ID = "USER_ID";
	public static final String PASSWORD = "PASSWORD";
	public static final String DB_ID = "DB_ID";
	public static final String HOST = "HOST";
	public static final String PORT = "PORT";
	public static final String SERVICE_TYPE = "SERVICE_TYPE";

	// other keys
	public static final String URI = "uri";
	public static final String OPERATION_STATE_SUCCEEDED = "succeeded";
	public static final String OPERATION_STATE_IN_PROGRESS = "in progress";

	//lifecycle keys
	public static final String SUCCESSFUL = "SUCCESSFUL";
	public static final String UNSUBMITTED = "UNSUBMITTED";
	public static final String SUBMITTED = "SUBMITTED";
	public static final String PENDING_PRE_APPROVAL = "PENDING_PRE_APPROVAL";
	public static final String IN_PROGRESS = "IN_PROGRESS";
	public static final String PENDING_POST_APPROVAL = "PENDING_POST_APPROVAL";
	
}
