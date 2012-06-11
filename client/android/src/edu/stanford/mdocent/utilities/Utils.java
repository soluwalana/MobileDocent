package edu.stanford.mdocent.utilities;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class Utils {

	private static final String TAG = "Utils";

	public static File getTempFile(Context context, String ext) throws IOException{
		if (ext == null){
			ext = "";
		}
		String tempFileName = context.getFilesDir().getPath().toString();
		tempFileName += "/"+UUID.randomUUID().toString()+ext;
		Log.v(TAG, tempFileName);
		File tempFile = new File(tempFileName);
		tempFile.createNewFile();
		return tempFile;
	}

	public static File getRealFile(String ext) throws IOException{
		if (ext == null){
			ext = "";
		}
		return new File (Environment.getExternalStorageDirectory(), UUID.randomUUID().toString()+ext);
	}

	/* Class to get around BitmapFactory.decodeStream bug */
	public static class FlushedInputStream extends FilterInputStream {
		public FlushedInputStream(InputStream inputStream) {
			super(inputStream);
		}

		@Override
		public long skip(long n) throws IOException {
			long totalBytesSkipped = 0L;
			while (totalBytesSkipped < n) {
				long bytesSkipped = in.skip(n - totalBytesSkipped);
				if (bytesSkipped == 0L) {
					int b = read();
					if (b < 0) {
						break;  // we reached EOF
					} else {
						bytesSkipped = 1; // we read one byte
					}
				}
				totalBytesSkipped += bytesSkipped;
			}
			return totalBytesSkipped;
		}
	}
}
