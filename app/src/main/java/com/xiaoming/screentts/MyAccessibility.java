package com.xiaoming.screentts;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Locale;

@SuppressLint("NewApi")
public class MyAccessibility extends AccessibilityService {
	private static final String TAG = MyAccessibility.class.getSimpleName();
	private BroadcastReceiver receiver;

	@Override
	protected void onServiceConnected() {
		Log.i(TAG,"config success!");
		initTts();
		initReceiver();
		AccessibilityServiceInfo accessibilityServiceInfo = new AccessibilityServiceInfo();
		// accessibilityServiceInfo.packageNames = PACKAGES;
		accessibilityServiceInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
		accessibilityServiceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
		accessibilityServiceInfo.notificationTimeout = 1000;
		setServiceInfo(accessibilityServiceInfo);
	}

	/** 广播接收器 */
	@SuppressLint("UnspecifiedRegisterReceiverFlag")
	private void initReceiver() {
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context,Intent intent) {
				if (TextUtils.equals(intent.getAction(),Constants.ACTION_CUSTOM_BROADCAST)) {
					Log.w(TAG,"onReceive: 收到广播");
					initTts();
				}
			}
		};
		IntentFilter filter = new IntentFilter(Constants.ACTION_CUSTOM_BROADCAST);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			registerReceiver(receiver,filter,RECEIVER_NOT_EXPORTED);
		} else {
			registerReceiver(receiver,filter);
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		// TODO Auto-generated method stub
		int eventType = event.getEventType();
		String eventText = "";
		// Log.i(TAG, "==============Start====================");
		switch (eventType) {
			case AccessibilityEvent.TYPE_VIEW_CLICKED -> {
				// eventText = "TYPE_VIEW_CLICKED";
				AccessibilityNodeInfo noteInfo = event.getSource();
				if (noteInfo != null && noteInfo.getText() != null && !noteInfo.getText().toString().trim().equals("")) {
					// Log.i(TAG, noteInfo.toString());
					Log.e(TAG,noteInfo.getText().toString());
					tts.speak(noteInfo.getText().toString(),TextToSpeech.QUEUE_FLUSH,null,"DEFAULT");
				} else {
					Log.i(TAG,"noteInfo error");
				}
				return;
			}
			case AccessibilityEvent.TYPE_VIEW_FOCUSED -> eventText = "TYPE_VIEW_FOCUSED";
			case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED -> eventText = "TYPE_VIEW_LONG_CLICKED";
			case AccessibilityEvent.TYPE_VIEW_SELECTED -> eventText = "TYPE_VIEW_SELECTED";
			case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> eventText = "TYPE_VIEW_TEXT_CHANGED";
			case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> eventText = "TYPE_WINDOW_STATE_CHANGED";
			case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> eventText = "TYPE_NOTIFICATION_STATE_CHANGED";
			case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END -> eventText = "TYPE_TOUCH_EXPLORATION_GESTURE_END";
			case AccessibilityEvent.TYPE_ANNOUNCEMENT -> eventText = "TYPE_ANNOUNCEMENT";
			case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START -> eventText = "TYPE_TOUCH_EXPLORATION_GESTURE_START";
			case AccessibilityEvent.TYPE_VIEW_HOVER_ENTER -> eventText = "TYPE_VIEW_HOVER_ENTER";
			case AccessibilityEvent.TYPE_VIEW_HOVER_EXIT -> eventText = "TYPE_VIEW_HOVER_EXIT";
			case AccessibilityEvent.TYPE_VIEW_SCROLLED -> eventText = "TYPE_VIEW_SCROLLED";
			case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED -> eventText = "TYPE_VIEW_TEXT_SELECTION_CHANGED";
			case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
				// eventText = "TYPE_WINDOW_CONTENT_CHANGED";
				return;
			}
			case AccessibilityEvent.TYPE_ASSIST_READING_CONTEXT -> eventText = "TYPE_ASSIST_READING_CONTEXT";
			case AccessibilityEvent.TYPE_GESTURE_DETECTION_END -> eventText = "TYPE_GESTURE_DETECTION_END";
			case AccessibilityEvent.TYPE_GESTURE_DETECTION_START -> eventText = "TYPE_GESTURE_DETECTION_START";
			case AccessibilityEvent.TYPE_SPEECH_STATE_CHANGE -> eventText = "TYPE_SPEECH_STATE_CHANGE";
			case AccessibilityEvent.TYPE_TOUCH_INTERACTION_END -> eventText = "TYPE_TOUCH_INTERACTION_END";
			case AccessibilityEvent.TYPE_TOUCH_INTERACTION_START -> eventText = "TYPE_TOUCH_INTERACTION_START";
			case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED -> eventText = "TYPE_VIEW_ACCESSIBILITY_FOCUSED";
			case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED -> eventText = "TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED";
			case AccessibilityEvent.TYPE_VIEW_CONTEXT_CLICKED -> eventText = "TYPE_VIEW_CONTEXT_CLICKED";
			case AccessibilityEvent.TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY ->
					eventText = "TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY";
			case AccessibilityEvent.TYPE_WINDOWS_CHANGED -> eventText = "TYPE_WINDOWS_CHANGED";
		}
		eventText = eventText + ":" + eventType;
		Log.i(TAG,eventText);
		// Log.i(TAG, "=============END=====================");
	}

	@Override
	public void onInterrupt() {
		// TODO Auto-generated method stub

	}
	private TextToSpeech tts;

	private void initTts() {
		TextToSpeech.OnInitListener listener = new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(final int status) {
				if (status == TextToSpeech.SUCCESS) {
					tts.setLanguage(Locale.CHINA);
				} else {
				}
			}
		};
		tts = new TextToSpeech(this.getApplicationContext(),listener);
	}

}
