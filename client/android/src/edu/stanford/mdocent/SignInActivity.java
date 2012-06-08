package edu.stanford.mdocent;

import java.io.FileNotFoundException;
import java.io.IOException;

import edu.stanford.mdocent.db.Administration;
import edu.stanford.mdocent.db.DBInteract;
import edu.stanford.mdocent.tests.SimpleTest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images.Media;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;


public class SignInActivity extends Activity {
	
	private static final String TAG = "SignInActivity";
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



				if (Administration.login(username, password)){

					startMainPage();
				} else {
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
		
		Button testButton = (Button) findViewById(R.id.test_button);
		testButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				Intent photoPick = new Intent(Intent.ACTION_GET_CONTENT);
				
				photoPick.setType("image/*");
				startActivityForResult(photoPick, 1);
				SimpleTest.testTourCreation();
				SimpleTest.testMultpartPost(getBaseContext());
				SimpleTest.testSaveNode(getBaseContext());
				
				
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

	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK){
			Uri chosenImageUri = data.getData();
			Log.v(TAG, chosenImageUri.toString());
			try {
				Bitmap mBitmap = Media.getBitmap(this.getContentResolver(), chosenImageUri);
				SimpleTest.testFileUpload(getBaseContext(), this, mBitmap);
				
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
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