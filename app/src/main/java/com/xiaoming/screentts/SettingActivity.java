package com.xiaoming.screentts;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;

@SuppressLint("ExportedPreferenceActivity")
@SuppressWarnings("deprecation")
public class SettingActivity extends PreferenceActivity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setResult(RESULT_OK);
		addPreferencesFromResource(R.xml.pref1);
		
		findPreference(Constants.PREF_TTS_REFRESH_TIME).setEnabled(SettingUtil.getBoolean(Constants.PREF_TTS_IS_NEED_REFRESH,false));
		findPreference("pref_whitelist_app").setOnPreferenceClickListener((preference) -> {
			startActivity(new Intent(this,WhitelistActivity.class));
			return false;
		});

		findPreference(Constants.PREF_TTS_IS_NEED_REFRESH).setOnPreferenceChangeListener((preference,newValue) -> {
			findPreference(Constants.PREF_TTS_REFRESH_TIME).setEnabled((boolean) newValue);
			return true;
		});

	}

	@Override
	public void finish() {
		Tools.sendCustomBroadcast(this,Constants.BROADCAST_REFRESH_TTS);
		super.finish();
	}
}
