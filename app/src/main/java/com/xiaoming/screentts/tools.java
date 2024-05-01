package com.xiaoming.screentts;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class Tools {
	private static final String TAG = "Tools";

	/** 调用系统打开链接 */
	public static void openBrowser(Context context,String url) {
		final Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		// 注意此处的判断intent.resolveActivity()可以返回显示该Intent的Activity对应的组件名
		// 官方解释 : Name of the component implementing an activity that can display the intent
		if (intent.resolveActivity(context.getPackageManager()) != null) {
			// 打印Log   ComponentName到底是什么 L.d("componentName = " + componentName.getClassName());
			// final ComponentName componentName = intent.resolveActivity(context.getPackageManager());
			context.startActivity(Intent.createChooser(intent,"请选择浏览器"));
		} else {
			Toast.makeText(context.getApplicationContext(),"请下载浏览器",Toast.LENGTH_SHORT).show();
		}
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

	/** 发送广播 */
	public static void sendCustomBroadcast(Context context,String content) {
		Intent intent = new Intent(content);
		intent.setPackage(context.getPackageName());
		context.sendBroadcast(intent);
		Log.w(TAG,"sendCustomBroadcast: 发送广播");
	}
}
