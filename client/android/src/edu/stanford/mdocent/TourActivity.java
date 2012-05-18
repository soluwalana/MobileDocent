package edu.stanford.mdocent;

import android.os.Bundle;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class TourActivity extends MapActivity {
    
    private MapView mapView;
    private MapController mapController;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tour);
        
        mapView = (MapView) findViewById(R.id.map_view);       
        mapView.setBuiltInZoomControls(true);
        
        mapController = mapView.getController();
        mapController.setZoom(16); 
        
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
    
}