package edu.stanford.mdocent.data;

import java.util.Arrays;
import java.util.HashMap;
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

public class Tour {	
	
	private static final String TAG = "Tour";
	private Integer tourId = null;
	private Integer userId = null;
	private Double latitude = null;
	private Double longitude = null;
	private String tourName = null;
	private String tourDesc = null;
	private Integer locId = null;
	private Double tourDist = null;
	private Vector<Node> tourNodes = null;
	private boolean official;
	private boolean active;
	private Vector<TourTag> tourTags;

	/* A private cache for non saved Tours */
	private static HashMap<Integer, Tour> tourCache = new HashMap<Integer, Tour>();
	private static HashMap<String, Integer> nameMap = new HashMap<String, Integer>();
	
	public Tour(){}
			
	public boolean save(){
		try {
			Gson gson = new Gson();
			JsonElement jsonElem = new JsonParser().parse(gson.toJson(this));
			if (this.tourId == null){
				
				JsonElement result = DBInteract.postData(jsonElem, Constants.TOUR_URL);
				if (result == null || !result.isJsonObject()){
					Log.v(TAG, "Something horrible happened when saving tour");
					return false;
				}
				JsonObject createResult = result.getAsJsonObject();
				JsonElement tourId = createResult.get("tourId");
				JsonElement userId = createResult.get("userId");
				
				if (tourId == null || userId == null || 
						!tourId.isJsonPrimitive() || !userId.isJsonPrimitive()){
					Log.v(TAG, "Something horrible happened when saving tour");
					return false;
				}
				this.tourId = new Integer(tourId.getAsInt());
				this.userId = new Integer(userId.getAsInt());
				jsonElem = new JsonParser().parse(gson.toJson(this));
			}
			Log.v(TAG, "Tour Pre Save");
			Log.v(TAG, jsonElem.toString());
			JsonElement result = DBInteract.postData(jsonElem, Constants.MODIFY_TOUR_URL);
			if (result == null || !result.isJsonObject()){
				Log.v(TAG, "Update Failed "+result.toString());
				return false;
			}
			JsonObject updateResult = result.getAsJsonObject();
			if (updateResult.get("success") == null){
				Log.v(TAG, "Update Failed "+updateResult.toString());
				return false;
			}
			if (tourNodes != null){
				for (int i = 0; i < tourNodes.size(); i ++){
					Node n = tourNodes.get(i);
					n.setTourId(tourId);
					n.save(new Callback(){
						@Override
						public void onFinish(Node newNode){
							if (newNode == null){
								Log.e(TAG, "A node failed to load");
							} else {
								Log.v(TAG, "Node Saved correctly");
							}
						}
					});
				}
			}
			return true;
		} catch (Exception e1){
			Log.v(TAG, "Something horrible happened when saving tour "+e1.toString());
			return false;
		}
	}
	
	public Double getLatitude() {
		return latitude;
	}
	
	public void setLatitude(Double latitude){
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude){
		this.longitude = longitude;
	}
	
	public String getTourName() {
		return tourName;
	}

	public void setTourName(String tourName) {
		this.tourName = tourName;
	}

	public String getDesc() {
		return tourDesc;
	}

	public void setTourDesc(String description) {
		this.tourDesc = description;
	}

	public int getLocId() {
		return locId;
	}

	public void setLocId(Integer locId) {
		this.locId = locId;
	}

	public Double getTourDist() {
		return tourDist;
	}

	public void setTourDist(Double walkingDistance) {
		this.tourDist = walkingDistance;
	}

	private void loadNodes() {
		if (tourId == null || tourNodes != null){
			return;
		}
		QueryString qs = new QueryString("tourId", tourId.toString());
		JsonElement result = DBInteract.getData(Constants.TOUR_URL, qs.toString());
		if (result == null || !result.isJsonObject()){
			Log.v(TAG, result.toString());
			return;
		}
		Log.v(TAG, result.toString());
		loadNodes(result);
	}
	private void loadNodes(JsonElement json){
		System.out.println("Loading Nodes");
		if(!json.isJsonObject() || !json.getAsJsonObject().has("nodes")){
			return;
		}
		JsonElement data = json.getAsJsonObject().get("nodes");
		if (!data.isJsonArray()){
			return;
		}
		JsonArray nodes = data.getAsJsonArray();
		tourNodes = new Vector<Node>();
		Gson gson = new Gson();
		for (int i = 0; i < nodes.size(); i ++){
			tourNodes.add(gson.fromJson(nodes.get(i), Node.class));
		}
	}
	
	public Vector<Node> getTourNodes() {
		if (tourNodes == null){
			loadNodes();
		}
		return tourNodes;
	}

	public void appendNode(Node newNode, final Callback cb){
		getTourNodes();
		newNode.setTourId(tourId);
		newNode.save(new Callback(){
			@Override
			public void onFinish(Node node){
				if (node == null){
					cb.onFinish(node);
					return;
				}
				tourNodes.add(node);
				cb.onFinish(node);
			}
		});
		return;
	}
	
