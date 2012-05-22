package data;

import com.google.gson.JsonObject;

public class TourData {	
	
	public int tourId;
	
	public int userId;
	
	public double latitude;
	
	public double longitude;

	public String tourName;
	
	public String description;

	public int locId;
	
	public double walkingDistance;
	
	public NodeData[] tourNodes;
	
	public boolean official;
	
	public boolean active;
	
	public TourTag[] tourTags;
	
	public TourData(int id, String name, String desc, NodeData[] nodeVec){
		this.tourId = id;
		this.tourName = name;
		this.description = desc;
		this.tourNodes = nodeVec;
	}

	public TourData(JsonObject jsonObj) {
		this.tourName = jsonObj.get("tourName").toString();
		// TODO Auto-generated constructor stub
	}
}
