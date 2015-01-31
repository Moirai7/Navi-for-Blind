package com.navi.blind;

import com.navi.client.Config;
import com.navi.client.Conmmunication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class RegisterActivity extends BaseActivity {

	EditText edit_username_reg,edit_password1_reg,edit_password2_reg;
	String username,password1,password2;
	
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// ȫ��
		requestWindowFeature(Window.FEATURE_NO_TITLE);// û�б�����
		setContentView(R.layout.activity_register);
		
		Button bt_register = (Button)findViewById(R.id.bt_register);
		//ImageButton bt_cancle_register = (ImageButton)findViewById(R.id.bt_cancle_register);
		edit_username_reg =(EditText)findViewById(R.id.edit_username_reg);  
		edit_password1_reg =(EditText)findViewById(R.id.edit_password1_reg);  
		edit_password2_reg =(EditText)findViewById(R.id.edit_password2_reg);  
		
		con = Conmmunication.newInstance();
		
		bt_register.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
	
				username=edit_username_reg.getText().toString();  
				password1=edit_password1_reg.getText().toString();  
				password2=edit_password2_reg.getText().toString();
				
				if(username.length() == 0)
					Toast.makeText(RegisterActivity.this, "用户名为空", Toast.LENGTH_SHORT).show();
				else if(password1.length() == 0)
					Toast.makeText(RegisterActivity.this, "密码为空", Toast.LENGTH_SHORT).show();
				else if(password2.length() == 0)
					Toast.makeText(RegisterActivity.this, "重复密码为空", Toast.LENGTH_SHORT).show();
				else if(!password1.equals(password2))
					Toast.makeText(RegisterActivity.this, "两次密码不一致", Toast.LENGTH_SHORT).show();
				else{
					con.register(username, password1);
				}
			}
		});
		
//        bt_cancle_register.setOnClickListener(new OnClickListener() {
//        	@Override
//        	public void onClick(View arg0) {
//
//        		// TODO Auto-generated method stub
//        		Toast.makeText(Register.this, "退回到登陆界面", Toast.LENGTH_SHORT).show();
//				Intent intent = new Intent();
//				intent.setClass(Register.this,Login.class);
//				startActivity(intent);
//				finish();
//        		}
//        	});
		}
	
		public void processMessage(Message message){
			System.out.print("执行处理信息");
			if(message.what == Config.REQUEST_REGISTER){
				int result = message.arg1;
				if(result == Config.SUCCESS){
					Toast.makeText(RegisterActivity.this, username+"注册成功", Toast.LENGTH_SHORT).show();
					Intent intent = new Intent();
					intent.setClass(RegisterActivity.this,LoginActivity.class);
					startActivity(intent);
					finish();
				}
				else{
					Toast.makeText(RegisterActivity.this, username+"注册失败", Toast.LENGTH_SHORT).show();
					edit_username_reg.setText("");
					edit_password1_reg.setText("");
					edit_password2_reg.setText("");
				}
			}
		}
}