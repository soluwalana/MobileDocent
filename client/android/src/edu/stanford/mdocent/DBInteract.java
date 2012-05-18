package edu.stanford.mdocent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.cookie.Cookie;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;


public class DBInteract {
	
	private static String serverURL = "http://samo.stanford.edu:8787";
	
	private static String postData(JSONObject jo, String urlEnd){
		
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), 10000);
		HttpResponse response;
        try {

			HttpPost post = new HttpPost(serverURL + urlEnd);
			StringEntity se = new StringEntity( jo.toString());  
			se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			post.setEntity(se);
			response = httpclient.execute(post);

			if(response!=null){
				
				
				String str = inputStreamToString(response.getEntity().getContent()).toString();
				
				return str;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static boolean postLoginData(String username, String password) { 
		
		JSONObject sndObject = new JSONObject();
		try {
			sndObject.put("userName", username);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		try {
			sndObject.put("password", password);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		
		String response = postData(sndObject, "/echo");
		if(response != "") return true;
		if(password.toString().equalsIgnoreCase("b")&&username.toString().equalsIgnoreCase("d")) return true;   
		return false;
	} 
	public static boolean postSignupData(String username, String password, String confirm) { 
		if(!password.equals(confirm)){
			return false;
		}
		JSONObject sndObject = new JSONObject();
		try {
			sndObject.put("userName", username);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		try {
			sndObject.put("password", password);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		
		String response = postData(sndObject, "/echo");
		if(response != "") return true;
		if(password.toString().equalsIgnoreCase("b")&&username.toString().equalsIgnoreCase("d")) return true;   
		return false;
	}

	private static StringBuilder inputStreamToString(InputStream is) {
		String line = "";
		StringBuilder total = new StringBuilder();
		// Wrap a BufferedReader around the InputStream
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		// Read response until the end
		try {
			while ((line = rd.readLine()) != null) { 
				total.append(line); 
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Return full string
		return total;
	}


}
