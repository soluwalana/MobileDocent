package edu.stanford.mdocent.tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Vector;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore.Images.Media;
import android.util.Log;

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

			JsonElement result = DBInteract.postData(jo, fileMap, typeMap, Constants.NODE_URL);
			Log.v(TAG, result.toString());

		} catch (Exception err){
			Log.e(TAG, err.toString());
			err.printStackTrace();
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

			Node node = tour.appendNode(newNode);
			Log.v(TAG, "Node 1 Added");
			Log.v(TAG, node.toString());
			Log.v(TAG, tour.toString());


			Node newNode2 = new Node();
			Brief newBrief2 = newNode2.getBrief();
			newBrief2.setDesc("Stuff");
			newBrief2.setTitle("Title");
			newNode2.setLatitude(22.34);
			newNode2.setLongitude(-122.34);
			node = tour.appendNode(newNode2);
			Log.v(TAG, "Node 2 Added");
			Log.v(TAG, node.toString());
			Log.v(TAG, tour.toString());


			Node newNode3 = new Node();
			newNode3.setLatitude(22.35);
			newNode3.setLongitude(-122.4);
			node = tour.appendNode(newNode3);
			Log.v(TAG, "Node 3 Added");
			Log.v(TAG, node.toString());
			Log.v(TAG, tour.toString());

		} catch (Exception err){
			Log.v(TAG, err.toString());
		}
	}

	public static Bitmap testFileUpload(Context context, ContentResolver cr, Bitmap mBitmap){
		Log.v(TAG, "Test Picture Upload From Gallery");
		Administration.login("samo", "samo");
		try {
			Tour tour = Tour.getTourById(1, false);
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

			FileOutputStream outStream = new FileOutputStream(imgFile);
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

			Node node = tour.appendNode(newNode);

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

			File input = DBInteract.getFile(section.getContentId(), context);
			if (input == null){
				Log.e(TAG, "Was null");
				return null;
			}
			Bitmap inBitmap = null;
			try {
				//InputStream inStream = new Utils.FlushedInputStream(new FileInputStream(input));
				//BufferedInputStream inStream = new BufferedInputStream(new FileInputStream(input));
				//inBitmap = BitmapFactory.decodeStream(inStream);
				//inStream.close();
				inBitmap = Media.getBitmap(cr, Uri.fromFile(input));
				return inBitmap;
			} catch (Exception err){
				err.printStackTrace();
				return null;
			}

		} catch (Exception err){
			Log.e(TAG, "Something Failed -_-");
			err.printStackTrace();
			return null;
		}
	}

	public static Bitmap simpleTest(Bitmap bm){
		return bm;
	}
}

