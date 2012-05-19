package edu.stanford.mdocent;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.maps.Overlay;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class TourActivity extends MapActivity {
    
    private MapView mapView;
    private MapController mapController;
	private LocationManager locationManager;
	private LocationListener locationListener;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tour);
        
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);  
        
        locationListener = new GPSLocationListener();
        
        locationManager.requestLocationUpdates(
          LocationManager.GPS_PROVIDER, 
          0, 
          0, 
          locationListener);
        
        mapView = (MapView) findViewById(R.id.map_view);       
        mapView.setBuiltInZoomControls(true);
        
        mapController = mapView.getController();
        mapController.setZoom(16); 
        
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
    
    
    
    private class GPSLocationListener implements LocationListener 
    {
      @Override
      public void onLocationChanged(Location location) {
        if (location != null) {
          GeoPoint point = new GeoPoint(
              (int) (location.getLatitude() * 1E6), 
              (int) (location.getLongitude() * 1E6));
          
         
          
          mapController.animateTo(point);
          mapController.setZoom(16);
          
          MapOverlay mapOverlay = new MapOverlay();
          mapOverlay.setPointToDraw(point);
          List<Overlay> listOfOverlays = mapView.getOverlays();
          listOfOverlays.clear();
          listOfOverlays.add(mapOverlay);
          mapView.invalidate();
          
          String address = ConvertPointToLocation(point);
          Toast.makeText(getBaseContext(), address, Toast.LENGTH_SHORT).show();
          
        }
      }

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	
	  public String ConvertPointToLocation(GeoPoint point) {   
		    String address = "";
		    Geocoder geoCoder = new Geocoder(
		        getBaseContext(), Locale.getDefault());
		    try {
		      List<Address> addresses = geoCoder.getFromLocation(
		        point.getLatitudeE6()  / 1E6, 
		        point.getLongitudeE6() / 1E6, 1);
		 
		      if (addresses.size() > 0) {
		        for (int index = 0; 
			index < addresses.get(0).getMaxAddressLineIndex(); index++)
		          address += addresses.get(0).getAddressLine(index) + " ";
		      }
		    }
		    catch (IOException e) {        
		      e.printStackTrace();
		    }   
		    
		    return address;
		  }     
    }
    
    class MapOverlay extends Overlay
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
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.cur_pos);
        canvas.drawBitmap(bmp, screenPts.x, screenPts.y - 24, null);    
        return true;
      }
    } 
    
}

