package edu.stanford.mdocent;

import edu.stanford.mdocent.data.Node;
import edu.stanford.mdocent.db.Constants;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;


public class AddPageActivity extends Activity {

	private static final String TAG = "AddPageActivity";

	private int curTourID;
	private int curNodeID;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.addpage);
		Intent sender = getIntent();
		curTourID = sender.getExtras().getInt("tourID");
		curNodeID = sender.getExtras().getInt("nodeID");
		Log.v(TAG, "Current tour: "+curTourID+" Current Node: "+curNodeID);
		
		Toast.makeText(getApplicationContext(), 
						"Select any type of content to add.", Toast.LENGTH_LONG).show();

		Button takeButton = (Button) findViewById(R.id.button1);
		takeButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
					cancelAddPage();
				
			}

		});

	}

	public void cancelAddPage (){
		Intent intent = new Intent(this, AddNodeActivity.class );
		intent.putExtra("tourID", curTourID);
		intent.putExtra("nodeID", curNodeID);
		setResult(RESULT_CANCELED, intent);
	    finish();
	}
	

	

}