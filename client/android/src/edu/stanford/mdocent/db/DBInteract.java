package edu.stanford.mdocent.db;


import org.apache.http.HttpResponse;
import org.apache.http.client.*;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import edu.stanford.mdocent.db.Constants;
import edu.stanford.mdocent.utilities.Utils;

import android.util.Log;

public class DBInteract {
	
	private static final String TAG = "DBInteract";
	private static CookieStore cookieStore = null;

	public static JsonElement postData(JsonElement jo, String url){
		if (jo == null || url == null){
			return null;
		}
		DefaultHttpClient httpclient = new DefaultHttpClient();
		if (cookieStore != null){
			httpclient.setCookieStore(cookieStore);
		}
		
		HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), 10000);
		HttpResponse response;
		try {
			HttpPost post = new HttpPost(Constants.SERVER_URL + url);
			StringEntity se = new StringEntity( jo.toString());  
			se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			post.setEntity(se);
			response = httpclient.execute(post);

			if (response != null){
				String str = Utils.inputStreamToString(response.getEntity().getContent()).toString();
				JsonParser parser = new JsonParser();
				JsonElement recvObj = parser.parse(str);
				Log.v(TAG, "Httpget returned: " + recvObj.toString());
				if (cookieStore == null){
					cookieStore = httpclient.getCookieStore();
				}
				return recvObj;
			}

		} catch (Exception e) {
			System.out.println("Failure");
			e.printStackTrace();
		}
		Log.v(TAG, "Http post failed. Posted: " + jo.toString() + " to url: "+ url);
		return null;
	}
		
	public static JsonElement getData(String url, String query){
		if (url == null || query == null){
			return null;
		}
		if (query != null){
			url += "?"+query;
		}
		DefaultHttpClient httpclient = new DefaultHttpClient();
		if (cookieStore != null){
			httpclient.setCookieStore(cookieStore);
		}
		HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), 10000);
		HttpResponse response;
		try {
			HttpGet get = new HttpGet(Constants.SERVER_URL + url); 
			response = httpclient.execute(get);
			if (response != null){
				String str = Utils.inputStreamToString(response.getEntity().getContent()).toString();
				JsonParser parser = new JsonParser();
				JsonElement recvElem = parser.parse(str);
				
				if (recvElem == null){
					Log.v(TAG, "Error in HttpGet. recieved null");
					return null;
				}
				
				if (recvElem.isJsonObject()){
					if (recvElem.getAsJsonObject().has("error")){
						Log.v(TAG, "Error in HttpGet. Received: " + recvElem.toString() + " url: "+ url);
						return null;
					} else {
						return recvElem;
					}
				}
				return recvElem;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		Log.v(TAG, "Http get failed. Get url: "+ url);
		return null;
	}
}
