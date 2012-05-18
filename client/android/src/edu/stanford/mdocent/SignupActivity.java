package edu.stanford.mdocent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;


public class SignupActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.signup);

		Button button = (Button) findViewById(R.id.signup_button);
		button.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				EditText uname = (EditText)findViewById(R.id.txt_username);
				String username = uname.getText().toString();

				EditText pword = (EditText)findViewById(R.id.txt_password);
				String password = pword.getText().toString();
				
				EditText conf = (EditText)findViewById(R.id.text_confirm);
				String confirm = conf.getText().toString();
				
				if(DBInteract.postSignupData(username, password, confirm)){
					startMainPage();
				}
				else {

				}
				
			}

		});

	}

	public void startMainPage (){
		Intent intent = new Intent(this, MainPageActivity.class );
		startActivity(intent);
	}


}