package com.xiaoming.screentts;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

public class TestActivity1 extends Activity {
	private static final String TAG = TestActivity1.class.getSimpleName();

	private ActivityMainBinding binding;
	private TextToSpeech tts;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityMainBinding.inflate(getLayoutInflater());
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
			sendCustomBroadcast();
		});
		// 前往无障碍设置
		binding.btnRunningStatus.setOnClickListener(v -> {
			Intent intent = new Intent();
			intent.setAction(Settings.ACTION_ACCESSIBILITY_SETTINGS);
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
			openBrowser(this,"https://github.com/jing332/tts-server-android/releases");
		});


	}

	/** 菜单_初始化  */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater=getMenuInflater();
		menuInflater.inflate(R.menu.menu_home, menu);
		return true;
	}

	/** 菜单_功能相关  */
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		if (item.getItemId() == R.id.menu_quit_app) {
			System.exit(0);
			return true;
		} else if (item.getItemId() == R.id.menu_about) {

			return true;
		} else if (item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public static void openBrowser(Context context,String url) {
		final Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		// 注意此处的判断intent.resolveActivity()可以返回显示该Intent的Activity对应的组件名
		// 官方解释 : Name of the component implementing an activity that can display the intent
		if (intent.resolveActivity(context.getPackageManager()) != null) {
			final ComponentName componentName = intent.resolveActivity(context.getPackageManager()); // 打印Log   ComponentName到底是什么 L.d("componentName = " + componentName.getClassName());
			context.startActivity(Intent.createChooser(intent,"请选择浏览器"));
		} else {
			Toast.makeText(context.getApplicationContext(),"请下载浏览器",Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		boolean isRunning = isServiceRunning(this,MyAccessibility.class);
		binding.tvServiceRunningStatus.setText("小明点读 " + (isRunning ? "正在运行！" : "未在运行！"));
		binding.tvServiceRunningStatusSub.setText((isRunning ? "版本 " +BuildConfig.VERSION_NAME :  "前往无障碍设置页面，找到并点击“小明点读”一项，选择开启或关闭"));
	}

	/** 检查指定服务是否正在运行 */
	public static boolean isServiceRunning(Context context,Class<?> serviceClass) {
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

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

	/** 在需要发送广播的地方调用此方法 */
	private void sendCustomBroadcast() {
		Intent intent = new Intent(Constants.ACTION_CUSTOM_BROADCAST);
		this.sendBroadcast(intent);
		Log.w(TAG,"sendCustomBroadcast: 发送广播");
	}

}
