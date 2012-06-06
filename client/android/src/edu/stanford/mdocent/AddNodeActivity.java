package edu.stanford.mdocent;

import java.util.Vector;

import edu.stanford.mdocent.data.Node;
import edu.stanford.mdocent.data.Tour;
import edu.stanford.mdocent.data.Node.Brief;
import edu.stanford.mdocent.db.DBInteract;
import edu.stanford.mdocent.utilities.Callback;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class AddNodeActivity extends Activity {
	
	private static final String TAG = "AddNodeActivity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.addnode);
		
		Intent sender = getIntent();
		int tourID = sender.getExtras().getInt("tourID");
		final Double nodeLat = sender.getExtras().getDouble("nodeLat");
		final Double nodeLon = sender.getExtras().getDouble("nodeLon");
		final Tour curTour = Tour.getTourById(tourID);

		Button loginButton = (Button) findViewById(R.id.button1);
		loginButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				EditText nameText = (EditText)findViewById(R.id.editText1);
				String nameStr = nameText.getText().toString();
				EditText descriptionText = (EditText)findViewById(R.id.editText2);
				String descriptionStr = descriptionText.getText().toString();
				//ADD NODE TO TOUR
				
				Node newNode = new Node();
				Brief newBrief = newNode.getBrief();
				newBrief.setDesc(descriptionStr);
				newBrief.setTitle(nameStr);
				newNode.setLatitude(nodeLat);
				newNode.setLongitude(nodeLon);
				
				curTour.appendNode(newNode, new Callback(){
					@Override
					public void onFinish(Node node){
						Toast.makeText(getApplicationContext(), 
								"New Node Added.", Toast.LENGTH_LONG).show();
						startCreateTour();	
					}
				});
			}
		});
		
		Button cancelButton = (Button) findViewById(R.id.button2);
		cancelButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				startCreateTour();	
			}
		});
		
		Button finishButton = (Button) findViewById(R.id.button3);
		finishButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				startMyTours();	
			}
		});
	}
	public void startCreateTour (){
		Intent intent = new Intent(this, TourNameActivity.class );
		setResult(RESULT_OK, intent);
	    finish();
	}
	public void startMyTours (){
		Intent intent = new Intent(this, TourNameActivity.class );
		setResult(RESULT_CANCELED, intent);
	    finish();
	}
}