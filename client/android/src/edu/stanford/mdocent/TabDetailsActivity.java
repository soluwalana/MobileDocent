package edu.stanford.mdocent;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class TabDetailsActivity extends Activity {
	 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String details = getIntent().getStringExtra("node_details");
		setContentView(R.layout.details_layout);
		TextView text = (TextView) findViewById(R.id.textView1);
		text.setText(details);
	}
}
