package com.lany.easytouch.service;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.lany.easytouch.widget.EasyTouchView;

/**
 * 辅助服务
 */
public class AuxiliaryService extends Service implements EasyTouchView.ServiceListener {
	private Intent mIntent;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void onCreate() {
		// 创建service时一个 实例化一个TableShowView对象并且调用他的fun()方法把它注册到windowManager上
		super.onCreate();
		new EasyTouchView(this, this).initTouchViewEvent();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mIntent = intent;
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void OnCloseService(boolean isClose) {
		stopService(mIntent);
	}
}