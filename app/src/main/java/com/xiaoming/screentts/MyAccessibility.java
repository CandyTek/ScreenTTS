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
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;

/** 无障碍服务，主功能，读取点击文本 */
public class MyAccessibility extends AccessibilityService {
	private static final String TAG = MyAccessibility.class.getSimpleName();

	private boolean isNeedReadChild = true;
	private boolean isNeedReadDescription = true;
	private boolean isNeedRefresh = false;
	private boolean isIncludeSystemApp = false;
	private int isNeedRefreshTime = 20;

	HashSet<String> systemAppMap;

	@Override
	protected void onServiceConnected() {
		Log.i(TAG,"config success!");
		initPref();
		initTts(false,null);
		initReceiver();
		AccessibilityServiceInfo accessibilityServiceInfo = new AccessibilityServiceInfo();
		// accessibilityServiceInfo.packageNames = PACKAGES;
		accessibilityServiceInfo.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED | AccessibilityEvent.TYPE_VIEW_LONG_CLICKED;
		// accessibilityServiceInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
		accessibilityServiceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
		accessibilityServiceInfo.notificationTimeout = 1000;
		setServiceInfo(accessibilityServiceInfo);

		systemAppMap = new HashSet<>();
		List<PackageInfo> packageInfos = getPackageManager().getInstalledPackages(0);
		for (PackageInfo packageInfo : packageInfos) {
			if ((ApplicationInfo.FLAG_SYSTEM & packageInfo.applicationInfo.flags) != 0)
				systemAppMap.add(packageInfo.packageName);
		}
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

	/** 读取配置 */
	private void initPref() {
		isIncludeSystemApp = SettingUtil.getBoolean(Constants.PREF_IS_INCLUDE_SYSTEMAPP,false);
		isNeedReadChild = SettingUtil.getBoolean(Constants.PREF_IS_READ_CHILD,true);
		isNeedReadDescription = SettingUtil.getBoolean(Constants.PREF_IS_READ_CONTENTDESCRIPTION,true);
		isNeedRefresh = SettingUtil.getBoolean(Constants.PREF_TTS_IS_NEED_REFRESH,false);
		try {
			isNeedRefreshTime = Integer.parseInt(SettingUtil.getString(Constants.PREF_TTS_REFRESH_TIME,"20"));
		}
		catch (NumberFormatException ignored) {}
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
		if (eventType == AccessibilityEvent.TYPE_VIEW_CLICKED && (isIncludeSystemApp || !systemAppMap.contains(packageName))) {
			speakSouce(source);
		}
	}

	/** 调用 TTS 读取文本 */
	private void speakSouce(AccessibilityNodeInfo source) {
		// 获取子控件数量
		int childCount = source.getChildCount();
		if (source.getText() != null && !source.getText().toString().trim().equals("")) {
			Log.w(TAG,"当前 Text: " + source.getText().toString());
			// toast(source.getText().toString());
			speak(source.getText().toString());
		} else if (isNeedReadDescription && source.getContentDescription() != null && !source.getContentDescription().toString().trim().equals("")) {
			Log.w(TAG,"当前 ContentDescription: " + source.getContentDescription());
			// toast(source.getContentDescription().toString());
			speak(source.getContentDescription().toString());
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
			// toast(sb.toString());
			if (!sb.toString().trim().equals("")) {
				speak(sb.toString());
			} else {
				Log.w(TAG,"子控件文本为空");
			}
		} else {
			Log.e(TAG,"获取文本失败\n" + source);
		}
	}

	private long lastReadCurrectMills = SystemClock.elapsedRealtime();

	/** 调用 TTS 说话，并包装是否需要刷新 */
	private void speak(String text) {
		if (isNeedRefresh) {
			if (((SystemClock.elapsedRealtime() - lastReadCurrectMills) / 60000) > isNeedRefreshTime) {
				initTts(true,text);
			} else {
				tts.speak(text,TextToSpeech.QUEUE_FLUSH,null,"DEFAULT");
			}
			lastReadCurrectMills = SystemClock.elapsedRealtime();
		} else {
			tts.speak(text,TextToSpeech.QUEUE_FLUSH,null,"DEFAULT");
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
