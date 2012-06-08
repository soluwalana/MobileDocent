package edu.stanford.mdocent.utilities;

import java.io.File;

import com.google.gson.JsonElement;

import edu.stanford.mdocent.data.Node;
import edu.stanford.mdocent.data.Tour;

public class Callback {
	public void onFinish (JsonElement jo){}
	public void onFinish (Node node){}
	public void onFinish (Tour tour){}
	public void onFinish (File file){}
}
