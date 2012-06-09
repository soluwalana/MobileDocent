package edu.stanford.mdocent;

import java.util.Vector;

import edu.stanford.mdocent.data.Node;
import edu.stanford.mdocent.data.Tour;
import edu.stanford.mdocent.data.Node.Brief;
import edu.stanford.mdocent.db.Constants;
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
	private int retTourID = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.addnode);
		
		Intent sender = getIntent();
		int tourID = sender.getExtras().getInt("tourID");
		final Double nodeLat = sender.getExtras().getDouble("nodeLat");
		final Double nodeLon = sender.getExtras().getDouble("nodeLon");
        final Tour curTour = Tour.getTourById(tourID, true);
		retTourID = tourID;
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
				
				Node node = curTour.appendNode(newNode);
				Toast.makeText(getApplicationContext(), 
						"New Node Added.", Toast.LENGTH_LONG).show();
				startCreateTourSuccess();	
			
			}
		});
		
		Button cancelButton = (Button) findViewById(R.id.button2);
		cancelButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				startCreateTourCancel();	
			}
		});

	}
	public void startCreateTourCancel (){
		Intent intent = new Intent(this, CreateTourActivity.class );
		intent.putExtra("tourID", retTourID);
		setResult(RESULT_CANCELED, intent);
	    finish();
	}
	public void startCreateTourSuccess (){
		Intent intent = new Intent(this, CreateTourActivity.class );
		intent.putExtra("tourID", retTourID);
		setResult(RESULT_OK, intent);
	    finish();
	}
	public void finishCreateTour (){
		Intent intent = new Intent(this, CreateTourActivity.class );
		setResult(Constants.RESULT_RETURN, intent);
	    finish();
	}
}