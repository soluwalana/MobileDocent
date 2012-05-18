package edu.stanford.mdocent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;


public class SignInActivity extends Activity {
	
	private PopupWindow pw;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.signin);

		Button loginButton = (Button) findViewById(R.id.login_button);
		loginButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				EditText uname = (EditText)findViewById(R.id.txt_username);
				String username = uname.getText().toString();

				EditText pword = (EditText)findViewById(R.id.txt_password);
				String password = pword.getText().toString();

				if(DBInteract.postLoginData(username, password)){
					startMainPage();
				}
				else {
					popUpNotification();
				}
			}

		});
		
		Button signupButton = (Button) findViewById(R.id.signup_button);
		signupButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
					startSignup();		
			}

		});

	}

	public void startMainPage (){
		Intent intent = new Intent(this, MainPageActivity.class );
		startActivity(intent);
	}
	

	public void startSignup (){
		Intent intent = new Intent(this, SignupActivity.class );
		startActivity(intent);
	}

	public void restartWelcome (){
		Intent intent = new Intent(this, SignInActivity.class );
		startActivity(intent);
	}
	
	public void popUpNotification () {
		   // Make a View from our XML file
		   LayoutInflater inflater = (LayoutInflater)
		         this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		   View layout = inflater.inflate(R.layout.popup,
		         (ViewGroup) findViewById(R.id.MyLinearLayout));
		 
		   pw = new PopupWindow( layout,  350,  250,  true);
		   pw.showAtLocation(layout, Gravity.CENTER, 0, 0);
		   
		   
		}
	
	public void closePopUp (View target) {
		pw.dismiss();
	}

}