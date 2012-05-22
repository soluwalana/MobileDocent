package edu.stanford.mdocent;

import java.util.Vector;

import data.TourData;
import db.DBInteract;
import android.app.Activity;
import android.app.ListActivity;
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


public class TourSearchActivity extends ListActivity {
	
	private static final String TAG = "TourSearchActivity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.toursearch);

		Button loginButton = (Button) findViewById(R.id.Button00);
		loginButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				EditText searchText = (EditText)findViewById(R.id.editText1);
				String searchStr = searchText.getText().toString();
				
				Vector<TourData> tourVector = DBInteract.tourKeywordSearch(searchStr);
				if(tourVector!=null){
					TourData[] tourArr = new TourData[tourVector.size()];
					tourVector.toArray(tourArr);
					String[] tourNames = new String[tourVector.size()];
					for(int i = 0; i <tourVector.size();i++){
						tourNames[i] = tourArr[i].tourName;
					}
					Log.v(TAG, "Tour search returned: " + tourArr[0].tourName);
					setListAdapter(new ArrayAdapter<String>(TourSearchActivity.this, R.layout.listitem, tourNames)); 
					
					ListView listView = getListView();
					listView.setTextFilterEnabled(true);
			 
					listView.setOnItemClickListener(new OnItemClickListener() {
						public void onItemClick(AdapterView<?> parent, View view,
								int position, long id) {
						    // When clicked, show a toast with the TextView text
						    Toast.makeText(getApplicationContext(),
							((TextView) view).getText(), Toast.LENGTH_SHORT).show();
						}
					});
				}
				
			}

		});
		

	}



}