package edu.stanford.mdocent.data;

import java.io.File;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.stanford.mdocent.db.Constants;
import edu.stanford.mdocent.db.DBInteract;
import edu.stanford.mdocent.utilities.Callback;
import edu.stanford.mdocent.utilities.QueryString;


public class Node {

	public class Brief {
		
		private String title = null;
		private String desc = null;
		private String thumbId = null;
		private String photoURL = null;
		private String audioURL = null;
		
		public String thumbType = Constants.PLAIN_TEXT;
		public transient File thumbImg = null;
						
		public Brief(){}
		
		public String getPhotoURL(){
			return photoURL;
		}
		
		public String getAudioURL(){
			return audioURL;
		}
		
		public void setPhotoURL(String url){
			photoURL = url;
		}
		
		public void setAudioURL(String url){
			audioURL = url;
		}

		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public String getDesc() {
			return desc;
		}
		public void setDesc(String desc) {
			this.desc = desc;
		}
		public String getThumJsonElementbId() {
			return thumbId;
		}
		public String getThumbId() {
			return this.thumbId;
		}
		public void setThumbImg(File thumbnail, String type){
			this.thumbImg = thumbnail;
			this.thumbType = type;
		}
	}

	private static final String TAG = "Node";
	
	private Integer tourId = null;
	private Integer nodeId = null;
	private Integer prevNode = null;
	private Integer nextNode = null;
	private Double latitude = null;
	private Double longitude = null;
	private String mongoId = null;
	private Vector<Page> pages = null;
	private Brief brief = null;

	public Node(){}
	
	private Random random = new Random();
	
	private String randomId(){
		return new BigInteger(64, random).toString(32);
	}
	
	public void save(final Callback cb){
		/* Serialize pages and save them, then when save complete 
		Vector<Page> pages = getPages();   replace all pages and sections */
		HashMap <String, File> fileMap = new HashMap <String, File>();
		HashMap <String, String> typeMap = new HashMap <String, String>();
        getPages();
        JsonObject jo = new JsonObject();
		jo.addProperty("tourId", tourId);
		jo.addProperty("latitude", latitude);
		jo.addProperty("longitude", longitude);
		
		if (brief != null){
			JsonObject jBrief = new JsonObject();
			
			if (brief.thumbImg != null && brief.thumbType != null){
				String fileId = randomId();
				fileMap.put(fileId, brief.thumbImg);
				typeMap.put(fileId, brief.thumbType);
				jBrief.addProperty("thumbId", fileId);
			} 
			if (brief.title != null && brief.desc != null){
				jBrief.addProperty("title", brief.getTitle());
				jBrief.addProperty("desc", brief.getDesc());
			}
			jo.add("brief", jBrief);
		}
		final Gson gson = new Gson();
		JsonParser parser = new JsonParser();
		JsonArray jPages = new JsonArray();
		
		for (int i = 0; i < pages.size(); i ++){
			Vector<Section> sections = pages.get(i).getSections();
			JsonArray jSections = new JsonArray();
			
			for (int j = 0; j < sections.size(); j ++){
				Section section = sections.get(j);
				JsonObject jSection = parser.parse(gson.toJson(section)).getAsJsonObject();
				
				if (section.getTempData() != null){
					String fileId = randomId();
					fileMap.put(fileId, section.getTempData());
					typeMap.put(fileId, section.getContentType());
					jSection.addProperty("contentId", fileId);
				}
				jSections.add(jSection);				
			}
			jPages.add(jSections);
		}
		
		if (jPages.size() > 0){
			jo.add("content", jPages);
		}
		
		System.out.println("Before Save");
		System.out.println(jo.toString());
		
		DBInteract.postData(jo, fileMap, typeMap, Constants.NODE_URL, new Callback(){
			@Override
			public void onFinish(JsonElement result){
				Node finished = null;
				if (result == null || !result.isJsonObject()){
					cb.onFinish(finished);
				}
				JsonObject res = result.getAsJsonObject();
				if (!res.has("result") || !res.get("result").isJsonObject()){
					cb.onFinish(finished);
				}
				res = res.get("result").getAsJsonObject();
				cb.onFinish(gson.fromJson(res, Node.class));
			}
		});
	}
	
	public Integer getTourId() {
		return tourId;
	}
	public void setTourId(Integer tourId) {
		this.tourId = tourId;
	}
	public Double getLatitude() {
		return latitude;
	}
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	public Double getLongitude() {
		return longitude;
	}
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	public Brief getBrief() {
		if (brief == null && nodeId != null){
			QueryString qs = new QueryString("nodeId", nodeId.toString());
			JsonElement je = DBInteract.getData(Constants.NODE_CONTENT_URL, qs.toString());
			if (!je.isJsonObject()){
				Log.e(TAG, "Was expecting a JSON object but got "+je.toString());
				return null;
			}
			JsonObject nodeData = je.getAsJsonObject();
			if (nodeData.has("brief")){
				brief = new Gson().fromJson(nodeData.get("brief").toString(), Brief.class);
			} else {
				brief = new Brief();
			}
			if (nodeData.has("content")){
				loadPages(je);
			}
		} 
		if (brief == null){
			brief = new Brief();
		}
		return brief;
	}
	public Integer getNodeId() {
		return nodeId;
	}
	public Integer getPrevNode() {
		return prevNode;
	}
	public void setPrevNode(Integer prevNode){
		this.prevNode = prevNode;
	}
	public Integer getNextNode() {
		return nextNode;
	}
	public void setNextNode(Integer nextNode){
		this.nextNode = nextNode;
	}
	public String getMongoId() {
		return mongoId;
	}
	
	private void loadPages (JsonElement je){
		pages = new Vector<Page>();
		if (!je.isJsonObject()){
			return;
		}
		JsonObject nodeData = je.getAsJsonObject();
		if (!nodeData.has("content") || !nodeData.get("content").isJsonArray()){
			return;
		}
		JsonArray nodePages = nodeData.get("content").getAsJsonArray();
		for (int i = 0; i < nodePages.size(); i ++){
			pages.add(new Page(nodePages.get(i)));
		}			
	}
	
	public Vector<Page> getPages() {
		if (pages == null && mongoId == null){
			pages = new Vector<Page>();
		}
		if (pages == null && mongoId != null){
			// Get the content from the server
			QueryString qs = new QueryString("mongoId", mongoId);
			JsonElement je = DBInteract.getData(Constants.NODE_CONTENT_URL, qs.toString());
			loadPages(je);
		}
		return pages;
	}

	public void appendPage(Page page){
		getPages();
		pages.add(page);
	}
	
	public void insertPage(Page page, int idx){
		getPages();
		if (idx > 0 && idx < pages.size()){
			pages.insertElementAt(page, idx);
		}
	}
		
	public void removePage(int idx){
		getPages();
		if (idx > 0 && idx < pages.size()){
			pages.remove(idx);
		}
	}

	@Override
	public String toString() {
		return "Node [tourId=" + tourId + ", nodeId=" + nodeId + ", prevNode="
				+ prevNode + ", nextNode=" + nextNode + ", latitude="
				+ latitude + ", longitude=" + longitude + ", mongoId="
				+ mongoId + ", pages=" + pages + ", brief=" + brief + "]";
	}

}
