package com.navi.blind;

import com.navi.client.Config;
import com.navi.util.BluetoothService;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;

public class NaviActivity extends BaseActivity {

	private BluetoothService.MyBinder myBinder;
	private Intent intent_main_service;
	private Context context = this;

	private ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			myBinder = (BluetoothService.MyBinder) service;
			myBinder.startService(context);

			Log.v(Config.TAG, "bind");
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 没有标题栏
		setContentView(R.layout.activity_navi);

		Button naviStart = (Button) findViewById(R.id.naviStart);
		intent_main_service = new Intent(this, BluetoothService.class);

		naviStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

			}
		});

	}

	@Override
	protected void onStart() {
		startService(intent_main_service);
		bindService(intent_main_service, connection, BIND_AUTO_CREATE);
		super.onStart();
	}

	@Override
	protected void onDestroy() {
		unbindService(connection);
		super.onDestroy();
	}

	public void processMessage(Message msg) {
		System.out.print("执行处理信息");
		switch (msg.what) {
		case Config.ACK_SERVICE:
			int bytes = msg.arg1;
			byte[] buffer = (byte[]) msg.obj;
			break;
		case Config.SUCCESS:
			Log.i(Config.TAG, "bluetooth 连接成功");
			break;
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Builder dialog = new AlertDialog.Builder(NaviActivity.this)
					.setTitle("提示")
					.setMessage("您是否要退出？")
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									con.clear();
									finish();
									int siz = BaseActivity.queue.size();
									for (int i = 0; i < siz; i++) {
										if (BaseActivity.queue.get(i) != null) {
											System.out
													.println((Activity) BaseActivity.queue
															.get(i) + "退出程序");
											((Activity) BaseActivity.queue
													.get(i)).finish();
										}
									}
								}
							})
					.setNegativeButton("取消",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub

								}
							});
			dialog.create().show();

			return true;
		}

		else
			return false;

	}

}
