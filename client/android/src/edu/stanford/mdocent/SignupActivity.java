package edu.stanford.mdocent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class SignupActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.signup);

		Button button = (Button) findViewById(R.id.signup_button);
		button.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
					startMap();
				
			}

		});

	}

	public void startMap (){
		Intent intent = new Intent(this, MobileDocentActivity.class );
		startActivity(intent);
	}


}