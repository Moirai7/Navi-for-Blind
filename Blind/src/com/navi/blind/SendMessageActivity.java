package com.navi.blind;

import java.util.List;

import com.navi.client.Config;
import com.navi.client.Conmmunication;
import com.navi.client.Constant;
import com.navi.model.Path;
import com.navi.util.Database;

import android.annotation.SuppressLint;
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
import android.widget.ImageButton;
import android.widget.Toast;

public class SendMessageActivity extends BaseActivity {
	
	private ImageButton save;
	private  EditText detail ;
	private  EditText receiver;
	
	private Database db;

	@SuppressLint("ShowToast")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 没有标题栏
		setContentView(R.layout.activity_sendmessage);
		// view
		save = (ImageButton) findViewById(R.id.bt_save_detail);
		detail = (EditText)findViewById(R.id.et_detail) ;  
		receiver = (EditText)findViewById(R.id.et_receiver);
		
		
		//con = Conmmunication.newInstance();
		 //db = Database.getInstance(this);
		 


		save.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String str_dt = detail.getText().toString();
				String str_rc = receiver.getText().toString();
				finish();
				
			}
		});
		
		
	}

	public void processMessage(Message message) {
		System.out.print("执行处理信息");
		switch (message.what) {
		case Config.REQUEST_SENDREQUEST:
			int result = message.arg1;
			if(result == Config.SUCCESS){
				//成功
			}
			break;
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
	}


}
