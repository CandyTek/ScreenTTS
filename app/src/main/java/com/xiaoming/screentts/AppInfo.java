package com.xiaoming.screentts;

public class AppInfo {
	final String pkgName;
	String label;
	final int id;
	boolean isSelected;

	public AppInfo(String pkgName,String label,int id,boolean isSelected) {
		this.pkgName = pkgName;
		this.label = label;
		this.id = id;
		this.isSelected = isSelected;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean selected) {
		isSelected = selected;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}
