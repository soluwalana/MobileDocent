package edu.stanford.mdocent;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;


public class WelcomeActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.welcome);

		Button button = (Button) findViewById(R.id.login_button);
		button.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				postLoginData();
				startMap();
			}

		});

	}

	public void startMap (){
		Intent intent = new Intent(this, MobileDocentActivity.class );
		startActivity(intent);
	}
	 public void postLoginData() {
	        // Create a new HttpClient and Post Header
	        HttpClient httpclient = new DefaultHttpClient();
	         
	        /* */
	        HttpPost httppost = new HttpPost("http://www.sencide.com/blog/login.php");
	 
	        try {
	            // Add user name and password
	         EditText uname = (EditText)findViewById(R.id.txt_username);
	         String username = uname.getText().toString();
	 
	         EditText pword = (EditText)findViewById(R.id.txt_password);
	         String password = pword.getText().toString();
	          
	            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	            nameValuePairs.add(new BasicNameValuePair("username", username));
	            nameValuePairs.add(new BasicNameValuePair("password", password));
	            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	 
	            // Execute HTTP Post Request
	            HttpResponse response = httpclient.execute(httppost);
	             
	            String str = inputStreamToString(response.getEntity().getContent()).toString();
	             
	            if(str.toString().equalsIgnoreCase("true"))
	            {
	             //result.setText("Login successful");   
	            }else
	            {
	             //result.setText(str);             
	            }
	 
	        } catch (ClientProtocolException e) {
	         e.printStackTrace();
	        } catch (IOException e) {
	         e.printStackTrace();
	        }
	    } 
	   
	    private StringBuilder inputStreamToString(InputStream is) {
	     String line = "";
	     StringBuilder total = new StringBuilder();
	     // Wrap a BufferedReader around the InputStream
	     BufferedReader rd = new BufferedReader(new InputStreamReader(is));
	     // Read response until the end
	     try {
	      while ((line = rd.readLine()) != null) { 
	        total.append(line); 
	      }
	     } catch (IOException e) {
	      e.printStackTrace();
	     }
	     // Return full string
	     return total;
	    }

}