package com.navi.blind;

import java.util.List;

import com.navi.client.Config;
import com.navi.client.Conmmunication;
import com.navi.client.Constant;
import com.navi.model.Path;
import com.navi.util.Database;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class DownloadActivity extends BaseActivity {

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

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Builder dialog = new AlertDialog.Builder(DownloadActivity.this)
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
