package edu.stanford.mdocent;

import java.io.IOException;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class TabAudioActivity extends Activity{

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final String audio_filename = getIntent().getStringExtra("node_audio");
		setContentView(R.layout.audio_layout);

		final MediaPlayer mp = new MediaPlayer();
		mp.reset();
		try {
			

			Button play_but = (Button) findViewById(R.id.button1);
			play_but.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) { 
					try {
						mp.setDataSource(getBaseContext(), Uri.parse(audio_filename));
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					mp.setLooping(false); // Set looping
					try {
						mp.prepare();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					mp.start();
				}
			});

			Button pause_but = (Button) findViewById(R.id.button2);
			pause_but.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) { 
					mp.pause();
				}
			});
			
			Button stop_but = (Button) findViewById(R.id.button3);
			stop_but.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) { 
					mp.stop();
					mp.reset();
					//mp.release();

					//mp.prepare();
				}
			});


			//mp.start();
		}
		catch(Exception e){                 
			e.printStackTrace();
		}

	}
}
