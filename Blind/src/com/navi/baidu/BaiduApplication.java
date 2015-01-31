package com.navi.baidu;

import android.app.Application;
import com.navi.voice.*;


import com.baidu.mapapi.SDKInitializer;

public class BaiduApplication extends Application {
	
	public VoiceService.MyBinder myBinder;

	@Override
	public void onCreate() {
		super.onCreate();
		// 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
		SDKInitializer.initialize(this);
		
	}

	public void setBinder(VoiceService.MyBinder mb){
		myBinder = mb;
	}
	
	public VoiceService.MyBinder getBinder(){
		return myBinder;
	}
}