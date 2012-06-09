package edu.stanford.mdocent;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import edu.stanford.mdocent.RoadProvider;
import edu.stanford.mdocent.data.CreateMapView;
import edu.stanford.mdocent.data.MapOverlay;
import edu.stanford.mdocent.data.Node;
import edu.stanford.mdocent.data.Road;
import edu.stanford.mdocent.data.Tour;
import edu.stanford.mdocent.db.Constants;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class CreateTourActivity extends MapActivity  {

	private static final String TAG = "CreateTourActivity";

	LinearLayout linearLayout;
	CreateMapView mapView;
	public Vector<Road> roadVec = new Vector<Road>(); 
	private MapController mapController;
	//Vector<NodeData> nodeVector = new Vector<NodeData>();
	private ArrayList _displayedMarkers; 
	private LinearLayout _bubbleLayout;
	private Tour newTour;
	private int tourID;
	private final static int tourRequestCode = 1;
	private double tla; //testing
	private double tlo; //testing

	@Override
	public void onActivityResult(int requestCode,int resultCode,Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK ||resultCode == RESULT_CANCELED){
			Log.v(TAG, "RESULT_OK");
			//RENDER POINTS
			renderPoints();
		}
		else if (resultCode ==Constants.RESULT_RETURN){
			Log.v(TAG, "RESULT_RETURN");
			Intent intent = new Intent(this, TourNameActivity.class );
			setResult(Constants.RESULT_RETURN, intent);
			finish();
		}
	}
	private void renderPoints(){
		newTour = Tour.getTourById(newTour.getTourId(), false);  //CHANEG UPON PULL TODO
		double prevLat = -900.0;
		double prevLong = -900.0;
		boolean firstNode = true;
		Vector<Node> nodes = newTour.getTourNodes();
		if(nodes!=null){
			for(int i = 0; i<nodes.size(); i++){
				Log.v(TAG, "Nodes size: "+nodes.size());
				Node curNode = nodes.get(i);
				if(firstNode){
					prevLat = curNode.getLatitude();
					prevLong = curNode.getLongitude();
					Log.v(TAG, "lat: " + prevLat + " long: "+ prevLong);
					renderPoint(prevLat, prevLong);
					firstNode = false;
				}
				else {
					/*new Thread() {
						@Override
						public void run() {*/
					double fromLat = prevLat, fromLon = prevLong, toLat =curNode.getLatitude(), toLon =curNode.getLongitude();
					Log.v(TAG, "Prevlat: "+prevLat+" Prevlong: "+prevLong+" ToLat: " + toLat+" ToLong: "+toLon);
					 String url = RoadProvider
							.getUrl(fromLat, fromLon, toLat, toLon);
					InputStream is = getConnection(url);
					Road mRoad = RoadProvider.getRoute(is);
					roadVec.add(mRoad);
					mHandler.sendEmptyMessage(0);

					prevLat = toLat;
					prevLong = toLon;
					renderPoint(toLat, toLon);
					/*}
					}.start();*/
				}

			}
		}
	}
	private void renderPoint(double newLat, double newLong){
		GeoPoint point = new GeoPoint(  //LatLng 
				(int) (newLat * 1E6),
				(int) (newLong * 1E6));
		MapOverlayPoint mapOverlay = new MapOverlayPoint();
		mapOverlay.setPointToDraw(point);
		mapView.getOverlays().add(mapOverlay);
		mapController.setZoom(18);
		mapView.invalidate();
		//addNewNode(newLat, newLong);
	}
	public void finishCreateTour (){
		Intent intent = new Intent(this, CreateTourActivity.class );
		setResult(Constants.RESULT_RETURN, intent);
	    finish();
	}
	public void createTourCancel (){
		Intent intent = new Intent(this, CreateTourActivity.class );
		setResult(RESULT_CANCELED, intent);
	    finish();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.createtour);
		Button finishButton = (Button) findViewById(R.id.button1);
		finishButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				finishCreateTour();	
			}
		});
		Button cancelButton = (Button) findViewById(R.id.button2);
		cancelButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				createTourCancel();	
			}
		});

		Intent sender=getIntent();
		String tourName=sender.getExtras().getString("tourName");
		String tourDescription=sender.getExtras().getString("tourDescription");
		Log.v(TAG, "name: " + tourName + " desc: "+ tourDescription);
		newTour = new Tour();
		newTour.setTourName(tourName);
		newTour.setTourDesc(tourDescription);
		if(!newTour.save()){
			Log.v(TAG,"Create tour error: " + tourName);
		}

		mapView = (CreateMapView) findViewById(R.id.map_view);
		mapView.setBuiltInZoomControls(true);
		mapController = mapView.getController();
		mapController.setZoom(18); 

		Toast.makeText(getApplicationContext(), 
				"Press and Hold to make a new Node", Toast.LENGTH_LONG).show();

		mapView.setOnLongpressListener(new CreateMapView.OnLongpressListener() {

			private static final String TAG = "setOnLongpressListener";

			/*private void addNewNode(double newLat, double newLong){
				NodeData newNode = new NodeData();
				newNode.latitude = newLat;
				newNode.longitude = newLong;
				nodeVector.add(newNode);
			}*/


			public void onLongpress(final MapView view, final GeoPoint longpressLocation) {
				runOnUiThread(new Runnable() {
					public void run() {
						mapView.getOverlays().clear();
						Intent intent = new Intent(getBaseContext(), AddNodeActivity.class );
						intent.putExtra("tourID", newTour.getTourId());
						tla = (double)longpressLocation.getLatitudeE6()/ (double)1E6;
						tlo = (double)longpressLocation.getLongitudeE6()/ (double)1E6;
						intent.putExtra("nodeLat", (double)longpressLocation.getLatitudeE6()/ (double)1E6);
						intent.putExtra("nodeLon", (double)longpressLocation.getLongitudeE6()/ (double)1E6);
						startActivityForResult(intent,tourRequestCode);
					}
				});
			}
		});
	}

	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			for(int i = 0; i < roadVec.size(); i++){
				Road mRoad = roadVec.get(i);
				TextView textView = (TextView) findViewById(R.id.description);
				textView.setText(mRoad.mName + " " + mRoad.mDescription);
				MapOverlay mapOverlay = new MapOverlay(mRoad, mapView);
				List<Overlay> listOfOverlays = mapView.getOverlays();
				//listOfOverlays.clear();
				listOfOverlays.add(mapOverlay);
				mapController.setZoom(18); 
				mapView.invalidate();
			}
		};
	};

	private InputStream getConnection(String url) {
		InputStream is = null;
		try {
			URLConnection conn = new URL(url).openConnection();
			is = conn.getInputStream();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return is;
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	class MapOverlayPoint extends Overlay
	{
		private GeoPoint pointToDraw;

		public void setPointToDraw(GeoPoint point) {
			pointToDraw = point;
		}



		public GeoPoint getPointToDraw() {
			return pointToDraw;
		}

		@Override
		public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
			super.draw(canvas, mapView, shadow);           

			// convert point to pixels
			Point screenPts = new Point();
			mapView.getProjection().toPixels(pointToDraw, screenPts);

			// add marker
			Bitmap bmp = BitmapFactory.decodeResource( getResources(), R.drawable.cur_pos);
			canvas.drawBitmap(bmp, screenPts.x, screenPts.y - 24, null);    
			return true;
		}
	} 

}