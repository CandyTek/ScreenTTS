package com.xiaoming.screentts;

public class AppInfo {
	final String pkgName;
	String label;
	final int id;

	public AppInfo(String pkgName,String label,int id) {
		this.pkgName = pkgName;
		this.label = label;
		this.id = id;
	}


	public void setLabel(String label) {
		this.label = label;
	}

}
