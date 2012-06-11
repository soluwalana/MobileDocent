package edu.stanford.mdocent;

import java.util.ArrayList;
import java.util.Vector;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

public class TourItemizedOverlay extends ItemizedOverlay<OverlayItem> {

	private Context mContext;
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	//private Paint linePaint;
	private Vector<GeoPoint> points;

	/*GeoPoint prevPoint=null, currentPoint=null;
	MapView mapView=null;
	Paint paint=new Paint();*/


	public TourItemizedOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
		/*this.currentPoint=currentPoint;
	    this.prevPoint = prePoint;
	    mapView=mapview;*/
		points = new Vector<GeoPoint>();
	    //set colour, stroke width etc.
	   /* linePaint = new Paint();
	    linePaint.setARGB(255, 255, 0, 0);
	    linePaint.setStrokeWidth(3);
	    linePaint.setDither(true);
	    linePaint.setStyle(Style.FILL);
	    linePaint.setAntiAlias(true);
	    linePaint.setStrokeJoin(Paint.Join.ROUND);
	    linePaint.setStrokeCap(Paint.Cap.ROUND);*/

	}
	
	/*@Override
	public void draw(Canvas canvas, MapView view, boolean shadow) {
		super.draw(canvas, view, shadow);

        Paint mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(2);

        int size = points.size();
	    Point lastPoint = new Point();
	    if(size == 0) return;
	    view.getProjection().toPixels(points.get(0), lastPoint);
	    Point point = new Point();
	    Path path = new Path();
	    for(int i = 1; i < size; i++){
	    	 path.moveTo(point.x, point.y);
	         path.lineTo(lastPoint.x,lastPoint.y);
	         canvas.drawPath(path, mPaint);
	         lastPoint = point;
	    }

	}*/



	public TourItemizedOverlay(Drawable defaultMarker, Context context) {
		super(boundCenterBottom(defaultMarker));
		mContext = context;
	}

	@Override
	protected boolean onTap(int index) {
		OverlayItem item = mOverlays.get(index);
		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		dialog.setTitle(item.getTitle());
		dialog.setMessage(item.getSnippet());
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
		points.addElement(overlay.getPoint());

	}

}
