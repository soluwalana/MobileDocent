package edu.stanford.mdocent;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Vector;

import edu.stanford.mdocent.data.Node;
import edu.stanford.mdocent.data.Page;
import edu.stanford.mdocent.data.Section;
import edu.stanford.mdocent.data.Tour;
import edu.stanford.mdocent.db.Constants;
import edu.stanford.mdocent.tests.SimpleTest;
import edu.stanford.mdocent.utilities.Utils;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;


public class AddPageActivity extends Activity {

	private static final String TAG = "AddPageActivity";

	private static Uri savedUri;
	
	private Integer curTourID;
	private Integer curNodeID;
	private Node curNode;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		//setContentView(R.layout.addpage);
		Intent sender = getIntent();
		curTourID = sender.getExtras().getInt("tourID");
		curNodeID = sender.getExtras().getInt("nodeID");
		
		curNode = Node.getNodeById(curNodeID);

		Log.v(TAG, "Current tour: "+curTourID+" Current Node: "+curNodeID);
		Log.v(TAG, curNode.toString());

		Toast.makeText(getApplicationContext(), 
				"Select a type of content to add.", Toast.LENGTH_LONG).show();

		Button cameraButton = new Button(getApplicationContext());
		cameraButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				imagePopUp();
			}
		});
		cameraButton.setText("Add Image");

		Button audioButton = new Button(getApplicationContext());
		audioButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				audioPopUp();
			}
		});
		audioButton.setText("Add Audio");

		Button videoButton = new Button(getApplicationContext());
		videoButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				videoPopUp();
			}
		});
		videoButton.setText("Add Video");

		Button textButton = new Button(getApplicationContext());
		textButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				textPopUp();
			}
		});
		textButton.setText("Add Text");

		Button previewButton = new Button(getApplicationContext());
		previewButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				//previewActivity SAME LAYOUT AS WHEN TAKING A TOUR TODO
				appendNewPage();
			}
		});
		previewButton.setText("Add New Page");
		
		Button saveButton = new Button(getApplicationContext());
		saveButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				returnToAddNode();
			}
		});
		saveButton.setText("Save");
		LinearLayout layout = new LinearLayout(getApplicationContext());
		layout.setOrientation(LinearLayout.VERTICAL);

		LayoutParams params = new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, 100);
		params.setMargins(10, 10, 10, 10);
		layout.addView(cameraButton, params);
		layout.addView(audioButton, params);
		layout.addView(videoButton, params);
		layout.addView(textButton, params);
		layout.addView(previewButton, params);

		layout.addView(saveButton, params);
		setContentView(layout);

	}

	private void saveToNode(int fileType){
		Toast.makeText(getApplicationContext(),"Saving new file", Toast.LENGTH_LONG).show();
		if (curNode != null){
			//Toast.makeText(getApplicationContext(),"Node found", Toast.LENGTH_LONG).show();
			Page curPage = curNode.getPages().get(curNode.getPages().size() - 1);
			if(curPage.getSections().size() >= Constants.SECTIONS_PER_PAGE){
				Log.v(TAG, "Adding a new page into node "+curNodeID);
				curPage = new Page();
				curNode.appendPage(curPage);
			}

			Section newSection = new Section();
			ContentResolver cr = getContentResolver();
			cr.notifyChange(savedUri, null);
			newSection.setTempData(savedUri);
			
			switch(fileType){
			case Constants.FILE_TYPE_IMAGE:
				Toast.makeText(getApplicationContext(),"New image section being created", Toast.LENGTH_LONG).show();
				Log.v(TAG, "Image URI "+savedUri.toString());
				if (cr.getType(savedUri) == null){
					newSection.setContentType(Constants.DEFAULT_IMG_TYPE);
				}
				break;
			case Constants.FILE_TYPE_VIDEO:
				Toast.makeText(getApplicationContext(),"New video section being created", Toast.LENGTH_LONG).show();				newSection.setTempData(savedUri);
				Log.v(TAG, "saving object of type "+cr.getType(savedUri)+ " into node "+curNodeID);
				if (cr.getType(savedUri) == null){
					newSection.setContentType(Constants.DEFAULT_VIDEO_TYPE);
				}
				break;
			case Constants.FILE_TYPE_AUDIO:
				Toast.makeText(getApplicationContext(),"New audio section being created", Toast.LENGTH_LONG).show();
				if (cr.getType(savedUri) == null){
					newSection.setContentType(Constants.DEFAULT_AUDIO_TYPE);
				}
				break;
			}
			curPage.appendSection(newSection);
			
			Log.v(TAG, curNode.toString());
			//curNode.appendPage(curPage);
			curNode = curNode.save(cr);
			Toast.makeText(getApplicationContext(),"Succesfully saved new file", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		//super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
		case Constants.RESULT_IMAGE_PICKER:
			Toast.makeText(getApplicationContext(),"Saving image", Toast.LENGTH_LONG).show();
			if (resultCode == Activity.RESULT_OK){
				Uri selectedImage = data.getData();
				Log.v(TAG, selectedImage.toString());
				savedUri = selectedImage;
				try {
					saveToNode(Constants.FILE_TYPE_IMAGE);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			break;
		case Constants.RESULT_CAMERA:
			Toast.makeText(getApplicationContext(),"Saving image", Toast.LENGTH_LONG).show();
			if (resultCode == Activity.RESULT_OK){
				Uri selectedImage = savedUri;
				savedUri = selectedImage;
				try{
					saveToNode(Constants.FILE_TYPE_IMAGE);
				} catch(Exception e){
					e.printStackTrace();
				}
			}
			break;
		case Constants.RESULT_VIDEO_CAMERA:
			if (resultCode == Activity.RESULT_OK){
				Toast.makeText(getApplicationContext(),"Saving video", Toast.LENGTH_LONG).show();
				Uri selectedVideo = data.getData();
				savedUri = selectedVideo;
				try{
					saveToNode(Constants.FILE_TYPE_VIDEO);
				} catch(Exception e){
					e.printStackTrace();
				}
			}
			break;
		case Constants.RESULT_VIDEO_PICKER:
			Toast.makeText(getApplicationContext(),"Saving video", Toast.LENGTH_LONG).show();
			if (resultCode == Activity.RESULT_OK){
				Uri selectedVideo = data.getData();
				savedUri = selectedVideo;
				Log.v(TAG, selectedVideo.toString());
				try {
					saveToNode(Constants.FILE_TYPE_VIDEO);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			break;
		case Constants.RESULT_AUDIO_PICKER:
			Toast.makeText(getApplicationContext(),"Saving audio", Toast.LENGTH_LONG).show();
			if (resultCode == Activity.RESULT_OK){
				Uri selectedAudio = data.getData();
				savedUri = selectedAudio;
				Log.v(TAG, selectedAudio.toString());
				try {
					saveToNode(Constants.FILE_TYPE_AUDIO);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			break;
		case Constants.RESULT_AUDIO:
			Toast.makeText(getApplicationContext(),"Saving audio", Toast.LENGTH_LONG).show();
			if (resultCode == Activity.RESULT_OK){
				//Toast.makeText(getApplicationContext(),"Recorded audio recorded correctly", Toast.LENGTH_LONG).show();
				//Toast.makeText(getApplicationContext(),"Recorded audio data is null: "+(data==null), Toast.LENGTH_LONG).show();
				Uri selectedAudio = data.getData();
				savedUri = selectedAudio;
				try{
					saveToNode(Constants.FILE_TYPE_AUDIO);
				} catch(Exception e){
					e.printStackTrace();
				}
			}
			break;
		}

	}

	public void returnToAddNode (){
		Intent intent = new Intent(this, AddNodeActivity.class );
		intent.putExtra("tourID", curTourID);
		intent.putExtra("nodeID", curNodeID);
		setResult(RESULT_OK, intent);
		finish();
	}

	private void imagePopUp() {
		final CharSequence[] items = {"From Camera", "From File", "Cancel"};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select a source for your video");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				switch(item){
				case 0: 
					File newPhoto = null;
					Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					try {
						newPhoto = Utils.getRealFile(".jpg");
					} catch (Exception e) {
						e.printStackTrace();
						return;
					}
					cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(newPhoto));
					savedUri = Uri.fromFile(newPhoto);
					startActivityForResult(cameraIntent, Constants.RESULT_CAMERA);
					break;
				case 1: 
					Intent photoPick = new Intent(Intent.ACTION_GET_CONTENT);
					photoPick.setType("image/*");
					startActivityForResult(photoPick, Constants.RESULT_IMAGE_PICKER);
					break;
				default:
					break;
				}
			}
		});
		AlertDialog helpDialog = builder.create();
		helpDialog.show();
	}

	private void videoPopUp() {
		final CharSequence[] items = {"From Camera", "From File", "Cancel"};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select a source for your video");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				switch(item){
				case 0: 
					Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
					startActivityForResult(videoIntent, Constants.RESULT_VIDEO_CAMERA);
					break;
				case 1: 
					Intent videoPick = new Intent(Intent.ACTION_GET_CONTENT);
					videoPick.setType("video/*");
					startActivityForResult(videoPick, Constants.RESULT_VIDEO_PICKER);
					break;
				default:
					break;
				}
			}
		});
		AlertDialog helpDialog = builder.create();
		helpDialog.show();
	}

	private void audioPopUp() {
		final CharSequence[] items = {"From Mic", "From File", "Cancel"};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select a source for your audio");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				switch(item){
				case 0: 
					Intent audioIntent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
					startActivityForResult(audioIntent, Constants.RESULT_AUDIO);
					break;
				case 1: 
					Intent audioPick = new Intent(Intent.ACTION_GET_CONTENT);
					audioPick.setType("audio/*");
					startActivityForResult(audioPick, Constants.RESULT_AUDIO_PICKER);
					break;
				default:
					break;
				}
			}
		});
		AlertDialog helpDialog = builder.create();
		helpDialog.show();
	}
	
	private void appendNewPage() {
		curNode.appendPage(new Page());
		Toast.makeText(getApplicationContext(),"Appended new page, you are on page #"+
				curNode.getPages().size(), Toast.LENGTH_LONG).show();
		
	}
	
	private void textPopUp() {
		AlertDialog.Builder helpBuilder = new AlertDialog.Builder(this);
		helpBuilder.setTitle("Add Text");

		final EditText input = new EditText(this);
		input.setLines(5);
		input.setText("");
		input.setGravity(Gravity.TOP);
		helpBuilder.setView(input);
		helpBuilder.setMessage("Type in text information here.");
		helpBuilder.setPositiveButton("Finish",
				new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				String textInfo = input.getText().toString();
				Log.v(TAG, "Adding description: "+textInfo+" to tour: "+curTourID);
				if (curNode != null){
					Log.v(TAG, "Creating a new Section");
					Page curPage = curNode.getPages().get(curNode.getPages().size()-1);
					if(curPage.getSections().size() >= Constants.SECTIONS_PER_PAGE){

						Toast.makeText(getApplicationContext(),"Added new page to section", Toast.LENGTH_LONG).show();
						Log.v(TAG, "Adding a new page into node "+curNodeID);
						curPage = new Page();
						curNode.appendPage(curPage);
					}
					Section newSection = new Section();
					newSection.setContentType(Constants.PLAIN_TEXT);
					newSection.setContent(textInfo);
					curPage.appendSection(newSection);

					Log.v(TAG, curNode.toString());
					//curNode.appendPage(curPage);
					curNode = curNode.save(getContentResolver());
					Toast.makeText(getApplicationContext(),"Succesfully saved new text", Toast.LENGTH_LONG).show();
				}

			}
		});

		helpBuilder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// Do nothing and close the dialog
			}
		});
		AlertDialog helpDialog = helpBuilder.create();
		helpDialog.show();
	}
}