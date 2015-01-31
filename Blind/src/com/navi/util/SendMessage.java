package com.navi.util;

import com.navi.client.Constant;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.telephony.SmsManager;

public abstract class SendMessage extends Activity{
    
	public void SendAMessage(String location){
		PendingIntent paIntent;
	    SmsManager smsManager;
		paIntent = PendingIntent.getBroadcast(this, 0, new Intent(), 0); 
        smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(Constant.receiver, Constant.userName, Constant.detail, paIntent, 
                null); 
	}

}
