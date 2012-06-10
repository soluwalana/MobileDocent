package edu.stanford.mdocent.data;

import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Page {
	private String pageId = null;
	private Vector<Section> sections = null;

	public Page(){
		sections = new Vector<Section>();
	}

	public Page(JsonElement je){
		if (!je.isJsonObject()){
			return;
		}
		JsonObject page = je.getAsJsonObject();
		if (!page.has("pageId") || !page.has("page")){
			return;
		}
		pageId = page.get("pageId").getAsString();
		if (!page.get("page").isJsonArray()){
			return;
		}
		JsonArray pageSections = page.get("page").getAsJsonArray();
		sections = new Vector<Section>();
		Gson gson = new Gson();
		for (int i = 0; i < pageSections.size(); i ++){
			sections.add(gson.fromJson(pageSections.get(i), Section.class));
		}
	}

	public void appendSection(Section section){
		sections.add(section);
	}
	public void insertSection(Section section, int idx){
		if (idx > 0 && idx < sections.size()){
			sections.insertElementAt(section, idx);
		}
	}

	public void removeSection(int idx){
		if (idx > 0 && idx < sections.size()){
			sections.remove(idx);
		}
	}

	public Vector<Section> getSections(){
		return sections;
	}

	public String getPageId(){
		return pageId;
	}
}
