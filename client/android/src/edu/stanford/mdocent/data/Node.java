package edu.stanford.mdocent.data;

import java.util.Arrays;


public class Node {

	private int tourId;
	
	private int nodeId;
	
	private Node prevNode;
	
	private double latitude;
	
	private double longitude;
	
	private String title;
	
	private String description;
	
	private String thumbId;
	
	private Page[] content;
	
	public Node(){
		
	}
	
	public Node(String jsonObj){
		this.tourId = 1;
		this.nodeId = 1;
		this.description = "Some String";
		this.latitude = -234.43;
		this.longitude = 23;
		this.title = "Some Title";
	}
		
	@Override
	public String toString() {
		return "NodeData [nodeId=" + nodeId + ", tourId=" + tourId
				+ ", content=" + Arrays.toString(content) + ", description="
				+ description + ", latitude=" + latitude + ", longitude="
				+ longitude + ", prevNode=" + prevNode + ", thumbId=" + thumbId
				+ ", title=" + title + "]";
	}

	public int getTourId() {
		return tourId;
	}

	public void setTourId(int tourId) {
		this.tourId = tourId;
	}

	public int getNodeId() {
		return nodeId;
	}

	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}

	public Node getPrevNode() {
		return prevNode;
	}

	public void setPrevNode(Node prevNode) {
		this.prevNode = prevNode;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getThumbId() {
		return thumbId;
	}

	public void setThumbId(String thumbId) {
		this.thumbId = thumbId;
	}

		
}
