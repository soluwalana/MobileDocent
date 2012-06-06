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
import edu.stanford.mdocent.data.Road;

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

public class CreateTourActivity extends MapActivity {

	private static final String TAG = "CreateTourActivity";
	
	LinearLayout linearLayout;
	CreateMapView mapView;
	private Road mRoad; 
	private MapController mapController;
	//Vector<NodeData> nodeVector = new Vector<NodeData>();
	private ArrayList _displayedMarkers; 
	private LinearLayout _bubbleLayout; 
 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.createtour);
		
		Intent sender=getIntent();
        String tourName=sender.getExtras().getString("tourName");
        String tourDescription=sender.getExtras().getString("tourDescription");
        Log.v(TAG, "name: " + tourName + " desc: "+ tourDescription);
		
		mapView = (CreateMapView) findViewById(R.id.map_view);
		mapView.setBuiltInZoomControls(true);
		mapController = mapView.getController();
		mapController.setZoom(18); 
		
		Toast.makeText(getApplicationContext(), 
                "Press and Hold to make a new Node", Toast.LENGTH_LONG).show();

		mapView.setOnLongpressListener(new CreateMapView.OnLongpressListener() {
			double prevLat = -900.0;
			double prevLong = -900.0;
			private static final String TAG = "setOnLongpressListener";
			
			/*private void addNewNode(double newLat, double newLong){
				NodeData newNode = new NodeData();
				newNode.latitude = newLat;
				newNode.longitude = newLong;
				nodeVector.add(newNode);
			}*/
			
			private void renderPoint(double newLat, double newLong){
				GeoPoint point = new GeoPoint(  //LatLng 
						(int) (newLat * 1E6),
						(int) (newLong * 1E6));
				MapOverlayPoint mapOverlay = new MapOverlayPoint();
				mapOverlay.setPointToDraw(point);
				mapView.getOverlays().add(mapOverlay);
				mapController.setZoom(16);
				mapView.invalidate();
				//addNewNode(newLat, newLong);

			}
			public void onLongpress(final MapView view, final GeoPoint longpressLocation) {
				runOnUiThread(new Runnable() {
					public void run() {
						if(prevLat != -900.0){
							double fromLat = prevLat, fromLon = prevLong, toLat = (double)longpressLocation.getLatitudeE6()  / (double)1E6, toLon = (double)longpressLocation.getLongitudeE6()/ (double)1E6;
							String url = RoadProvider
									.getUrl(fromLat, fromLon, toLat, toLon);
							InputStream is = getConnection(url);
							mRoad = RoadProvider.getRoute(is);
							mHandler.sendEmptyMessage(0);
							prevLat = toLat;
							prevLong = toLon;
							renderPoint(toLat, toLon);
						}
						else {
							prevLat = (double)longpressLocation.getLatitudeE6()  / (double)1E6;
							prevLong = (double)longpressLocation.getLongitudeE6()/ (double)1E6;
							Log.v(TAG, "lat: " + prevLat + " long: "+ prevLong);
							renderPoint(prevLat, prevLong);
						}
						

				        // Get instance of the Bubble Layout ...
				        LayoutInflater inflater = (LayoutInflater) mapView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				        _bubbleLayout = (LinearLayout) inflater.inflate(R.layout.createtourbubble, mapView, false);
				 
				        // .. configure its layout parameters
				        MapView.LayoutParams params = new MapView.LayoutParams(300, 500, longpressLocation,
				                MapView.LayoutParams.BOTTOM_CENTER);
				 
				       _bubbleLayout.setLayoutParams(params);
				 
				        // Locate the TextView
				        TextView locationNameText = (TextView) _bubbleLayout.findViewById(R.id.locationName);
				         
				        // Set the Text
				        //locationNameText.setText(getSampleText());
				 
				        // Add the view to the Map
				        mapView.addView(_bubbleLayout);
				         
				        // Animate the map to center on the location
				        mapView.getController().animateTo(longpressLocation);
						mapController.setZoom(18);
						//mapView.invalidate();
				        
				        Button takeButton = (Button) findViewById(R.id.button2);
						takeButton.setOnClickListener(new OnClickListener(){
							public void onClick(View v) {
								mapView.removeAllViews();
								mapView.getOverlays().remove(mapView.getOverlays().size()-1);
							}
						});
						
				        Button submitButton = (Button) findViewById(R.id.button1);
				        submitButton.setOnClickListener(new OnClickListener(){
							public void onClick(View v) {
								mapView.removeAllViews();
								mapView.getOverlays().remove(mapView.getOverlays().size()-1);
							}
						});
				    
					}
				});
			}
		});
	}

	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			TextView textView = (TextView) findViewById(R.id.description);
			textView.setText(mRoad.mName + " " + mRoad.mDescription);
			MapOverlay mapOverlay = new MapOverlay(mRoad, mapView);
			List<Overlay> listOfOverlays = mapView.getOverlays();
			//listOfOverlays.clear();
			listOfOverlays.add(mapOverlay);
			mapController.setZoom(18); 
			mapView.invalidate();
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

	class MapOverlay extends com.google.android.maps.Overlay {
		Road mRoad;
		ArrayList<GeoPoint> mPoints;

		public MapOverlay(Road road, MapView mv) {
			mRoad = road;
			if (road.mRoute.length > 0) {
				mPoints = new ArrayList<GeoPoint>();
				for (int i = 0; i < road.mRoute.length; i++) {
					mPoints.add(new GeoPoint((int) (road.mRoute[i][1] * 1000000),
							(int) (road.mRoute[i][0] * 1000000)));
				}
				int moveToLat = (mPoints.get(0).getLatitudeE6() + (mPoints.get(
						mPoints.size() - 1).getLatitudeE6() - mPoints.get(0)
						.getLatitudeE6()) / 2);
				int moveToLong = (mPoints.get(0).getLongitudeE6() + (mPoints.get(
						mPoints.size() - 1).getLongitudeE6() - mPoints.get(0)
						.getLongitudeE6()) / 2);
				GeoPoint moveTo = new GeoPoint(moveToLat, moveToLong);

				MapController mapController = mv.getController();
				mapController.animateTo(moveTo);
				mapController.setZoom(7);
			}
		}

		@Override
		public boolean draw(Canvas canvas, MapView mv, boolean shadow, long when) {
			super.draw(canvas, mv, shadow);
			drawPath(mv, canvas);
			return true;
		}

		public void drawPath(MapView mv, Canvas canvas) {
			int x1 = -1, y1 = -1, x2 = -1, y2 = -1;
			Paint paint = new Paint();
			paint.setColor(Color.GREEN);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(3);
			for (int i = 0; i < mPoints.size(); i++) {
				Point point = new Point();
				mv.getProjection().toPixels(mPoints.get(i), point);
				x2 = point.x;
				y2 = point.y;
				if (i > 0) {
					canvas.drawLine(x1, y1, x2, y2, paint);
				}
				x1 = x2;
				y1 = y2;
			}
		}
	}
}