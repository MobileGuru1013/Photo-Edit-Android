package com.example.photoeditor;

import com.example.photoeditor.graphics.ImageProcessor;

import android.app.Application;

public class Platform extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		ImageProcessor.getInstance().setApplicationCotnext(
				getApplicationContext());
	}
}
