package edu.stanford.mdocent.db;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import edu.stanford.mdocent.utilities.QueryString;
import edu.stanford.mdocent.utilities.Utils;

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

		try {
			HttpPost post = new HttpPost(Constants.SERVER_URL + url);
			StringEntity se = new StringEntity( jo.toString());
			se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			post.setEntity(se);
			HttpResponse response = httpclient.execute(post);

			if (response != null){
				StringWriter writer = new StringWriter();
				IOUtils.copy(response.getEntity().getContent(), writer, "UTF-8");
				JsonParser parser = new JsonParser();
				JsonElement recvObj = parser.parse(writer.toString());
				Log.v(TAG, "Httpget returned: " + recvObj.toString());
				if (cookieStore == null){
					cookieStore = httpclient.getCookieStore();
				}
				return recvObj;
			}

		} catch (Exception e) {
			Log.v(TAG, "Failure");
			e.printStackTrace();
		} finally {
			try { httpclient.getConnectionManager().shutdown(); } catch (Exception ignored){}
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
		try {
			HttpGet get = new HttpGet(Constants.SERVER_URL + url);
			HttpResponse response = httpclient.execute(get);
			if (response != null){
				StringWriter writer = new StringWriter();
				IOUtils.copy(response.getEntity().getContent(), writer, "UTF-8");
				JsonParser parser = new JsonParser();
				JsonElement recvElem = parser.parse(writer.toString());

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
		} finally {
			try { httpclient.getConnectionManager().shutdown(); } catch (Exception ignored){}
		}
		Log.v(TAG, "Http get failed. Get url: "+ url);
		return null;
	}

	public static JsonElement postData (JsonElement jo, HashMap<String, Uri> files, 
			HashMap<String, String> typeMap, String url, ContentResolver cr){

		if (jo == null || files == null || url == null ){
			return null;
		}
		DefaultHttpClient httpclient = new DefaultHttpClient();
		if (cookieStore != null){
			httpclient.setCookieStore(cookieStore);
		}
		HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), 10000);
		try {
			HttpPost httppost = new HttpPost(Constants.SERVER_URL+url);
			MultipartEntity reqEntity = new MultipartEntity();

			Iterator<String> itr = files.keySet().iterator();
			while(itr.hasNext()){
				String key = itr.next();
				Uri file = files.get(key);
				
				InputStream is = cr.openInputStream(file);
				InputStreamBody bin = new InputStreamBody(is, typeMap.get(key), file.getLastPathSegment());
				reqEntity.addPart(key, bin);
			}
			reqEntity.addPart("nodeData", new StringBody(jo.toString()));
			httppost.setEntity(reqEntity);

			/* Run the post asynchronously and return to the callback when finished */
			HttpResponse response = httpclient.execute(httppost);
			JsonElement recvObj = null;
			if (response != null){
				try {
					StringWriter writer = new StringWriter();
					IOUtils.copy(response.getEntity().getContent(), writer, "UTF-8");
					JsonParser parser = new JsonParser();
					recvObj = parser.parse(writer.toString());
					Log.v(TAG, "Httppost returned: " + recvObj.toString());
					return recvObj;
				} catch (Exception err){
					Log.e(TAG, "Error parsing the response Multipart Post");
					Log.e(TAG, err.toString());
					err.printStackTrace();
					return null;
				}
			} else {
				return null;
			}
		} catch (Exception err){
			Log.e(TAG, "Failed to post files");
			err.printStackTrace();
		} finally {
			try { httpclient.getConnectionManager().shutdown(); } catch (Exception ignored){}
		}
		return null;
	}


	public static File getFile (String fileId, Context context){
		if (fileId == null || context == null){
			Log.e(TAG, "Invalid parameters");
			return null;
		}
		DefaultHttpClient httpclient = new DefaultHttpClient();
		if (cookieStore != null){
			httpclient.setCookieStore(cookieStore);
		}

		HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), 10000);

		QueryString urlQuery = new QueryString("mongoFileId", fileId);
		urlQuery.add("scaleDown", "true");
		String url = Constants.SERVER_URL + Constants.MONGO_FILE_URL+"?"+urlQuery.toString();
		Log.v(TAG, url);

		HttpGet httpget = new HttpGet(url);
		HttpResponse response = null;
		try{
			response = httpclient.execute(httpget);
		} catch (Exception ignored){}

		File outputFile = null;
		if (response != null){
			try {
				outputFile = Utils.getTempFile(context, "");
				if (outputFile == null || !outputFile.canWrite()){
					Log.e(TAG, "Couldn't write File");
					return null;
				}
				for (int i = 0; i < response.getAllHeaders().length; i ++){
					Header h = response.getAllHeaders()[i];
					Log.v(TAG, h.getName()+": "+h.getValue());
				}

				FileOutputStream outStream = new FileOutputStream(outputFile);
				response.getEntity().writeTo(outStream);
				return outputFile;
			} catch (Exception err){
				Log.e(TAG, "Error parsing the response");
				Log.e(TAG, err.toString());
				return null;
			}
		} else {
			Log.e(TAG, "Response was null");
			return null;
		}
	}

}
