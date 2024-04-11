package com.xiaoming.screentts;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;

import androidx.annotation.Nullable;

import com.xiaoming.screentts.databinding.ActivityListviewBinding;

import java.util.ArrayList;
import java.util.List;

public class WhitelistActivity extends Activity {

	ActivityListviewBinding binding;
	MainAppAdapter appAdapter;
	private SearchView searchView;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityListviewBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		if(getActionBar()!=null){
			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setHomeAsUpIndicator(R.drawable.outline_cancel_24);
		}
		initData();
	}

	private void initData() {
		List<AppInfo> mListAppInfo = new ArrayList<>();
		List<PackageInfo> packageInfos = getPackageManager().getInstalledPackages(0);
		for (int i = 0, packageInfosSize = packageInfos.size();i < packageInfosSize;i++) {
			PackageInfo packageInfo = packageInfos.get(i);
			mListAppInfo.add(new AppInfo(packageInfo.packageName,"",i));// 添加至列表中
		}

		appAdapter = new MainAppAdapter(this,mListAppInfo);

		binding.listView.setAdapter(appAdapter);
		new Thread(() -> {
			for (AppInfo appInfo : mListAppInfo) {
				PackageInfo packageInfo;
				try {
					packageInfo = getPackageManager().getPackageInfo(appInfo.pkgName,0);
				}
				catch (PackageManager.NameNotFoundException e) {
					continue;
				}
				appInfo.setLabel(getPackageManager().getApplicationLabel(packageInfo.applicationInfo).toString());
			}
			runOnUiThread(() -> appAdapter.notifyDataSetChanged());
		}).start();
	}

	/** 菜单_初始化  */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater=getMenuInflater();
		menuInflater.inflate(R.menu.menu_whitelist, menu);
		MenuItem menuItem = menu.findItem(R.id.search);//获取搜索的菜单组件
		searchView = (SearchView) menuItem.getActionView();

		searchView.setIconified(true);
		searchView.setIconifiedByDefault(true);
		searchView.setSubmitButtonEnabled(false);
		 searchView.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {
				// 当搜索 提交时
				searchView.clearFocus();  //可以收起键盘
				return false;
			}
			@Override
			public boolean onQueryTextChange(String newText) {
				// 当搜索栏文字改变时
				//mNow_SearchText = newText;
				// isNowIsInputSearchText = true;
				appAdapter.getFilter().filter(TextUtils.isEmpty(newText)?"":newText);
				//showText1("更改");
				return false;
			}
		});

		return true;
	}

	/** 菜单_功能相关  */
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		if (item.getItemId() == R.id.menuitem_done) {

			return true;
		} else if (item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
