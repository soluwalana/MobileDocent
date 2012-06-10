package edu.stanford.mdocent;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class NodeTabLayoutActivity extends TabActivity{
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Intent caller = getIntent();

		super.onCreate(savedInstanceState);
		setContentView(R.layout.node_tabs_layout);

		TabHost tabHost = getTabHost();

		// Tab for details
		TabSpec detailspec = tabHost.newTabSpec("Details");
		detailspec.setIndicator("Details", getResources().getDrawable(R.drawable.icon_details_tab));
		Intent detailsIntent = new Intent(this, TabDetailsActivity.class);
		detailsIntent.putExtra("node_details", caller.getStringExtra("node_details"));
		detailsIntent.putExtra("node_title", caller.getStringExtra("node_title"));
		detailspec.setContent(detailsIntent);

		// Tab for Photos
		TabSpec photospec = tabHost.newTabSpec("Photos");
		photospec.setIndicator("Photos", getResources().getDrawable(R.drawable.icon_photos_tab));
		Intent photosIntent = new Intent(this, TabPhotosActivity.class);
		photosIntent.putExtra("node_photo", caller.getStringExtra("node_photo"));
		photosIntent.putExtra("node_title", caller.getStringExtra("node_title"));
		photospec.setContent(photosIntent);

		// Tab for audio
		TabSpec audiospec = tabHost.newTabSpec("Audio");
		// setting Title and Icon for the Tab
		audiospec.setIndicator("Audio", getResources().getDrawable(R.drawable.icon_audio_tab));
		Intent audioIntent = new Intent(this, TabAudioActivity.class);
		audioIntent.putExtra("node_audio", caller.getStringExtra("node_audio"));
		audioIntent.putExtra("node_title", caller.getStringExtra("node_title"));
		audiospec.setContent(audioIntent);



		// Adding all TabSpec to TabHost
		tabHost.addTab(detailspec); // Adding details tab
		tabHost.addTab(photospec); // Adding photos tab
		tabHost.addTab(audiospec); // Adding audio tab

	}

}
