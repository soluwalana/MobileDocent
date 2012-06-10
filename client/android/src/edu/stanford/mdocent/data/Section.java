package edu.stanford.mdocent.data;

import java.io.File;

public class Section {

	/* Required Fields that will always be saved */
	private String sectionId = null;
	private String title = null;
	private Integer xpos = null;
	private Integer ypos = null;
	private Integer width = null;
	private Integer height = null;
	private String contentType = null;

	private String contentId = null;
	private String content = null;

	private transient File tempData = null;

	public Section (){}

	/**
	 * 
	 * @param tempData - Set this to stage data to be pushed on node save
	 */
	public void setTempData(File tempData) {
		this.tempData = tempData;
	}

	public File getTempData(){
		return this.tempData;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Integer getXpos() {
		return xpos;
	}

	public void setXpos(Integer xpos) {
		this.xpos = xpos;
	}

	public Integer getYpos() {
		return ypos;
	}

	public void setYpos(Integer ypos) {
		this.ypos = ypos;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getSectionId() {
		return sectionId;
	}

	public String getContentId() {
		return contentId;
	}

}
