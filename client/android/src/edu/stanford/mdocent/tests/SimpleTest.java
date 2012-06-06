package edu.stanford.mdocent.tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Vector;

import org.json.JSONObject;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import edu.stanford.mdocent.data.Node;
import edu.stanford.mdocent.data.Node.Brief;
import edu.stanford.mdocent.data.Page;
import edu.stanford.mdocent.data.Section;
import edu.stanford.mdocent.data.Tour;
import edu.stanford.mdocent.db.Administration;
import edu.stanford.mdocent.db.Constants;
import edu.stanford.mdocent.db.DBInteract;
import edu.stanford.mdocent.utilities.Callback;


public class SimpleTest{


	public static void testTourCreation(){
		Administration.login("samo", "samo");
		
		System.out.println("Tour1");
		Tour tour = Tour.getTourByName("SamO's Tour");
		if (tour == null){
			tour = new Tour();
			tour.setTourName("SamO's Tour");
			tour.setTourDesc("A tour of the ability to save tours");
			tour.setLatitude(37.814);
			tour.setLongitude(-122.423);
			tour.setActive(true);
			tour.setTourDist(1.5);
			System.out.println(tour.toString());
			tour.save();
			
		}
		
		System.out.println(tour.toString());
		System.out.println("Tour2");
		tour = Tour.getTourById(1);
		System.out.println(tour.toString());
		
		Vector<Tour> tours = Tour.tourKeywordSearch("stanford");
		for (int i = 0; i < tours.size(); i ++){
			System.out.println("tour "+i);
			System.out.println(tours.get(i).toString());
		}
	}
	
	public static void testMultpartPost(Context context){
		Administration.login("samo", "samo");
		try {
			String filePath = context.getFilesDir().getPath().toString()+"testFile.txt";
			File newFile = new File(filePath);
			FileWriter fstream = new FileWriter(newFile);
			BufferedWriter outStream = new BufferedWriter(fstream);
			outStream.write("New File with Shit in it");
			outStream.close();
						
			HashMap <String, File> fileMap = new HashMap <String, File>();
			fileMap.put("test", newFile);
			HashMap <String, String> typeMap = new HashMap <String, String>();
			typeMap.put("test", "text/plain");


			JsonObject section = new JsonObject();
			section.addProperty("contentType", "text/plain");
			section.addProperty("contentId", "test");

			JsonArray pages = new JsonArray();
			pages.add(section);

			JsonArray content = new JsonArray();
			content.add(pages);

			JsonObject jo = new JsonObject();
			jo.addProperty("tourId", 1);
			jo.addProperty("latitude", 22.23);
			jo.addProperty("longitude", -122.74);
			jo.add("content", content);

			DBInteract.postData(jo, fileMap, typeMap, Constants.NODE_URL, new Callback(){
				@Override 
				public void onFinish(JsonElement result){
					System.out.println(result.toString());
				}
			});
		} catch (Exception err){
			System.out.println(err.toString());
		}
	}
	
	public static void testSaveNode(Context context){
		Administration.login("samo", "samo");
		try {
			final Tour tour = Tour.getTourById(1);
			System.out.println(tour.toString());
			
			Section newSection = new Section();
			newSection.setContent("This is text content");
			newSection.setContentType(Constants.PLAIN_TEXT);
			newSection.setHeight(231);
			newSection.setWidth(132);
			
			Page newPage = new Page();
			newPage.appendSection(newSection);
			
			
			Node newNode = new Node();
			Brief newBrief = newNode.getBrief();
			newBrief.setDesc("This is a Node that you can observe");
			newBrief.setTitle("A title");
			newNode.setLatitude(22.43);
			newNode.setLongitude(-122.34);
			
			newNode.appendPage(newPage);
						
			tour.appendNode(newNode, new Callback(){
				@Override
				public void onFinish(Node node){
					System.out.println(node);
					System.out.println(tour);
				}
			});
			
		} catch (Exception err){
			System.out.println(err.toString());
		}
	}
	
}

