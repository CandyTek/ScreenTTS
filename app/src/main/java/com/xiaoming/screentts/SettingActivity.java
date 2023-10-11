package com.xiaoming.screentts;

import android.os.Bundle;
import android.preference.PreferenceActivity;

@SuppressWarnings("deprecation")
public class SettingActivity extends PreferenceActivity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setResult(RESULT_OK);
		addPreferencesFromResource(R.xml.pref1);
		
		findPreference(Constants.PREF_TTS_REFRESH_TIME).setEnabled(SettingUtil.getBoolean(Constants.PREF_TTS_IS_NEED_REFRESH,false));
		
		findPreference(Constants.PREF_TTS_IS_NEED_REFRESH).setOnPreferenceChangeListener((preference,newValue) -> {
			findPreference(Constants.PREF_TTS_REFRESH_TIME).setEnabled((boolean) newValue);
			return true;
		});
	}

}
