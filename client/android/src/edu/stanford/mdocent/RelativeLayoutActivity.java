package edu.stanford.mdocent;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.EditText;


public class RelativeLayoutActivity extends Activity {

	
	public static final String TAG = "Relative Layout";
	
	@Override
	public void onCreate(Bundle b){
		super.onCreate(b);
		DisplayMetrics metrics = this.getResources().getDisplayMetrics();
		int width = metrics.widthPixels - 10;
		int height = metrics.heightPixels - 85;
		Log.v(TAG, "Width "+width+" Height "+height);
		EditText tv1 = new EditText(this);
		EditText tv2 = new EditText(this);
		EditText tv3 = new EditText(this);
		EditText tv4 = new EditText(this);
		
		Button b1 = new Button(this);
		Button b2 = new Button(this);
		Button b3 = new Button(this);
		Button b4 = new Button(this);
		
		b1.setText("Button 1");
		b2.setText("Button 2");
		b3.setText("Button 3");
		b4.setText("Button 4");
		
		int quarterHeight = (height-50)/2;
		int quarterWidth = (width-50)/2; 
		
		RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(width/2, height/2); 
		params1.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params1.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		tv1.setHeight(quarterHeight);
		tv1.setWidth(quarterWidth);
		tv1.setGravity(Gravity.TOP);
		tv1.setPadding(25, 25, 25, 25);
		
		RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(width/2, height/2); 
		params2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		params2.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		tv2.setHeight(quarterHeight);
		tv2.setWidth(quarterWidth);
		tv2.setGravity(Gravity.TOP);
		tv2.setPadding(25, 25, 25, 25);

		RelativeLayout.LayoutParams params3 = new RelativeLayout.LayoutParams(width/2, height/2); 
		params3.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params3.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		tv3.setHeight(quarterHeight);
		tv3.setWidth(quarterWidth);
		tv3.setGravity(Gravity.TOP);
		tv3.setPadding(25, 25, 25, 25);
		
		RelativeLayout.LayoutParams params4 = new RelativeLayout.LayoutParams(width/2, height/2); 
		params4.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		params4.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		tv4.setHeight(quarterHeight);
		tv4.setWidth(quarterWidth);
		tv4.setPadding(25, 25, 25, 25);
		tv4.setGravity(Gravity.TOP);
		LinearLayout buttonLayout = new LinearLayout(this);
		buttonLayout.setOrientation(LinearLayout.VERTICAL);
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(90, 90);
		params.setMargins(10, 10, 10, 10);
		buttonLayout.addView(b1, params);
		buttonLayout.addView(b2, params);
		buttonLayout.addView(b3, params);
		buttonLayout.addView(b4, params);
		
		
		RelativeLayout layout = new RelativeLayout(this);
		
		layout.addView(tv1, params1);
		layout.addView(tv2, params2);
		layout.addView(tv3, params3);
		layout.addView(tv4, params4);
		//layout.addView(buttonLayout, params4);
		
		setContentView(layout);
	}
}
