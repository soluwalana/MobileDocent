package edu.stanford.mdocent;

import android.os.Bundle;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

public class MobileDocentActivity extends MapActivity {
    
    private MapView mapView;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mapView = (MapView) findViewById(R.id.map_view);       
        mapView.setBuiltInZoomControls(true);
        
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
    
}