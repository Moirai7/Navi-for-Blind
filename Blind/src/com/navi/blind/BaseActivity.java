package com.navi.blind;

import java.util.LinkedList;

import com.navi.client.Conmmunication;
import com.navi.util.Database;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public abstract class BaseActivity extends Activity {
	// 将生成的Activity都放到LinkList集合中
	protected static LinkedList<BaseActivity> queue = new LinkedList<BaseActivity>();
	public static Conmmunication con;
	public static Database db;

	// 监听home键
	HomeKeyEventBroadCastReceiver receiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// 判断该Activity是否在LinkedList中，没有在的话就添加上
		if (!queue.contains(this)) {
			queue.add(this);
			System.out.println("将" + queue.getLast() + "添加到list中去");
		}

		// 监听home键
		
		
		receiver = new HomeKeyEventBroadCastReceiver();
		registerReceiver(receiver, new IntentFilter(
				Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

	}

	public abstract void processMessage(Message message);

	private static Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			default:
				 System.out.println("执行到了控制信息处理handle");
				 if(!queue.isEmpty()){
				 Log.i("提示","值="+msg.arg1+"!!!!!!"+"类型="+msg.what);
				 queue.getLast().processMessage(msg);
				 }
				break;
			}

		};
	};

	// 发送消息（、、、）
	public static void sendMessage(Message msg) {
		handler.sendMessage(msg);
	}

	class HomeKeyEventBroadCastReceiver extends BroadcastReceiver {
		static final String SYSTEM_REASON = "reason";
		static final String SYSTEM_HOME_KEY = "homekey";// home key
		static final String SYSTEM_RECENT_APPS = "recentapps";// long home key

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
				String reason = intent.getStringExtra(SYSTEM_REASON);
				if (reason != null) {
					if (reason.equals(SYSTEM_HOME_KEY)) {
						// home key处理点

					} else if (reason.equals(SYSTEM_RECENT_APPS)) {

					}
				}
			}
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onRestart() {
		super.onResume();

		// Toast.makeText(BaseActivity.this, "i'm back",
		// Toast.LENGTH_SHORT).show();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}

}
