package edu.stanford.mdocent.db;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;



public class Administration {

	private static final String TAG = "Administration";

	public static boolean login(String username, String password) {

		JsonObject sndObject = new JsonObject();
		try {
			sndObject.addProperty("userName", username);
			sndObject.addProperty("pass", password);

			JsonElement result = DBInteract.postData(sndObject, Constants.LOGIN_URL);

			if (result == null || !result.isJsonObject()){
				Log.v(TAG, "Unexpected result2");
				return false;
			}
			JsonObject recieved = result.getAsJsonObject();

			if(recieved != null) {
				if(recieved.get("success") != null){
					Log.v(TAG, "Authenticated " + recieved.toString());
					return true;
				} else {
					return false;
				}
			}
		}
		catch (Exception e1) {
			e1.printStackTrace();
		}
		return false;
	}

	public static boolean createUser(String username, String password, String confirm) {
		if(!password.equals(confirm)){
			return false;
		}

		JsonObject sndObject = new JsonObject();

		try {
			sndObject.addProperty("userName", username);
			sndObject.addProperty("pass", password);
			sndObject.addProperty("passConf", confirm);
			JsonElement result = DBInteract.postData(sndObject, Constants.CREATE_USER_URL);

			if (result == null || !result.isJsonObject()){
				Log.v(TAG, "Unexpected Result");
				return false;
			}
			JsonObject recieved = result.getAsJsonObject();
			if(recieved != null) {
				Log.v(TAG, recieved.toString());
				if(recieved.get("success") != null){
					Log.v(TAG, "Authenticated " + recieved.toString());
					return true;
				} else {
					return false;
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return false;
	}
}
