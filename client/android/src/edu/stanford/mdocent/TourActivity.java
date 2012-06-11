package edu.stanford.mdocent;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import edu.stanford.mdocent.data.MapOverlay;
import edu.stanford.mdocent.data.Page;
import edu.stanford.mdocent.data.Road;
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
	List<GeoPoint> TourGeoPoints;
	Vector<Node> nodes;
	String tourName;
	Vector<Road> roadVec = new Vector<Road>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		//Vector<Road> roadVec = new Vector<Road>();

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

		TourGeoPoints = new ArrayList<GeoPoint>();

		for(int i = 0; i < nodes.size(); i++){
			Node curNode = nodes.get(i);
			GeoPoint gp = new GeoPoint((int)(curNode.getLatitude()* 1E6),(int)(curNode.getLongitude()* 1E6));
			TourGeoPoints.add(gp);
			OverlayItem overlayitem = new OverlayItem(gp, "", "");
			itemizedOverlay.addOverlay(overlayitem);
			Log.e("NODE_INFO", curNode.toString());
		}

		//mapOverlays.add(new RoutePathOverlay(TourGeoPoints));
		drawRoadRoute();
		mapOverlays.add(itemizedOverlay);

		mapController = mapView.getController();
		mapController.animateTo(TourGeoPoints.get(0));
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

				Log.e("shortestDistance", Integer.toString((int) shortestDistance));
				if(shortestDistance <= 400){ //if within .25 of a mile 
					mapController.setCenter(itemizedOverlay.getItem(closestNode).getPoint());
					itemizedOverlay.onTap(closestNode);
				}
				else{
					mapController.setCenter(myLocationOverlay.getMyLocation());
					mapOverlays.add(myLocationOverlay);
				}

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

	private void drawRoadRoute() {
		double prevLat = -900.0;
		double prevLong = -900.0;
		boolean firstNode = true;
		if(nodes!=null){
			for(int i = 0; i<nodes.size(); i++){
				//Log.v(TAG, "Nodes size: "+nodes.size());
				Node curNode = nodes.get(i);
				if(firstNode){
					prevLat = curNode.getLatitude();
					prevLong = curNode.getLongitude();
					firstNode = false;
				}
				else {
					double fromLat = prevLat, fromLon = prevLong, toLat =curNode.getLatitude(), toLon =curNode.getLongitude();
					//Log.v(TAG, "Prevlat: "+prevLat+" Prevlong: "+prevLong+" ToLat: " + toLat+" ToLong: "+toLon);
					String url = RoadProvider.getUrl(fromLat, fromLon, toLat, toLon);
					InputStream is = getConnection(url);
					Road mRoad = RoadProvider.getRoute(is);
					roadVec.add(mRoad);
					mHandler.sendEmptyMessage(0);
					prevLat = toLat;
					prevLong = toLon;
				}

			}
		}
	}


		Handler mHandler = new Handler() {
			@Override
			public void handleMessage(android.os.Message msg) {
				for(int i = 0; i < roadVec.size(); i++){
					Road mRoad = roadVec.get(i);
					MapOverlay mapOverlay = new MapOverlay(mRoad, mapView);
					
					mapView.getOverlays().add(mapOverlay);
					mapController = mapView.getController();
					mapController.animateTo(TourGeoPoints.get(0));
					mapController.setZoom(18); 
					//mapView.invalidate();
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

		/*@Override
	protected boolean isRouteDisplayed() {
		return false;
	}*/


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
				final Node node = nodes.get(index); 
				Brief info = node.getBrief();
				final Vector<Page> pages = node.getPages();

				AlertDialog.Builder nodeDialog = new AlertDialog.Builder(TourActivity.this);
				nodeDialog.setTitle(info.getTitle());
				nodeDialog.setMessage(info.getDesc());

				nodeDialog.setPositiveButton("More Information",
						new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						//if pages is zero or null than disable this button, else start node activity 
						if(pages != null && pages.size() > 0){
							Intent intent = new Intent(TourActivity.this, NodeActivity.class);
							intent.putExtra("node_id", node.getNodeId());
							startActivity(intent);
						}

					}
				});

				nodeDialog.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						// Do nothing and close the dialog
					}
				});

				AlertDialog dialog = nodeDialog.create();


				dialog.setOnShowListener(new OnShowListener() {

					public void onShow(DialogInterface dialog) {
						if(pages == null || pages.size() <= 0)
							((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
					}
				});

				dialog.show();



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

