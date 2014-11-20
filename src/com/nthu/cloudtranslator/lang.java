package com.nthu.cloudtranslator;

public class lang {
	private String language;
	private String description;
	private String callNumber;
	private int iconId;
	
	public lang(String language, String description, String callNumber, int iconId) {
		super();
		this.language = language;
		this.description = description;
		this.callNumber = callNumber;
		this.iconId = iconId;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCallNumber() {
		return callNumber;
	}

	public void setCallNumber(String callNumber) {
		this.callNumber = callNumber;
	}

	public int getIconId() {
		return iconId;
	}

	public void setIconId(int iconId) {
		this.iconId = iconId;
	}
	
	
	
}
