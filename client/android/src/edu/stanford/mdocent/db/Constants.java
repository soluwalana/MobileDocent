package edu.stanford.mdocent.db;

import android.graphics.Bitmap;

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

	public static final String JPEG_TYPE = "image/jpeg";
	public static final String PNG_TYPE = "image/png";

	public static final String AMR_TYPE = "audio/AMR";
	public static final String MP4_AUDIO_TYPE = "audio/mp4a-latm";
	
	public static final String MP4_VIDEO_TYPE = "video/mp4";
	
	public static final String AUDIO_MPEG_TYPE = "audio/mpeg";
	
	
	public static final int RESULT_RETURN = 3;

	public static final int RESULT_IMAGE_PICKER = 4;
	public static final int RESULT_CAMERA = 5;
	public static final int RESULT_VIDEO_CAMERA = 6;
	public static final int RESULT_AUDIO = 7;
	public static final int RESULT_VIDEO_PICKER = 8;
	public static final int RESULT_AUDIO_PICKER = 9;
	

	public static final int FILE_TYPE_IMAGE = 0;
	public static final int FILE_TYPE_VIDEO = 1;
	public static final int FILE_TYPE_AUDIO = 2;
	
	public static final int SECTIONS_PER_PAGE = 4;

	public static final Integer DEFAULT_QUALITY = 80;
	public static final String DEFAULT_IMG_TYPE = JPEG_TYPE;
	public static final String DEFAULT_VIDEO_TYPE = MP4_VIDEO_TYPE;
	public static final String DEFAULT_AUDIO_TYPE = AMR_TYPE;
	
	
	public static final Bitmap.CompressFormat DEFAULT_BMAP_IMG_TYPE = Bitmap.CompressFormat.JPEG;

	public static final int CLICK_THRESH = 3000;

}
