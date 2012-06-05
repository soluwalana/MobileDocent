package edu.stanford.mdocent.data;

import java.util.Arrays;
import java.util.Vector;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.stanford.mdocent.db.Constants;
import edu.stanford.mdocent.db.DBInteract;
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
	private Node[] tourNodes = null;
	private boolean official;
	private boolean active;
	private TourTag[] tourTags;

	public Tour() {}
	
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
				this.tourId = tourId.getAsInt();
				this.userId = userId.getAsInt();
				jsonElem = new JsonParser().parse(gson.toJson(this));
			}
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
			return true;
		} catch (Exception e1){
			Log.v(TAG, "Something horrible happened when saving tour");
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

	public String getDescription() {
		return tourDesc;
	}

	public void setDescription(String description) {
		this.tourDesc = description;
	}

	public int getLocId() {
		return locId;
	}

	public void setLocId(Integer locId) {
		this.locId = locId;
	}

	public Double getWalkingDistance() {
		return tourDist;
	}

	public void setWalkingDistance(Double walkingDistance) {
		this.tourDist = walkingDistance;
	}

	public Node[] getTourNodes() {
		
		return tourNodes;
	}

	public void appendNode(Node newNode){
				
	}
	
	public void insertNode(Node newNode, int idx){
		
	}
	
	public void deleteNode(int idx){
				
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

	public TourTag[] getTourTags() {
		return tourTags;
	}
	

	@Override
	public String toString() {
		return "Tour [tourId=" + tourId + ", userId=" + userId + ", latitude="
				+ latitude + ", longitude=" + longitude + ", tourName="
				+ tourName + ", tourDesc=" + tourDesc + ", locId="
				+ locId + ", tourDist=" + tourDist
				+ ", tourNodes=" + Arrays.toString(tourNodes) + ", official="
				+ official + ", active=" + active + ", tourTags="
				+ Arrays.toString(tourTags) + "]";
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
	
	public static Tour getTourById (Integer tourId){
		QueryString qs = new QueryString("tourId", tourId.toString());
		try {
			JsonElement result = DBInteract.getData(Constants.TOUR_URL, qs.toString());
			if (result == null || !result.isJsonObject()){
				Log.v(TAG, result.toString());
				return null;
			}
			Log.v(TAG, result.toString());
			return new Gson().fromJson(result.toString(), Tour.class);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return null;
	}
	
	public static Tour getTourByName (String tourName){
		QueryString qs = new QueryString("tourName", tourName);
		try {
			JsonElement result = DBInteract.getData(Constants.TOUR_URL, qs.toString());
			if (result == null || !result.isJsonObject()){
				Log.v(TAG, result.toString());
				return null;
			}
			return new Gson().fromJson(result.toString(), Tour.class);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return null;
	}
}
