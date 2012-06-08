package edu.stanford.mdocent.utilities;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import android.content.Context;
import android.util.Log;

public class Utils {
	
	private static final String TAG = "Utils";

	public static File getTempFile(Context context) throws IOException{
		String tempFileName = context.getFilesDir().getPath().toString();
		tempFileName += "/"+UUID.randomUUID().toString();
		Log.v(TAG, tempFileName);
		File tempFile = new File(tempFileName);
		tempFile.createNewFile();
		return tempFile; 
	}	
}
