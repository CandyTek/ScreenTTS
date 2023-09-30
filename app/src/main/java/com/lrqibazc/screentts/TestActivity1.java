package com.lrqibazc.screentts;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.lrqibazc.screentts.databinding.ActivityMainBinding;

import java.util.Locale;

public class TestActivity1 extends AppCompatActivity {

	private ActivityMainBinding binding;
	private TextToSpeech tts;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		initTts();
		// Speak the string
		binding.test1.setOnClickListener(v -> {
			tts.speak("你好",TextToSpeech.QUEUE_FLUSH,null,"DEFAULT");
		});



	}

	private void initTts(){
		TextToSpeech.OnInitListener listener = new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(final int status) {
				if (status == TextToSpeech.SUCCESS) {
					tts.setLanguage(Locale.CHINA);
				}
				else {
				}
			}
		};
		tts = new TextToSpeech(this.getApplicationContext(), listener);
	}
}
