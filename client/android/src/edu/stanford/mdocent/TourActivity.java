package edu.stanford.mdocent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.maps.Overlay;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;


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

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.tour);

		mapView = (MapView) findViewById(R.id.map_view);       
		mapView.setBuiltInZoomControls(true);

		mapOverlays = mapView.getOverlays();
		drawable = this.getResources().getDrawable(R.drawable.icon);
		itemizedOverlay = new TourItemizedOverlay(drawable, this);

		final GeoPoint test_point = new GeoPoint((int)(37.424 * 1E6),(int)(-122.174 * 1E6));
		OverlayItem overlayitem = new OverlayItem(test_point, "", "");
		GeoPoint point2 = new GeoPoint((int)(37.426 * 1E6),(int)(-122.172 * 1E6));
		OverlayItem overlayitem2 = new OverlayItem(point2, "Main Quad", "this is the second node in the tour. You are at Main Quad.");
		GeoPoint point3 = new GeoPoint((int)(37.429* 1E6),(int)(-122.170 * 1E6));
		OverlayItem overlayitem3 = new OverlayItem(point3, "The Oval", "This is the last node in the tour. Go out and play frisbee on the oval!");

		itemizedOverlay.addOverlay(overlayitem);
		itemizedOverlay.addOverlay(overlayitem2);
		itemizedOverlay.addOverlay(overlayitem3);

		mapOverlays.add(itemizedOverlay);

		mapController = mapView.getController();
		mapController.animateTo(test_point);
		mapController.setZoom(16); 

		//create overlay for current position
		myLocationOverlay = new MyLocationOverlay(this, mapView);
		myLocationOverlay.enableMyLocation();
		
		/*View popUp = getLayoutInflater().inflate(R.layout.popup, mapView, false);
		
		MapView.LayoutParams mapParams = new MapView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 
                ViewGroup.LayoutParams.WRAP_CONTENT, test_point, MapView.LayoutParams.BOTTOM_CENTER);
		mapView.addView(popUp, mapParams);*/


		/*locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);  

        locationListener = new GPSLocationListener();

        locationManager.requestLocationUpdates(
          LocationManager.GPS_PROVIDER, 
          0, 
          0, 
          locationListener);*/

		LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		// Location listener
		LocationListener listener = new LocationListener(){

			
			public void onLocationChanged(Location loc) {
				// TODO Auto-generated method stub
				GeoPoint cur_point = new GeoPoint((int)(loc.getLatitude()* 1E6),(int)(loc.getLongitude()* 1E6));
				
				int closestNode = 0;
				float shortestDistance = 0;
				Location tempLocation = new Location("");
				for(int i=0; i<3; i++){
					GeoPoint gp = itemizedOverlay.getItem(i).getPoint();
					float latitude = (float) (gp.getLatitudeE6() / 1E6);
					float longitude = (float) (gp.getLongitudeE6() / 1E6);

					tempLocation.setLatitude(latitude);
					tempLocation.setLongitude(longitude);
					
					float tempDistance = tempLocation.distanceTo(loc);
					if(i == 0) shortestDistance = tempDistance;
					else if(tempDistance < shortestDistance) {
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
		  OverlayItem item = mOverlays.get(index);
		  /*GeoPoint geo=item.getPoint();
		  PopupPanel panel = new PopupPanel(R.layout.node_tabs_layout);
		  
		  Point pt=mapView.getProjection().toPixels(geo, null);
		  panel.show(pt.y*2>mapView.getHeight());*/
	 
		  /*View popUp = getLayoutInflater().inflate(R.layout.node_tabs_layout, mapView, false);
		  MapView.LayoutParams mapParams = new MapView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 
		                          ViewGroup.LayoutParams.WRAP_CONTENT,
		                          item.getPoint(),
		                          MapView.LayoutParams.BOTTOM_CENTER);
		  mapView.addView(popUp, mapParams);*/
		  Intent intent = new Intent(TourActivity.this, NodeTabLayoutActivity.class);
		  intent.putExtra("node_details", "This is all the text details/information about this particular node. Lots of info yo.");
		  intent.putExtra("node_photo", "http://blogs.ubc.ca/CourseBlogSample01/wp-content/themes/thesis/rotator/sample-1.jpg");
		  intent.putExtra("node_audio", "http://www.dccl.org/Sounds/songsparrow.wav");
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
	
	
	class PopupPanel {
	    View popup;
	    boolean isVisible=false;
	    
	    PopupPanel(int layout) {
	      ViewGroup parent=(ViewGroup)mapView.getParent();

	      popup=getLayoutInflater().inflate(layout, parent, false);
	                  
	      popup.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	          hide();
	        }
	      });
	    }
	    
	    View getView() {
	      return(popup);
	    }
	    
	    void show(boolean alignTop) {
	      RelativeLayout.LayoutParams lp=new RelativeLayout.LayoutParams(
	            RelativeLayout.LayoutParams.WRAP_CONTENT,
	            RelativeLayout.LayoutParams.WRAP_CONTENT
	      );
	      
	      if (alignTop) {
	        lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
	        lp.setMargins(0, 20, 0, 0);
	      }
	      else {
	        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
	        lp.setMargins(0, 0, 0, 60);
	      }
	      
	      hide();
	      
	      ((ViewGroup)mapView.getParent()).addView(popup, lp);
	      isVisible=true;
	    }
	    
	    void hide() {
	      if (isVisible) {
	        isVisible=false;
	        ((ViewGroup)popup.getParent()).removeView(popup);
	      }
	    }
	  }



}

