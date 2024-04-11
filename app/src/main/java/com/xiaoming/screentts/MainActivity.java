package com.xiaoming.screentts;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
// import androidx.appcompat.app.AppCompatActivity;

import com.xiaoming.screentts.databinding.ActivityMainBinding;

import java.util.Locale;

/** {@link R.layout#activity_main} */
public class MainActivity extends Activity {
	private static final String TAG = MainActivity.class.getSimpleName();
	private static final int RESULT_SETTINGS = 101;

	private static final String EXTRA_FRAGMENT_ARG_KEY = ":settings:fragment_args_key";
	private static final String EXTRA_SHOW_FRAGMENT_ARGUMENTS = ":settings:show_fragment_args";

	private ActivityMainBinding binding;
	private TextToSpeech tts;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityMainBinding.inflate(getLayoutInflater());
		initPermission();
		setContentView(binding.getRoot());
		if (getActionBar() != null) {
			getActionBar().setElevation(0);
		}
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
				| View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
		getWindow().setNavigationBarColor(0x00000000);
		// 测试播放文本
		binding.btnRefreshTTS.setOnClickListener(v -> {
			initTts();
			sendCustomBroadcast(Constants.BROADCAST_REFRESH_TTS);
		});
		// 前往无障碍设置，并高亮显示
		binding.btnRunningStatus.setOnClickListener(v -> {
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			intent.setAction(Settings.ACTION_ACCESSIBILITY_SETTINGS);
			String componentName = new ComponentName(this,MyAccessibility.class).flattenToString();
			bundle.putString(EXTRA_FRAGMENT_ARG_KEY,componentName);
			intent.putExtra(EXTRA_FRAGMENT_ARG_KEY,componentName);
			intent.putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS,bundle);
			startActivity(intent);
		});
		// 前往系统 TTS
		binding.btnGotoSystemSettingsTTS.setOnClickListener(v -> {
			Intent intent = new Intent();
			intent.setAction("com.android.settings.TTS_SETTINGS");
			startActivity(intent);
		});
		// 下载更多TTS
		binding.btnDownloadTTS.setOnClickListener(v -> {
			tools.openBrowser(this,"https://github.com/jing332/tts-server-android/releases");
		});
		// 前往应用设置
		binding.btnGotoAppSettings.setOnClickListener(v -> {
			Intent intent = new Intent(this,SettingActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivityForResult(intent,RESULT_SETTINGS);
		});


	}

	/** 尝试自动开启无障碍权限 */
	private void initPermission() {
		if (BuildConfig.DEBUG) {
			try {
				Settings.Secure.putString(getContentResolver(),
						Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
						getPackageName() + "/." + MyAccessibility.class.getSimpleName());

				Settings.Secure.putInt(getContentResolver(),
						Settings.Secure.ACCESSIBILITY_ENABLED,1);
			}
			catch (Exception e) {
				e.printStackTrace();
			}

			Log.e(TAG,"initPermission: 尝试开启无障碍权限" + getPackageName() + "/." + MyAccessibility.class.getSimpleName());
		}
	}

	@SuppressLint("SetTextI18n")
	@Override
	protected void onResume() {
		super.onResume();
		boolean isRunning = tools.isServiceRunning(this,MyAccessibility.class);
		binding.tvServiceRunningStatus.setText("小明点读 " + (isRunning ? "正在运行！" : "未在运行！"));
		binding.tvServiceRunningStatusSub.setText((isRunning ? "版本 " + BuildConfig.VERSION_NAME : "前往无障碍设置页面，找到并点击“小明点读”一项，选择开启或关闭"));
		binding.viewRunningStatus.setBackgroundResource(isRunning ? R.drawable.circle_running : R.drawable.circle_running_false);
	}

	/** 菜单_初始化 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.menu_home,menu);
		return true;
	}

	/** 菜单_功能相关 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_quit_app) {
			sendCustomBroadcast(Constants.BROADCAST_CLOSE_SERVICE);
			// 延迟退出，防止广播来不及发
			Runnable exitRunnable = () -> System.exit(0);
			Handler handler = new Handler(Looper.getMainLooper());
			handler.postDelayed(exitRunnable,700); // 延迟执行
			finish();
			return true;
		} else if (item.getItemId() == R.id.menu_about) {

			return true;
		} else if (item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/** 初始化 TTS 引擎 */
	private void initTts() {
		TextToSpeech.OnInitListener listener = status -> {
			if (status == TextToSpeech.SUCCESS) {
				tts.setLanguage(Locale.CHINA);
				tts.speak("生活就像海洋，只有意志坚强的人才能到达彼岸",TextToSpeech.QUEUE_FLUSH,null,"DEFAULT");
			} else {
				Toast.makeText(getApplicationContext(),"初始化引擎失败",Toast.LENGTH_SHORT).show();
			}
		};
		tts = new TextToSpeech(this.getApplicationContext(),listener);
	}

	/** 发送广播 */
	private void sendCustomBroadcast(String content) {
		Intent intent = new Intent(content);
		intent.setPackage(this.getPackageName());
		this.sendBroadcast(intent);
		Log.w(TAG,"sendCustomBroadcast: 发送广播");
	}

	@Override
	protected void onActivityResult(int requestCode,int resultCode,Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				// 设置完成，发送广播刷新配置
				case RESULT_SETTINGS: {
					sendCustomBroadcast(Constants.BROADCAST_REFRESH_TTS);
				}
			}
		}
	}
}
