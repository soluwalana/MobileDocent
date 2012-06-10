package edu.stanford.mdocent;

import java.io.IOException;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class TabAudioActivity extends Activity{

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final String audio_filename = getIntent().getStringExtra("node_audio");
		setContentView(R.layout.audio_layout);

		String title = getIntent().getStringExtra("node_title");
		TextView text = (TextView) findViewById(R.id.textView1);
		text.setText(title);

		try {
			final MediaPlayer mp = new MediaPlayer();
			mp.reset();
			mp.setDataSource(getBaseContext(), Uri.parse(audio_filename));
			mp.setLooping(false); // Set looping
			mp.prepare();

			Button play_but = (Button) findViewById(R.id.button1);
			play_but.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					mp.start();
				}

			});

			Button pause_but = (Button) findViewById(R.id.button2);
			pause_but.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					mp.pause();
				}
			});

			Button stop_but = (Button) findViewById(R.id.button3);
			stop_but.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					try {
						mp.stop();
						mp.reset();
						mp.setDataSource(getBaseContext(), Uri.parse(audio_filename));
						mp.setLooping(false); // Set looping
						mp.prepare();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			});

		}
		catch(Exception e){
			e.printStackTrace();
		}

	}
}