	public void insertNode(Node newNode, final int idx, final Callback cb){
		Node finished = null;
		if (idx >= tourNodes.size() || idx < 0){
			cb.onFinish(finished);
			return;
		}
		newNode.setPrevNode(tourNodes.get(idx).getPrevNode());
		newNode.setTourId(tourId);
		newNode.save(new Callback(){
			@Override
			public void onFinish(Node node){
				if (node == null){
					cb.onFinish(node);
					return;
				}
				tourNodes.get(idx).setPrevNode(node.getNodeId());
				if (idx > 0){
					tourNodes.get(idx - 1).setNextNode(node.getNodeId());
				}
				tourNodes.insertElementAt(node, idx);
				cb.onFinish(node);
			}
		});
		return;
	}


	public boolean isOfficial() {
		return official;
	}

	public void setOfficial(boolean official) {
		this.official = official;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public int getTourId() {
		return tourId;
	}

	public int getUserId() {
		return userId;
	}

	public Vector<TourTag> getTourTags() {
		return tourTags;
	}
	
	@Override
	public String toString() {
		String nodes = "null";
		if (tourNodes != null){
			nodes = tourNodes.toString();
		}
		String tags = "null";
		if (tourTags != null){
			tags = tourTags.toString(); 
		}
		return "Tour [tourId=" + tourId + ", userId=" + userId + ", latitude="
				+ latitude + ", longitude=" + longitude + ", tourName="
				+ tourName + ", tourDesc=" + tourDesc + ", locId="
				+ locId + ", tourDist=" + tourDist
				+ ", tourNodes=" +  nodes + ", official="
				+ official + ", active=" + active + ", tourTags="
				+ tags + "]";
	}

	/* Static Functions on the tour object*/
	
	/***
	 * Returns an array of tours based on the 
	 * @param tours
	 * @return
	 */
	public static Vector<Tour> getToursFromJsonArray (JsonArray tours){
		try {
			Gson gson = new Gson();
			Vector<Tour> tourVector = new Vector<Tour>();
			for(int i = 0; i < tours.size(); i++){
				tourVector.add(gson.fromJson(tours.get(i).toString(), Tour.class));
			}
			return tourVector;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return null;
	}
	
	//For now only returns true if json array is present. eventually needs to return elements of array
	public static Vector<Tour> tourKeywordSearch(String searchStr) { 	
		QueryString qs = new QueryString("q", searchStr);

		try {
			JsonElement result = DBInteract.getData(Constants.SEARCH_TOURS_URL, qs.toString());
			
			if(result == null || !result.isJsonArray()) {
				Log.v(TAG, result.toString());
				return null;
			}
			
			JsonArray tours = result.getAsJsonArray();
			Log.v(TAG, "Tour Search successful "+ tours.toString());
			return getToursFromJsonArray(tours);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return null;
	} 


	//For now only returns true if json array is present. eventually needs to return elements of arraytoString
	public static Vector<Tour> tourUserSearch() { 	
		QueryString qs = new QueryString("userId", "true");
		try {
			JsonElement result = DBInteract.getData(Constants.SEARCH_TOURS_URL, qs.toString());
			if (result == null || !result.isJsonArray()){
				Log.v(TAG, result.toString());
				return null;
			}
			JsonArray tours = result.getAsJsonArray();
			Log.v(TAG, "Tour Search successful "+ tours.toString());
			return getToursFromJsonArray(tours);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return null;
	} 
	
	public static Tour getTourById (Integer tourId, boolean cached){
		if (cached && tourCache.containsKey(tourId)){
			return tourCache.get(tourId);
		}
		QueryString qs = new QueryString("tourId", tourId.toString());
		try {
			JsonElement result = DBInteract.getData(Constants.TOUR_URL, qs.toString());
			if (result == null || !result.isJsonObject()){
				Log.v(TAG, result.toString());
				return null;
			}
			Log.v(TAG, "GET BY ID");
			Log.v(TAG, result.toString());
			Tour newTour = new Gson().fromJson(result.toString(), Tour.class);
			newTour.loadNodes(result);
			System.out.println(newTour);
			tourCache.put(tourId, newTour);
			nameMap.put(newTour.getTourName(), newTour.getTourId());
			return newTour;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return null;
	}
	
	public static Tour getTourByName (String tourName, boolean cached){
		if (cached && nameMap.containsKey(tourName) && 
				tourCache.containsKey(nameMap.get(tourName))){
			return tourCache.get(nameMap.get(tourName));
		}
		QueryString qs = new QueryString("tourName", tourName);
		try {
			JsonElement result = DBInteract.getData(Constants.TOUR_URL, qs.toString());
			if (result == null || !result.isJsonObject()){
				Log.v(TAG, "No Tours found for that query");
				return null;
			}
			Tour newTour = new Gson().fromJson(result.toString(), Tour.class);
			newTour.loadNodes(result);
			return newTour;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return null;
	}
}
