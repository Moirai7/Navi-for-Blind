package com.navi.blind;



import com.navi.client.Config;
import com.navi.client.Conmmunication;
import com.navi.client.Constant;
import com.navi.util.Database;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

public class LoginActivity extends BaseActivity {

	ImageView imageView1, imageView2;
	AnimationDrawable animationDrawable1, animationDrawable2;
	EditText edit_username_log,edit_password_log;
	Database db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// 全屏
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 没有标题栏
		setContentView(R.layout.activity_login);
		
		con = Conmmunication.newInstance();
		
		init();

		ImageButton bt_log = (ImageButton)findViewById(R.id.login);
		
		ImageButton bt_register = (ImageButton)findViewById(R.id.register);
	
		
		bt_log.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				final String username;
				final String password; 
				username=edit_username_log.getText().toString();  
				password=edit_password_log.getText().toString();  
				CheckBox f = (CheckBox)findViewById(R.id.family);
				if(f.isChecked())
					Constant.type = "1";
				con.login(username, password);
			}
		});
		

        
        bt_register.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View arg0) {
        		// TODO Auto-generated method stub
    		
        		//Toast.makeText(Login.this, "register", Toast.LENGTH_SHORT).show();
        		Intent intent = new Intent();
				intent.setClass(LoginActivity.this,RegisterActivity.class);
				startActivity(intent);
				finish();
        		}
        	});
        

        
		}
	
	private void verifylogin() {
		db = Database.getInstance(this);
		if(db.getUserInfo()){
			con.login(Constant.userName, Constant.userPassword);
		}
	}

	public void processMessage(Message message){
		System.out.print("执行处理信息");
		switch(message.what){
		case Config.REQUEST_LOGIN:
			int result = message.arg1;
			if(result == Config.SUCCESS){
				Constant.userName = edit_username_log.getText().toString();
				Constant.userPassword = edit_password_log.getText().toString();
				db.setUserInfo(Constant.userName,Constant.userPassword,Constant.type);
				if(Constant.type.equals("1")){
					Toast.makeText(LoginActivity.this, "用户"+Constant.userName+"登陆成功", Toast.LENGTH_SHORT).show();
					Intent intent = new Intent();
					intent.setClass(LoginActivity.this,ShowPositionActivity.class);
					startActivity(intent);
					finish();
				}
				Toast.makeText(LoginActivity.this, "用户"+Constant.userName+"登陆成功", Toast.LENGTH_SHORT).show();
				Intent intent = new Intent();
				intent.setClass(LoginActivity.this,MainActivity.class);
				startActivity(intent);
				finish();
			}
			break;
		case Config.FAIl:
			Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
			break;
		case Config.CON_SUCCESS:
	        verifylogin();
	        break;
		}
	}
	
	public void init() { 
		//imageView1 = (ImageView) findViewById(R.id.anim_yunduo);
		//imageView2 = (ImageView) findViewById(R.id.circle_animation);
		//animationDrawable1 = (AnimationDrawable) imageView1.getBackground();
		//animationDrawable2 = (AnimationDrawable) imageView2.getBackground();
		edit_username_log =(EditText)findViewById(R.id.et_username);  
		edit_password_log =(EditText)findViewById(R.id.et_psw); 
	}
	

	


}
