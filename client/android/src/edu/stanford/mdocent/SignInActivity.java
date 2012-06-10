package edu.stanford.mdocent;

import java.io.File;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
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
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import edu.stanford.mdocent.db.Administration;
import edu.stanford.mdocent.db.Constants;
import edu.stanford.mdocent.tests.SimpleTest;
import edu.stanford.mdocent.utilities.Utils;


public class SignInActivity extends Activity {

	private static final String TAG = "SignInActivity";
	private PopupWindow pw;

	private static Uri cameraUri ;

	private Bitmap curImage;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		curImage = (Bitmap) getLastNonConfigurationInstance();
		setContentView(R.layout.signin);

		Button loginButton = (Button) findViewById(R.id.login_button);
		loginButton.setOnClickListener(new OnClickListener(){
			@Override
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
			@Override
			public void onClick(View v) {
				startSignup();
			}

		});


		Button testButton = (Button) findViewById(R.id.test_button);
		testButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){


				Intent photoPick = new Intent(Intent.ACTION_GET_CONTENT);

				photoPick.setType("image/*");
				startActivityForResult(photoPick, Constants.RESULT_IMAGE_PICKER);
				SimpleTest.testTourCreation();
				SimpleTest.testMultpartPost(getBaseContext());
				SimpleTest.testSaveNode(getBaseContext());
			}
		});

		Button cameraButton = (Button) findViewById(R.id.camera_button);
		cameraButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){

				File newPhoto = null;
				Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				try {
					newPhoto = Utils.getRealFile();
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
				cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(newPhoto));
				cameraUri = Uri.fromFile(newPhoto);
				startActivityForResult(cameraIntent, Constants.RESULT_CAMERA);

			}
		});
		
		Button videoButton = (Button) findViewById(R.id.video_button);
		Button textButton = (Button) findViewById(R.id.text_button);
		Button audioButton = (Button) findViewById(R.id.audio_button);
		Button activityButton = (Button) findViewById(R.id.activity_button);
		activityButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				startRelativeThing();
			}
		});
				
	}
	private void startRelativeThing() {
		Intent intent = new Intent (this, RelativeLayoutActivity.class);
		startActivity(intent);
	}
	
	private class ImageDialog extends Dialog implements OnDismissListener, OnCancelListener{
		private Bitmap bmap;
		public ImageDialog(Context context, Bitmap bmap) {
			super(context);
			this.bmap = bmap;

		}

		@Override
		public void onCreate(Bundle b){
			ImageView image = new ImageView(getApplicationContext());
			image.setImageBitmap(bmap);
			Button button = new Button(getApplicationContext());
			button.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v){
					dismiss();
				}
			});
			button.setText("Dismiss");
			setTitle("Custom Dialog");

			LinearLayout layout = new LinearLayout(getApplicationContext());
			layout.setOrientation(LinearLayout.VERTICAL);
			layout.setMinimumHeight(400);

			layout.addView(image, android.view.ViewGroup.LayoutParams.MATCH_PARENT, 300);
			LayoutParams params = new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, 100);
			params.setMargins(20, 20, 20, 20);
			layout.addView(button, params);

			setOnCancelListener(this);
			setOnDismissListener(this);
			setContentView(layout);
		}
		@Override
		public void onDismiss(DialogInterface dialog){
			Log.v(TAG, "On Dismiss called");
			bmap = null;
		}

		@Override
		public void onCancel(DialogInterface dialog){
			Log.v(TAG, "On Cancel called");
			bmap = null;
		}


	}

	@Override
	public Dialog onCreateDialog(int dialogInt){
		if (curImage != null){
			return new ImageDialog(this, curImage);
		} else {
			return super.onCreateDialog(dialogInt);
		}

	}

	@Override
	public Object onRetainNonConfigurationInstance(){
		final Bitmap data = curImage;
		return data;
	}

	@Override
	public void onResume(){
		super.onResume();
		Log.v(TAG, "Resume called");
		showDialog(1);
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		//super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
		case Constants.RESULT_IMAGE_PICKER:
			if (resultCode == Activity.RESULT_OK){
				Uri chosenImageUri = data.getData();
				Log.v(TAG, chosenImageUri.toString());
				try {
					Bitmap bitmap = Media.getBitmap(getContentResolver(), chosenImageUri);
					curImage = SimpleTest.testFileUpload(getBaseContext(), getContentResolver(), bitmap);
					Log.v(TAG, "1a "+(curImage != null));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			break;
		case Constants.RESULT_CAMERA:
			if (resultCode == Activity.RESULT_OK){
				Uri selectedImage = cameraUri;
				try{
					ContentResolver cr = getContentResolver();
					cr.notifyChange(selectedImage, null);
					Bitmap inBitmap = Media.getBitmap(cr, selectedImage);
					Log.v(TAG, "Inbitmap is: "+(inBitmap != null));
					curImage = SimpleTest.testFileUpload(getBaseContext(), getContentResolver(), inBitmap);
					//curImage = SimpleTest.simpleTest(inBitmap);
					Log.v(TAG, "1v "+(curImage != null));
				} catch(Exception e){
					e.printStackTrace();
				}
			}
			break;
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