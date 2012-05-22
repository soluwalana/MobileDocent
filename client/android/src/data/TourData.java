package data;


public class TourData {	
	
	public int tourId;
	
	public int userId;

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
}
