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


public class MainPageActivity extends Activity {

	private static final String TAG = "MainPageActivity";
	private final static int mainTourCreateRequestCode = 3;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);

		Button takeButton = (Button) findViewById(R.id.Button00);
		takeButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
					startSearchTour();
				
			}

		});
		
		Button createButton = (Button) findViewById(R.id.Button01);
		createButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				createTour();
				
			}

		});
		
		Button accountButton = (Button) findViewById(R.id.Button02);
		accountButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				myTours();
				
			}

		});
		
		Button signOutButton = (Button) findViewById(R.id.Button03);
		signOutButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				signIn();
				
			}

		});

	}

	public void startSearchTour (){
		Intent intent = new Intent(this, TourSearchActivity.class );
		startActivity(intent);
	}
	
	public void createTour (){
		Intent intent = new Intent(this, TourNameActivity.class );
		startActivityForResult(intent, mainTourCreateRequestCode);
	}
	
	public void myTours (){
		Intent intent = new Intent(this, MyToursActivity.class );
		startActivity(intent);
	}
	
	public void signIn (){
		Intent intent = new Intent(this, SignInActivity.class );
		startActivity(intent);
	}
	@Override
	public void onActivityResult(int requestCode,int resultCode,Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Constants.RESULT_RETURN){
			Log.v(TAG, "RESULT_RETURN");
			myTours();
		}
	}

}