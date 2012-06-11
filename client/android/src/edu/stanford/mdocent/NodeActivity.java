package edu.stanford.mdocent;

import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.ImageButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.net.MalformedURLException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Vector;

import edu.stanford.mdocent.db.Constants;
import edu.stanford.mdocent.db.DBInteract;

import edu.stanford.mdocent.data.Node;
import edu.stanford.mdocent.data.Page;
import edu.stanford.mdocent.data.Section;
import edu.stanford.mdocent.data.Tour;
import edu.stanford.mdocent.utilities.QueryString;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore.Images.Media;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.ViewFlipper;

public class NodeActivity extends Activity implements  android.view.GestureDetector.OnGestureListener, OnErrorListener,
OnBufferingUpdateListener, OnCompletionListener,
MediaPlayer.OnPreparedListener, SurfaceHolder.Callback {


	private ViewFlipper viewFlipper = null;  
	private GestureDetector gestureDetector = null;

	//variables for playing video
	String TAG = "VideoPlayer";
	MediaPlayer mp;
	SurfaceView mPreview;
	EditText mPath;
	SurfaceHolder holder;
	Button mPlay;
	Button mPause;
	Button mReset;
	Button mStop;
	String current;

	private RelativeLayout.LayoutParams getParams (int sectionSize, int sectionNum){
		DisplayMetrics metrics = this.getResources().getDisplayMetrics();
		int width = metrics.widthPixels - 10;
		int height = metrics.heightPixels - 85;
		int halfHeight = height/2;
		int halfWidth = width/2;
		Log.e("WIDTH_HEIGHT", Integer.toString(width) + " " + Integer.toString(height));
		RelativeLayout.LayoutParams params;

		switch(sectionSize){
		case 1:
			params = new RelativeLayout.LayoutParams(width, height);
			params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			return params;
		case 2:
			params = new RelativeLayout.LayoutParams(width, halfHeight);
			if(sectionNum == 0) params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			if(sectionNum == 1) params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);	
			return params;
		case 3:
		case 4:
			params = new RelativeLayout.LayoutParams(halfWidth, halfHeight);
			switch (sectionNum){
			case 0:
				params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
				return params;
			case 1:
				params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
				return params;
			case 2:
				params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				return params;
			case 3:
				params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				return params;
			default:
				Log.e(TAG, "There is no default");
			}
		}

		return null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//Remove notification bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.node_ontap);
		viewFlipper = (ViewFlipper) findViewById(R.id.viewflipper); 
		// gestureDetector Object is used to detect gesture events
		gestureDetector = new GestureDetector(this); 

		int nodeID = getIntent().getIntExtra("node_id",0);
		Node node = Node.getNodeById(nodeID);
		Log.e("NODE_ID", Integer.toString(nodeID));
		Vector<Page> pages = node.getPages();
		Log.v("TEST!", node.toString());
		Log.e("numpages", Integer.toString(pages.size()));

		//for each page create a relativelayout view and then add them to viewflipper
		for(int i = 0; i < pages.size(); i++){
			RelativeLayout relLayout = new RelativeLayout(getApplicationContext());
			relLayout.setId(i+1);
			Vector<Section> sections = pages.get(i).getSections();
			Log.e("numSections", Integer.toString(sections.size()));

			//for each section create and add appropriate view (like images, media, text) to scrollview>linearlayout page
			//at most 4 sections per page
			int sectionSize = sections.size();
			for(int j = 0; j < sectionSize; j++){
				Section section = sections.get(j);

				DisplayMetrics metrics = this.getResources().getDisplayMetrics();
				int width = metrics.widthPixels - 10;
				int height = metrics.heightPixels - 85;
				int halfHeight = height/2;
				int halfWidth = width/2;

				Log.e("section content type", section.getContentType());
				Log.e(TAG, section.toString());
				String type = section.getContentType();

				if(type.equals(Constants.PLAIN_TEXT)){
					TextView tv = new TextView(getApplicationContext());
					tv.setGravity(Gravity.TOP);
					//tv.setPadding(25, 25, 25, 25);

					tv.setText(section.getContent());
					relLayout.addView(tv, getParams(sectionSize, j));
				}
				else if(type.equals(Constants.PNG_TYPE) || 
						type.equals(Constants.JPEG_TYPE)){
					ImageView iv = new ImageView(getApplicationContext());
					//if(sectionSize == 1){
					iv.setScaleType(ImageView.ScaleType.FIT_XY);
					//}

					File input = DBInteract.getFile(section.getContentId(), this);
					if (input == null){
						Log.e("input image", "Was null");
					}

					try {
						BitmapFactory.Options opts = new BitmapFactory.Options();
						opts.inSampleSize = 5;
						Bitmap inBitmap = BitmapFactory.decodeFile(input.getAbsolutePath(), opts);
						//Bitmap inBitmap = Media.getBitmap(this.getContentResolver(), Uri.fromFile(input));
						Log.v(TAG, "in bitmap was null? "+(inBitmap == null));
						iv.setImageBitmap(inBitmap);
					} catch (Exception e) {
						e.printStackTrace();
					}		

					relLayout.addView(iv, getParams(sectionSize, j));
				}
				else if(type.equals(Constants.MP4_VIDEO_TYPE)){
					// Display Video Player MediaStore.AsyncPlayer

					// Set up the play/pause/reset/stop buttons
					//mPreview = (SurfaceView) findViewById(R.id.surface);
					//mPath = (EditText) findViewById(R.id.path);
					//LinearLayout ll = new LinearLayout(getApplicationContext());
					//ll.setOrientation(LinearLayout.HORIZONTAL);
					
					/*mPlay = new Button(getApplicationContext());
					mPlay.setText("Play");
					ll.addView(mPlay);
					mPause = new Button(getApplicationContext());
					mPause.setText("Pause");
					ll.addView(mPause);
					mReset = new Button(getApplicationContext());
					mReset.setText("Reset");
					ll.addView(mReset);
					mStop = new Button(getApplicationContext());
					mStop.setText("Stop");
					ll.addView(mStop);*/
					
				/*	String urlQuery = new QueryString("mongoFileId", section.getContentId()).toString();
					final String url = Constants.SERVER_URL + Constants.MONGO_FILE_URL+"?"+urlQuery;
					Log.e("VIDEO_URL", url);
					
					VideoView vv = new VideoView(getApplicationContext());
					vv.setVideoURI(Uri.parse(url));
					vv.setMediaController(new MediaController(getApplicationContext()));
		            vv.requestFocus();


					relLayout.addView(vv, getParams(sectionSize, j));

*/

				}
				else if(type.equals(Constants.MP4_AUDIO_TYPE) || type.equals(Constants.AMR_TYPE)
						|| type.equals(Constants.AUDIO_MPEG_TYPE)){
					String urlQuery = new QueryString("mongoFileId", section.getContentId()).toString();
					final String url = Constants.SERVER_URL + Constants.MONGO_FILE_URL+"?"+urlQuery;
					Log.e("audio_URL", url);

					LinearLayout ll = new LinearLayout(getApplicationContext());
					//ll.setOrientation(LinearLayout.HORIZONTAL);

					try {
						final MediaPlayer mp_audio = new MediaPlayer();
						mp_audio.reset();
						mp_audio.setDataSource(getBaseContext(), Uri.parse(url));
						//mp_audio.setDataSource(url);
						//mp_audio.setAudioStreamType(AudioManager.STREAM_MUSIC);
						mp_audio.setLooping(false); // Set looping
						mp_audio.prepare();

						Button play_but = new Button(getApplicationContext(), null, android.R.attr.buttonStyleSmall);
						play_but.setText("Play");
						ll.addView(play_but);
						play_but.setOnClickListener(new View.OnClickListener() {
							public void onClick(View v) {
								mp_audio.start();
							}
						});

						Button pause_but = new Button(getApplicationContext(), null, android.R.attr.buttonStyleSmall);
						pause_but.setText("Pause");
						ll.addView(pause_but);
						pause_but.setOnClickListener(new View.OnClickListener() {
							public void onClick(View v) {
								mp_audio.pause();
							}
						});

						Button stop_but = new Button(getApplicationContext(), null, android.R.attr.buttonStyleSmall);
						stop_but.setText("Stop");
						ll.addView(stop_but);
						stop_but.setOnClickListener(new View.OnClickListener() {
							public void onClick(View v) {
								try {
									mp_audio.stop();
									mp_audio.reset();
									mp_audio.setDataSource(getBaseContext(), Uri.parse(url));
									mp_audio.setLooping(false); // Set looping
									mp_audio.prepare();
								} catch (Exception e) {
									e.printStackTrace();
								}

							}
						});

					}
					catch(Exception e){
						e.printStackTrace();
					}

					relLayout.addView(ll, getParams(sectionSize, j));
				}
			}
			//add page/relativelayout to viewflipper
			viewFlipper.addView(relLayout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		}
		Toast.makeText(getApplicationContext(), "Swipe screen to flip through pages", Toast.LENGTH_SHORT).show();
	}

	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// TODO Auto-generated method stub
		if (arg0.getX() - arg1.getX() > 120)  
		{  

			this.viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this,  
					R.anim.push_left_in));  
			this.viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this,  
					R.anim.push_left_out));  
			this.viewFlipper.showNext();  
			Toast.makeText(getApplicationContext(), "Swipe screen to flip through pages", Toast.LENGTH_SHORT).show();
			return true;  
		}
		else if (arg0.getX() - arg1.getX() < -120)  
		{  
			this.viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this,  
					R.anim.push_right_in));  
			this.viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this,  
					R.anim.push_right_out));  
			this.viewFlipper.showPrevious();  
			Toast.makeText(getApplicationContext(), "Swipe screen to flip through pages", Toast.LENGTH_SHORT).show();
			return true;  
		}  
		return true;
	}
	public void onLongPress(MotionEvent arg0) {
		// TODO Auto-generated method stub

	}
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// TODO Auto-generated method stub
		return false;
	}
	public void onShowPress(MotionEvent arg0) {
		// TODO Auto-generated method stub

	}
	public boolean onSingleTapUp(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override  
	public boolean onTouchEvent(MotionEvent event)  
	{  
		return this.gestureDetector.onTouchEvent(event);  
	}

	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	public void surfaceCreated(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		
	}

	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		
	}

	public void onPrepared(MediaPlayer arg0) {
		// TODO Auto-generated method stub
		
	}

	public void onCompletion(MediaPlayer arg0) {
		// TODO Auto-generated method stub
		
	}

	public void onBufferingUpdate(MediaPlayer arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return false;
	}


}
