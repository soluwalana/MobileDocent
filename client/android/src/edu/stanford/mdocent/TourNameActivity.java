package edu.stanford.mdocent;

import java.util.Vector;

import data.TourData;
import db.DBInteract;
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


public class TourNameActivity extends Activity {
	
	private static final String TAG = "TourNameActivity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.tourname);

		Button loginButton = (Button) findViewById(R.id.button1);
		loginButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				EditText nameText = (EditText)findViewById(R.id.editText1);
				String nameStr = nameText.getText().toString();
				EditText descriptionText = (EditText)findViewById(R.id.editText2);
				String descriptionStr = descriptionText.getText().toString();
				startCreateTour(nameStr, descriptionStr);
				
			}

		});
		

	}
	public void startCreateTour (String nameStr, String descriptionStr){
		Intent intent = new Intent(this, CreateTourActivity.class );
		intent.putExtra("tourName", nameStr);
		intent.putExtra("tourDescription", descriptionStr);
		startActivity(intent);
	}



}