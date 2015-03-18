package com.navi.blind;

import java.util.List;

import com.navi.client.Config;
import com.navi.client.Conmmunication;
import com.navi.client.Constant;
import com.navi.model.Path;
import com.navi.util.Database;
import com.navi.util.PathOperationService;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
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
import android.widget.EditText;

public class DownloadActivity extends BaseActivity {
	private PathOperationService.MyBinder path_binder;
	private Intent intent_path_service ;
	private ServiceConnection connection_path = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			path_binder = (PathOperationService.MyBinder) service;
			path_binder.downloadInitService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			
		}
	};
	
	@Override
	protected void onStart() {
		
		super.onStart();
	}

	@Override
	protected void onDestroy() {
		stopService(intent_path_service);
		super.onDestroy();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 没有标题栏
		setContentView(R.layout.activity_download);
		Button download = (Button) findViewById(R.id.download);
		Button submit = (Button) findViewById(R.id.submit);
		Button local = (Button) findViewById(R.id.submit_local);
		Button read_local = (Button) findViewById(R.id.read_local);
		final EditText pointID = (EditText)findViewById(R.id.pointid) ;  
		con = Conmmunication.newInstance();

		download.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				con.download();
			}
		});
		submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String point = pointID.getText().toString();
				con.pathInfo(point);
			}
		});
		local.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String point = pointID.getText().toString();
				Database db = Database.getInstance(getApplicationContext());
				Path path = db.readPathInfo(point);
				Log.i(Config.TAG, path.toString());
			}
		});
		read_local.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Database db = Database.getInstance(getApplicationContext());
				//List<Path> path = db.readPaths();
				//Log.i(Config.TAG, path.toString());
				db.getUserInfo();
				Log.i(Config.TAG, Constant.userName);
			}
		});
		
	}

	public void processMessage(Message message) {
		System.out.print("执行处理信息");
		switch (message.what) {
		case Config.REQUEST_DOWNLOAD:
			List<Path> list = (List<Path>) message.obj;
			Database db = Database.getInstance(this);
			db.writePaths(list);
			intent_path_service = new Intent(this,
					PathOperationService.class);
			startService(intent_path_service);
			bindService(intent_path_service, connection_path, BIND_AUTO_CREATE);
			break;
		case Config.REQUEST_PATHINFO:
			Path path = (Path) message.obj;
			Log.i(Config.TAG, path.toString());
			break;
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
	}

}
