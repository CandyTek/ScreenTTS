package com.xiaoming.screentts;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/** 无障碍服务，主功能，读取点击文本 */
public class MyAccessibility extends AccessibilityService {
	private static final String TAG = MyAccessibility.class.getSimpleName();

	private boolean isNeedReadChild = true;
	private boolean isNeedReadDescription = true;
	private boolean isNeedRefresh = false;
	private boolean isIncludeSystemApp = false;
	private boolean isIncludeUserApp = true;
	private int isNeedRefreshTime = 20;

	private HashSet<String> systemAppMap;
	private HashSet<String> whiteAppMap;

	@Override
	protected void onServiceConnected() {
		Log.i(TAG,"config success!");
		initPref();
		initTts(false,null);
		initReceiver();
		AccessibilityServiceInfo accessibilityServiceInfo = new AccessibilityServiceInfo();
		// accessibilityServiceInfo.packageNames = PACKAGES;
		// accessibilityServiceInfo.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED | AccessibilityEvent.TYPE_VIEW_LONG_CLICKED;
		accessibilityServiceInfo.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED;
		// accessibilityServiceInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
		accessibilityServiceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
		accessibilityServiceInfo.notificationTimeout = 1000;
		setServiceInfo(accessibilityServiceInfo);
	}

	/** 广播接收器 */
	private void initReceiver() {
		BroadcastReceiver receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context,Intent intent) {
				if (intent.getAction() == null) return;
				String action = intent.getAction();
				if (action.equals(Constants.BROADCAST_REFRESH_TTS)) {
					Log.w(TAG,Constants.BROADCAST_REFRESH_TTS + " 收到广播刷新配置");
					initPref();
					initTts(false,null);
				} else if (action.equals(Constants.BROADCAST_CLOSE_SERVICE)) {
					Log.w(TAG,Constants.BROADCAST_CLOSE_SERVICE + " 结束自己");
					// 貌似只有大于 Android N 才能手动结束自己 https://developer.android.google.cn/reference/android/accessibilityservice/AccessibilityService.html#disableSelf()
					// https://stackoverflow.com/questions/40433449/how-can-i-programmatically-start-and-stop-an-accessibilityservice
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
						disableSelf();
					} else {
						MyAccessibility.this.stopSelf(); // 这个貌似是无效的
					}
				}
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.BROADCAST_REFRESH_TTS);
		filter.addAction(Constants.BROADCAST_CLOSE_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			registerReceiver(receiver,filter,RECEIVER_NOT_EXPORTED);
		} else {
			registerReceiver(receiver,filter);
		}
	}

	private List<String> startsWithList;
	private List<String> endsWithList;
	private List<String> containsList;
	private final HashSet<Pattern> containsRegexList = new HashSet<>();
	private final static List<String> EMPTY_LIST = new ArrayList<>();

	static {
		EMPTY_LIST.add(null);
		EMPTY_LIST.add("");
		EMPTY_LIST.add("\n");
		EMPTY_LIST.add("\t");
		EMPTY_LIST.add(" ");
		EMPTY_LIST.add("\r");
	}

	/** 读取配置 */
	private void initPref() {
		isIncludeSystemApp = SettingUtil.getBoolean(Constants.PREF_IS_INCLUDE_SYSTEMAPP,false);
		isIncludeUserApp = SettingUtil.getBoolean(Constants.PREF_IS_INCLUDE_USERAPP,false);
		isNeedReadChild = SettingUtil.getBoolean(Constants.PREF_IS_READ_CHILD,true);
		isNeedReadDescription = SettingUtil.getBoolean(Constants.PREF_IS_READ_CONTENTDESCRIPTION,true);
		isNeedRefresh = SettingUtil.getBoolean(Constants.PREF_TTS_IS_NEED_REFRESH,false);
		startsWithList = new ArrayList<>(Arrays.asList(SettingUtil.getString("pref_startswith","未播放\n").trim().split("\n")));
		endsWithList = new ArrayList<>(Arrays.asList(SettingUtil.getString("pref_endswith","").trim().split("\n")));
		containsList = new ArrayList<>(Arrays.asList(SettingUtil.getString("pref_contains","").trim().split("\n")));
		List<String> containsRegexListTemp = new ArrayList<>(Arrays.asList(SettingUtil.getString("pref_contains_regex","").trim().split("\n")));
		// 移除空值
		containsRegexListTemp.removeAll(EMPTY_LIST);
		startsWithList.removeAll(EMPTY_LIST);
		endsWithList.removeAll(EMPTY_LIST);
		containsList.removeAll(EMPTY_LIST);
		containsRegexList.clear();
		for (String s : containsRegexListTemp) {
			containsRegexList.add(Pattern.compile(s));
		}
		try {
			isNeedRefreshTime = Integer.parseInt(SettingUtil.getString(Constants.PREF_TTS_REFRESH_TIME,"20"));
		}
		catch (NumberFormatException ignored) {}

		systemAppMap = new HashSet<>();
		List<PackageInfo> packageInfos = getPackageManager().getInstalledPackages(0);
		for (PackageInfo packageInfo : packageInfos) {
			if ((ApplicationInfo.FLAG_SYSTEM & packageInfo.applicationInfo.flags) != 0)
				systemAppMap.add(packageInfo.packageName);
		}
		String saveWhiteList = SettingUtil.getString("saveWhiteList","");
		whiteAppMap = new HashSet<>(Arrays.asList(saveWhiteList.split("/")));
	}

	/** 监听无障碍事件 */
	@SuppressLint("NewApi")
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		int eventType = event.getEventType();
		// 同一个event竟然getSource()会获取到不同的结果！要记住这个方法，以后遇到类似的问题先把获取的结果在判断之前保存！
		AccessibilityNodeInfo source = event.getSource();
		String packageName;
		if (source != null) {
			// packge name被获取一次之后就会变空，所以一定要在这里一开始就先保存起来
			packageName = source.getPackageName().toString();
		} else {
			return;
		}
		Log.w(TAG,"当前包名:" + packageName + "\nEvent " + AccessibilityEvent.eventTypeToString(eventType));

		// 是否需要排除系统应用
		if (eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
			if (whiteAppMap.contains(packageName)) {
				speakSouce(source);
			} else if (isIncludeSystemApp || isIncludeUserApp) {
				if (systemAppMap.contains(packageName)) {
					if (isIncludeSystemApp) {
						speakSouce(source);
					}
				} else if (isIncludeUserApp) {
					speakSouce(source);
				}
			}
		}
	}

	/** 调用 TTS 读取文本 */
	private void speakSouce(AccessibilityNodeInfo source) {
		// 获取子控件数量
		int childCount = source.getChildCount();
		if (source.getText() != null && source.getText().toString().trim().length() > 0) {
			Log.w(TAG,"当前 Text: " + source.getText().toString());
			// toast(source.getText().toString());
			speak(source.getText().toString().trim());
		} else if (isNeedReadDescription && source.getContentDescription() != null && source.getContentDescription().toString().trim().length() > 0) {
			Log.w(TAG,"当前 ContentDescription: " + source.getContentDescription());
			// toast(source.getContentDescription().toString());
			speak(source.getContentDescription().toString().trim());
		} else if (isNeedReadChild && childCount != 0) {
			Log.w(TAG,"总共有 " + childCount + " 个子控件");
			// 遍历子控件并获取文本
			StringBuilder sb = new StringBuilder();
			for (int i = 0;i < childCount;i++) {
				AccessibilityNodeInfo childNode = source.getChild(i);
				if (childNode != null && childNode.getText() != null) {
					sb.append(childNode.getText().toString());
				}
			}
			String result = sb.toString().trim();
			// toast(sb.toString());
			if (result.length() > 0) {
				speak(result);
			} else {
				Log.w(TAG,"子控件文本为空");
			}
		} else {
			Log.e(TAG,"获取文本失败\n" + source);
		}
	}

	private long lastReadCurrectMills = SystemClock.elapsedRealtime();

	/** 调用 TTS 说话，并包装是否需要刷新 */
	private void speak(@NonNull String text) {
		// 过滤开头包含
		for (String s : startsWithList) {
			if (text.startsWith(s)) {
				return;
			}
		}
		// 过滤结尾包含
		for (String s : endsWithList) {
			if (text.endsWith(s)) {
				return;
			}
		}
		// 过滤完整内容包含
		if (containsList.contains(text)) {
			return;
		}
		// 过滤正则包含
		for (Pattern pattern : containsRegexList) {
			if (pattern.matcher(text).matches()) {
				return;
			}
		}
		// 是否超时刷新引擎
		if (isNeedRefresh) {
			if (((SystemClock.elapsedRealtime() - lastReadCurrectMills) / 60000) > isNeedRefreshTime) {
				initTts(true,text);
			} else {
				// TextToSpeech.QUEUE_FLUSH，QUEUE_DESTROY
				tts.speak(text,2,null,"DEFAULT");
			}
			lastReadCurrectMills = SystemClock.elapsedRealtime();
		} else {
			tts.speak(text,2,null,"DEFAULT");
		}
	}

	private void toast(String text) {
		Toast.makeText(getApplicationContext(),text,Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onInterrupt() {
		// TODO Auto-generated method stub

	}

	private TextToSpeech tts;

	/** 初始化 TTS */
	private void initTts(boolean needSpeak,@Nullable String text) {
		TextToSpeech.OnInitListener listener = status -> {
			if (status == TextToSpeech.SUCCESS) {
				tts.setLanguage(Locale.CHINA);
				if (needSpeak) tts.speak(text,TextToSpeech.QUEUE_FLUSH,null,"DEFAULT");
			} else {
				toast("TTS 引擎初始化失败！");
			}
		};
		tts = new TextToSpeech(this.getApplicationContext(),listener);
	}

}
