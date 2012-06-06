package edu.stanford.mdocent.tests;

import java.util.Vector;

import com.google.gson.Gson;
import edu.stanford.mdocent.data.Node;
import edu.stanford.mdocent.data.Tour;
import edu.stanford.mdocent.db.Administration;


public class SimpleTest{

	public static void testRandom() {
		Node x = new Node("some string");
		x.setDescription("New Description");
		Gson gson = new Gson();
		String json = gson.toJson(x, x.getClass());
		System.out.println(json);
		Node y = gson.fromJson(json, Node.class);
		System.out.println(y.toString());
		
		Tour z = new Tour();
		z.setTourName("New Tour");
		z.setTourDesc("New Description");
		z.setLatitude(new Double(122));
		z.setLongitude(new Double(-122));
	}

	public static void testTourCreation(){
		Administration.login("samo", "samo");
		Tour tour = new Tour();
		tour.setTourName("SamO's Tour");
		tour.setTourDesc("A tour of the ability to save tours");
		tour.setLatitude(37.814);
		tour.setLongitude(-122.423);
		tour.setActive(true);
		tour.setTourDist(1.5);
		System.out.println(tour.toString());
		tour.save();
		
		tour = Tour.getTourById(1);
		System.out.println(tour.toString());
		
		Vector<Tour> tours = Tour.tourKeywordSearch("stanford");
		for (int i = 0; i < tours.size(); i ++){
			System.out.println(tours.get(i).toString());
		}
	}
	
}

