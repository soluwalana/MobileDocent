package edu.stanford.mdocent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;


public class TourSearchActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.toursearch);

		Button loginButton = (Button) findViewById(R.id.Button00);
		loginButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				EditText searchText = (EditText)findViewById(R.id.editText1);
				String searchStr = searchText.getText().toString();

				DBInteract.tourKeywordSearch(searchStr);

				
			}

		});
		

	}



}