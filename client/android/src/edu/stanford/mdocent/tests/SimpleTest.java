package edu.stanford.mdocent.tests;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Vector;

import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;

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
import edu.stanford.mdocent.utilities.Utils;


public class SimpleTest{


	private static final String TAG = "Simple Test";

	public static void testTourCreation(){
		Log.v(TAG, "Test Tour Creation");
		Administration.login("samo", "samo");
		
		Log.v(TAG, "Tour1");
		Tour tour = Tour.getTourByName("SamO's Tour", false);
		if (tour == null){
			tour = new Tour();
			tour.setTourName("SamO's Tour");
			tour.setTourDesc("A tour of the ability to save tours");
			tour.setLatitude(37.814);
			tour.setLongitude(-122.423);
			tour.setActive(true);
			tour.setTourDist(1.5);
			Log.v(TAG, tour.toString());
			tour.save();
			
		}
		
		Log.v(TAG, tour.toString());
		Log.v(TAG, "Tour2");
		tour = Tour.getTourById(1, false);
		Log.v(TAG, tour.toString());
		
		Vector<Tour> tours = Tour.tourKeywordSearch("stanford");
		for (int i = 0; i < tours.size(); i ++){
			Log.v(TAG, "tour "+i);
			Log.v(TAG, tours.get(i).toString());
		}
	}
	
	public static void testTourCache(){
		Administration.login("samo", "samo");
		Log.v(TAG, "Test Tour Cache");
	}
	
	public static void testMultpartPost(Context context){
		Log.v(TAG, "Test Multi Part File Upload");
		Administration.login("samo", "samo");
		try {

			File newFile = Utils.getTempFile(context);
			BufferedWriter outStream = new BufferedWriter(new FileWriter(newFile));
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
					Log.v(TAG, result.toString());
				}
			});
		} catch (Exception err){
			Log.v(TAG, err.toString());
		}
	}
	
	public static void testSaveNode(Context context){
		Log.v(TAG, "Test Save Node");
		Administration.login("samo", "samo");
		try {
			final Tour tour = Tour.getTourById(1, false);
			Log.v(TAG, tour.toString());
			
			Section newSection = new Section();
			newSection.setContent("This is text content");
			newSection.setContentType(Constants.PLAIN_TEXT);
			newSection.setHeight(231);
			newSection.setWidth(132);
			newSection.setXpos(0);
			newSection.setYpos(0);
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
					Log.v(TAG, "Node 1 Added");
					Log.v(TAG, node.toString());
					Log.v(TAG, tour.toString());
				}
			});
			
			Node newNode2 = new Node();
			Brief newBrief2 = newNode2.getBrief();
			newBrief2.setDesc("Stuff");
			newBrief2.setTitle("Title");
			newNode2.setLatitude(22.34);
			newNode2.setLongitude(-122.34);
			tour.appendNode(newNode2, new Callback(){
				@Override
				public void onFinish(Node node){
					Log.v(TAG, "Node 2 Added");
					Log.v(TAG, node.toString());
					Log.v(TAG, tour.toString());
				}
			});
			
			Node newNode3 = new Node();
			newNode3.setLatitude(22.35);
			newNode3.setLongitude(-122.4);
			tour.appendNode(newNode3, new Callback(){
				@Override
				public void onFinish(Node node){
					Log.v(TAG, "Node 3 Added");
					Log.v(TAG, node.toString());
					Log.v(TAG, tour.toString());
				}
			});
			
		} catch (Exception err){
			Log.v(TAG, err.toString());
		}			
	}
	
	public static void testFileUpload(final Context context, final Context appContext, final Bitmap mBitmap){
		Log.v(TAG, "Test Picture Upload From Gallery");
		Administration.login("samo", "samo");
		try {
			final Tour tour = Tour.getTourById(1, false);
			Log.v(TAG, tour.toString());
			
			Section newSection = new Section();
			
			/* Code to convert a Gallery BitMap returned from select Intent to a
			   File */
			File imgFile = Utils.getTempFile(context);
			//String name = context.getFilesDir().getPath().toString()+"test.i";
			//File imgFile = new File(name);
			if (!imgFile.canWrite()){
				Log.e(TAG, "There was an error getting a temp file");
			}
			
			BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(imgFile));
			if (!mBitmap.compress(Constants.DEFAULT_IMG_TYPE, Constants.DEFAULT_QUALITY, outStream)){
				Log.v(TAG, "Error Compressing the File");
			}
			outStream.close();
						
			newSection.setTempData(imgFile);
			newSection.setContentType(Constants.DEFAULT_IMG_MIME_TYPE);
			newSection.setHeight(231);
			newSection.setWidth(132);
			newSection.setXpos(0);
			newSection.setYpos(0);
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
					Log.v(TAG, "Node 1 Added");
					Log.v(TAG, node.toString());
					Log.v(TAG, tour.toString());
					
					Brief brief = node.getBrief();
					assert(brief.getTitle().equals("A title"));
					assert(brief.getDesc().equals("This is a Node that you can observe"));					
					
					Vector<Page> pages = node.getPages();
					
					assert(pages.size() == 1);
					
					Page page = pages.get(0);
					Vector<Section> sections  = page.getSections();
					assert(sections.size() == 1);
											
					Section section = sections.get(0);
					assert(section.getContentId() != null);
					assert(section.getContentType().equals(Constants.PNG_TYPE));
					assert(section.getWidth().equals(new Integer(132)));
					
					DBInteract.getFile(section.getContentId(), context, new Callback(){
						@Override
						public void onFinish(File input){
							try{
								if (input == null){
									Log.e(TAG, "Was null");
								}
								//InputStream inStream = new Utils.FlushedInputStream(new FileInputStream(input));
								BufferedInputStream inStream = new BufferedInputStream(new FileInputStream(input));								Bitmap inBitmap = BitmapFactory.decodeStream(inStream);
								inStream.close();
								
								LinearLayout layout = new LinearLayout(context);
								ImageView image = new ImageView(context);
								image.setImageBitmap(inBitmap);
								layout.addView(image);
								
								Dialog dialog = new Dialog(appContext);
								dialog.setContentView(layout);
								dialog.setTitle("Custom Dialog");
								
								dialog.show();
							} catch (Exception err){
								Log.e(TAG, err.toString());
							}
						}
					});

				}
			});


		} catch (Exception err){
			Log.e(TAG, "Something Failed -_-");
			Log.e(TAG, err.toString());
		}
	}
}

