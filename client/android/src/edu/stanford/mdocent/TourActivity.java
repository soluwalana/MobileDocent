package edu.stanford.mdocent;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import edu.stanford.mdocent.data.Node;
import edu.stanford.mdocent.data.Node.Brief;
import edu.stanford.mdocent.data.Tour;


public class TourActivity extends MapActivity {

	private LinearLayout linearLayout;
	private MapView mapView;
	private MapController mapController;
	private LocationManager locationManager;
	private LocationListener locationListener;
	List<Overlay> mapOverlays;
	Drawable drawable;
	TourItemizedOverlay itemizedOverlay;
	MyLocationOverlay myLocationOverlay;
	GeoPoint[] TourGeoPoints;
	Vector<Node> nodes;
	String tourName;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.tour);

		mapView = (MapView) findViewById(R.id.map_view);
		mapView.setBuiltInZoomControls(true);

		mapOverlays = mapView.getOverlays();
		drawable = this.getResources().getDrawable(R.drawable.icon);
		itemizedOverlay = new TourItemizedOverlay(drawable, this);

		//get tours, populate overlays, populate array of geopoints
		tourName = getIntent().getStringExtra("tour_name");
		Tour tour = Tour.getTourByName(tourName, false);
		if(tour == null) {
			Log.e("tour", "null");
		}
		nodes = tour.getTourNodes();
		if(nodes == null) {
			Log.e("nodes", "null");
		}
		Log.e("number of nodes", Integer.toString(nodes.size()));

		TourGeoPoints = new GeoPoint[nodes.size()];

		for(int i = 0; i < nodes.size(); i++){
			GeoPoint gp = new GeoPoint((int)(nodes.get(i).getLatitude()* 1E6),(int)(nodes.get(i).getLongitude()* 1E6));
			TourGeoPoints[i] = gp;
			OverlayItem overlayitem = new OverlayItem(gp, "", "");
			itemizedOverlay.addOverlay(overlayitem);
		}
		/*final GeoPoint test_point = new GeoPoint((int)(37.424 * 1E6),(int)(-122.174 * 1E6));
		OverlayItem overlayitem = new OverlayItem(test_point, "", "");
		GeoPoint point2 = new GeoPoint((int)(37.426 * 1E6),(int)(-122.172 * 1E6));
		OverlayItem overlayitem2 = new OverlayItem(point2, "Main Quad", "this is the second node in the tour. You are at Main Quad.");
		GeoPoint point3 = new GeoPoint((int)(37.429* 1E6),(int)(-122.170 * 1E6));
		OverlayItem overlayitem3 = new OverlayItem(point3, "The Oval", "This is the last node in the tour. Go out and play frisbee on the oval!");

		itemizedOverlay.addOverlay(overlayitem);
		itemizedOverlay.addOverlay(overlayitem2);
		itemizedOverlay.addOverlay(overlayitem3);*/

		mapOverlays.add(itemizedOverlay);

		mapController = mapView.getController();
		mapController.animateTo(TourGeoPoints[0]);
		mapController.setZoom(16);

		//create overlay for current position
		myLocationOverlay = new MyLocationOverlay(this, mapView);
		myLocationOverlay.enableMyLocation();


		LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		// Location listener
		LocationListener listener = new LocationListener(){


			public void onLocationChanged(Location loc) {
				// TODO Auto-generated method stub
				GeoPoint cur_point = new GeoPoint((int)(loc.getLatitude()* 1E6),(int)(loc.getLongitude()* 1E6));

				int closestNode = 0;
				float shortestDistance = 0;
				Location tempLocation = new Location("");
				for(int i = 0; i < nodes.size(); i++){
					GeoPoint gp = itemizedOverlay.getItem(i).getPoint();
					float latitude = (float) (gp.getLatitudeE6() / 1E6);
					float longitude = (float) (gp.getLongitudeE6() / 1E6);

					tempLocation.setLatitude(latitude);
					tempLocation.setLongitude(longitude);

					float tempDistance = tempLocation.distanceTo(loc);
					if(i == 0) {
						shortestDistance = tempDistance;
					} else if(tempDistance < shortestDistance) {
						shortestDistance = tempDistance;
						closestNode = i;
					}

				}

				mapController.setCenter(itemizedOverlay.getItem(closestNode).getPoint());


				mapOverlays.add(myLocationOverlay);

				itemizedOverlay.onTap(closestNode);

			}

			
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub
			}

			
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub
			}

		
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				// TODO Auto-generated method stub
			}
		};
		// Register to get location updates
		manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,listener);


	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}


	private class TourItemizedOverlay extends ItemizedOverlay {

		private Context mContext;
		private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

		public TourItemizedOverlay(Drawable defaultMarker) {
			super(boundCenterBottom(defaultMarker));

		}

		public TourItemizedOverlay(Drawable defaultMarker, Context context) {
			super(boundCenterBottom(defaultMarker));
			mContext = context;
		}

		@Override
		protected boolean onTap(int index) {
			Log.e("index of on Tap",Integer.toString(index));
			OverlayItem item = mOverlays.get(index);
			Node node = nodes.get(index);
			Brief info = node.getBrief();
			
			
			Intent intent = new Intent(TourActivity.this, NodeActivity.class);
			intent.putExtra("tour_name", tourName);
			/* Intent intent = new Intent(TourActivity.this, NodeTabLayoutActivity.class);
			 * intent.putExtra("node_title", info.getTitle());
			intent.putExtra("node_details", info.getDesc());
			info.setPhotoURL("http://blogs.ubc.ca/CourseBlogSample01/wp-content/themes/thesis/rotator/sample-1.jpg");
		  intent.putExtra("node_photo", info.getPhotoURL());
		  info.setAudioURL("http://www.dccl.org/Sounds/songsparrow.wav");
		  intent.putExtra("node_audio", info.getAudioURL());*/
			startActivity(intent);

			return true;
		}

		@Override
		protected OverlayItem createItem(int i) {

			return mOverlays.get(i);

		}

		@Override
		public int size() {

			return mOverlays.size();
		}

		public void addOverlay(OverlayItem overlay) {
			mOverlays.add(overlay);
			populate();
		}

	}



}

