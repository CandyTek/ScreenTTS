package com.xiaoming.screentts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * 应用设置工具类
 * application 中 SettingUtil.init(...);
 */
@SuppressWarnings("unused")
public final class SettingUtil {
	private static final String TAG = SettingUtil.class.getSimpleName();
	public static String[] myList3 = new String[]{"B","KB","MB","GB","TB"};
	/** 更改包名修改点 */
	public static final String APP_SETTING = BuildConfig.APPLICATION_ID + "_preferences";

	@SuppressLint("StaticFieldLeak")
	private static Context context;

	/** 不能实例化 */
	private SettingUtil() {}

	/** 初始化 */
	public static void init(Context context1) {
		context = context1;
	}

	/** 快速获取 SharedPreferences */
	public static SharedPreferences getDefaultSP() {
		return context.getSharedPreferences(APP_SETTING,0);
	}
	public static void putString(String key,String value) {
		getDefaultSP().edit().putString(key,value).apply();
	}
	public static void saveString(String key,String value) {
		putString(key,value);
	}
	public static String getString(String key,String defValue) {
		return getDefaultSP().getString(key,defValue);
	}
	public static void putLong(String key,long value) {
		getDefaultSP().edit().putLong(key,value).apply();
	}
	public static void saveLong(String key,long value) {
		putLong(key,value);
	}
	public static long getLong(String key,long defValue) {
		return getDefaultSP().getLong(key,defValue);
	}
	public static void putInt(String key,int value) {
		getDefaultSP().edit().putInt(key,value).apply();
	}
	public static void saveInt(String key,int value) {
		putInt(key,value);
	}
	public static int getInt(String key,int defValue) {
		return getDefaultSP().getInt(key,defValue);
	}
	public static void putBoolean(String key,boolean value) {
		getDefaultSP().edit().putBoolean(key,value).apply();
	}
	public static void saveBoolean(String key,boolean value) {
		putBoolean(key,value);
	}
	public static boolean getBoolean(String key,boolean defValue) {
		return getDefaultSP().getBoolean(key,defValue);
	}
	public static void putFloat(String key,float value) {
		getDefaultSP().edit().putFloat(key,value).apply();
	}
	public static void saveFloat(String key,float value) {
		putFloat(key,value);
	}
	public static float getFloat(String key,float defValue) {
		return getDefaultSP().getFloat(key,defValue);
	}

}
