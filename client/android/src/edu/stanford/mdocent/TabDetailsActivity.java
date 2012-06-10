package edu.stanford.mdocent;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class TabDetailsActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String title = getIntent().getStringExtra("node_title");
		String details = getIntent().getStringExtra("node_details");
		setContentView(R.layout.details_layout);
		TextView text = (TextView) findViewById(R.id.textView1);
		text.setText(title);
		TextView text2 = (TextView) findViewById(R.id.textView2);
		text2.setText(details);
	}
}
