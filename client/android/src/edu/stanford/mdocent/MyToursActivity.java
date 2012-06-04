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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


public class MyToursActivity extends ListActivity {

	private static final String TAG = "MyToursActivity";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.mytours);

		Vector<TourData> tourVector = DBInteract.tourUserSearch();
		if(tourVector!=null){
			TourData[] tourArr = new TourData[tourVector.size()];
			tourVector.toArray(tourArr);
			String[] tourNames = new String[tourVector.size()];
			for(int i = 0; i <tourVector.size();i++){
				tourNames[i] = tourArr[i].tourName;
			}
			Log.v(TAG, "Tour search returned: " + tourArr[0].tourName);
			setListAdapter(new ArrayAdapter<String>(MyToursActivity.this, R.layout.listitem, tourNames)); 

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



}