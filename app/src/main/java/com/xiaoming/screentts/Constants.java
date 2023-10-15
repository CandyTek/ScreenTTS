package com.xiaoming.screentts;

public class Constants {

	// 加个随机字符串作结尾，防止其他应用伪造广播
	
	public static final String BROADCAST_REFRESH_TTS = BuildConfig.APPLICATION_ID + ".REFRESH_TTS" + RandomStringUtil.generateRandomString(5);
	public static final String BROADCAST_CLOSE_SERVICE = BuildConfig.APPLICATION_ID + ".CLOSE_SERVICE" + RandomStringUtil.generateRandomString(5);

	// 配置 Key
	
	public static final String PREF_IS_READ_CHILD = "pref_is_read_child";
	public static final String PREF_IS_INCLUDE_SYSTEMAPP = "pref_is_include_systemapp";
	public static final String PREF_IS_READ_CONTENTDESCRIPTION = "pref_is_read_contentdescription";
	public static final String PREF_TTS_IS_NEED_REFRESH = "pref_tts_is_need_refresh_regularly";
	public static final String PREF_TTS_REFRESH_TIME = "pref_tts_refresh_time";
}
