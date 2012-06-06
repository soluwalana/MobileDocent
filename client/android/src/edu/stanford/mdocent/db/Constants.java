package edu.stanford.mdocent.db;

public interface Constants {
	public static final String SERVER_URL = "http://samo.stanford.edu:8787";
	public static final String MONGO_FILE_URL = "/mongoFile";
	public static final String USER_URL = "/user";
	public static final String TOUR_URL = "/tour";
	public static final String NODE_URL = "/node";
	public static final String NODE_CONTENT_URL = "/nodeContent";
	public static final String IP_TO_LOCATION_URL = "/ipLocation";
	public static final String TAGS_URL = "/tags";
	public static final String TAG_TOUR_URL = "/tagTour";
	public static final String MODIFY_TOUR_URL = "/modifyTour";
	public static final String MODIFY_NODE_URL = "/modifyNode";
	public static final String DELETE_TOUR_URL = "/deleteTour";
	public static final String DELETE_NODE_URL = "/deleteNode";
	public static final String DELETE_TOUR_TAG_URL = "/deleteTag";
	public static final String SEARCH_TOURS_URL = "/tours";
	public static final String CREATE_USER_URL = "/user";
	public static final String LOGIN_URL = "/login";
	public static final String LOGOUT_URL = "/logout";
	
	public static final String PLAIN_TEXT = "text/plain";
	public static final String HTML_TEXT = "text/html";
	public static final String JPEC_TYPE = "img/jpeg";
	public static final String PNG_TYPE = "img/png";
}
