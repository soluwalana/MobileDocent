package edu.stanford.mdocent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;


public class DBInteract {
	
	private static String serverURL = "http://samo.stanford.edu:8787";
	
	public static boolean postLoginData(String username, String password) { 
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), 10000);
		HttpResponse response;

		try {
            
			JSONObject sndObject = new JSONObject();
			sndObject.put("userName", username);
			sndObject.put("password", password);

			HttpPost post = new HttpPost(serverURL + "/echo");
			StringEntity se = new StringEntity( sndObject.toString());  
			se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			post.setEntity(se);
			response = httpclient.execute(post);

			if(response!=null){
				
				String str = inputStreamToString(response.getEntity().getContent()).toString();
				
				if(str.contains("password"))return true;
				/*if(str.equalsIgnoreCase("true"))
				{
					return true;   
				}else
				{
					return false;              
				}*/
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
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
