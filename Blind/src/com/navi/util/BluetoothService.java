package com.navi.util;

import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.navi.blind.BaseActivity;
import com.navi.client.Config;
import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class BluetoothService extends Service {
	private final static String TAG = "bluetooth lanlan:";
	private final static String ADDRESS = "20:14:02:17:23:04";

	/* deviceActivity.java */
	/* ȡ��Ĭ�ϵ����������� */
	private BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();

	/* Bluetooth.java */
	enum ServerOrCilent {
		NONE, CILENT
	};

	static String BlueToothAddress = "null";
	static ServerOrCilent serviceOrCilent = ServerOrCilent.NONE;

	/* chatActivity.javaһЩ������������������� */
	public static final String PROTOCOL_SCHEME_L2CAP = "btl2cap";
	public static final String PROTOCOL_SCHEME_RFCOMM = "btspp";
	public static final String PROTOCOL_SCHEME_BT_OBEX = "btgoep";
	public static final String PROTOCOL_SCHEME_TCP_OBEX = "tcpobex";

	private clientThread clientConnectThread = null;
	private BluetoothSocket socket = null;
	private BluetoothDevice device = null;
	private readThread mreadThread = null;;
	private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
			.getDefaultAdapter();
	private Context context;
	private int i = 1;

	public BluetoothService() {

	}

	public class MyBinder extends Binder {
		public void startService(Context context) {
			init(context);
		}
		public void stopService(){
			shutdownClient();
		}
		public void startTimer(){
			Timer mTimer = null;
			TimerTask mTimerTask = null;  
			if (mTimer == null) {  
	            mTimer = new Timer();  
	        }  
			final String path[]={"042","043","010","009","002","001","006","005","004","003","002","001","013","014","015","016"};
	        
	        if (mTimerTask == null) {  
	            mTimerTask = new TimerTask() {  
	                @Override  
	                public void run() {  
	                    do {  
	                        try {  
	                            Log.i(TAG, "sleep(5000)...");  
	                            Thread.sleep(5000);  
	                            Message msg = Message.obtain();
	                			msg.what = Config.ACK_BLUE_SUCCESS;
	                			msg.arg1 = 3;
	                			msg.obj = path[i++];
	                			if(i>15) i=0;
	                			BaseActivity.sendMessage(msg);
	                        } catch (InterruptedException e) {  
	                        }     
	                    } while (true);   
	                }  
	            };  
	        }  
	  
	        if(mTimer != null && mTimerTask != null )  
	            mTimer.schedule(mTimerTask, 10000, 10000);  
	       
		}
	}

	// ��ʼ������
	public void init(Context context) {
		this.context = context;
		// Register for broadcasts when a device is discovered
		IntentFilter discoveryFilter = new IntentFilter(
				BluetoothDevice.ACTION_FOUND);
		context.registerReceiver(mReceiver, discoveryFilter);

		// Register for broadcasts when discovery has finished
		IntentFilter foundFilter = new IntentFilter(
				BluetoothDevice.ACTION_FOUND);
		context.registerReceiver(mReceiver, foundFilter);

		// Get a set of currently paired devices
		Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

		// If there are paired devices, add each one to the ArrayAdapter
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				if (device.getAddress().equals(ADDRESS)) {
					BlueToothAddress = ADDRESS;
					mBtAdapter.cancelDiscovery();
					serviceOrCilent = ServerOrCilent.CILENT;

					this.device = mBluetoothAdapter
							.getRemoteDevice(BlueToothAddress);
					clientConnectThread = new clientThread();
					clientConnectThread.start();
					break;
				}
			}
		} else {
			mBtAdapter.startDiscovery();
		}
	}

	// ��ʼ������
	// The BroadcastReceiver that listens for discovered devices and
	// changes the title when discovery is finished
	public final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice devices = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// If it's already paired, skip it, because it's been listed
				// already
				if (devices.getBondState() != BluetoothDevice.BOND_BONDED) {
					if (devices.getAddress() == ADDRESS) {
						BlueToothAddress = ADDRESS;
						mBtAdapter.cancelDiscovery();
						serviceOrCilent = ServerOrCilent.CILENT;

						device = mBluetoothAdapter
								.getRemoteDevice(BlueToothAddress);
						clientConnectThread = new clientThread();
						clientConnectThread.start();
					}
				}
				// When discovery is finished, change the Activity title
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {

			}
		}
	};

	// �����ͻ���
	public class clientThread extends Thread {
		@SuppressLint("NewApi")
		public void run() {
			// while(true){
			try {
				// ����һ��Socket���ӣ�ֻ��Ҫ��������ע��ʱ��UUID��
				// socket =
				// device.createRfcommSocketToServiceRecord(BluetoothProtocols.OBEX_OBJECT_PUSH_PROTOCOL_UUID);

				try {
					socket = device
							.createRfcommSocketToServiceRecord(UUID
									.fromString("00001101-0000-1000-8000-00805F9B34FB"));
				} catch (Exception e) {
					Log.e("", "Error creating socket");
				}
				Log.i(TAG, "connect to bluetooth");
				socket.connect();
				Log.i(TAG, "connect to bluetooth");
				Message msg = Message.obtain();
				msg.what = Config.ACK_BLUE_CON_SUCCESS;
				BaseActivity.sendMessage(msg);
				// �����������
				mreadThread = new readThread();
				mreadThread.start();
				// break;
			} catch (IOException e) {
				// Log.i(TAG, "û����");
				e.printStackTrace();
				Log.e(TAG, "trying fallback...");

				try {
					int sdk = Integer.parseInt(Build.VERSION.SDK);
					if (sdk >= 10) {
						socket = device
								.createInsecureRfcommSocketToServiceRecord(UUID
										.fromString("00001101-0000-1000-8000-00805F9B34FB"));
					} else {
						socket = device
								.createRfcommSocketToServiceRecord(UUID
										.fromString("00001101-0000-1000-8000-00805F9B34FB"));
					}
					socket.connect();
					Log.e(TAG, "Connected");
				} catch (Exception e1) {
					e1.printStackTrace();
					Message msg = Message.obtain();
					msg.what = Config.FAIl;
					BaseActivity.sendMessage(msg);
				}
			}
			// }
		}
	};

	public void endFun(int tempBytes,byte[] info_temp){
		
		int info_len = info_temp[2];
		byte[] info = new byte[info_len];
		System.arraycopy(info_temp, 3, info, 0,
				info_len);
		int checkid = info_temp[1];
		if(checkid!=1){
			Message msg = Message.obtain();
			msg.what = Config.ACK_BLUE_SUCCESS;
			msg.arg1 = info_len;
			String m_file_string = "";
			
			if(lastByte==info[0]){
				return;
			}
			
			for (int k = 0; k < info_len; k++) {
				if(info[k]<=0 || info[k] > 45)
					return;
				m_file_string += info[k];
				lastByte=info[k];
			}
			if(info[0]<10)
				m_file_string = "00" + m_file_string;
			else
				m_file_string = "0" + m_file_string;
			msg.obj = m_file_string;
			BaseActivity.sendMessage(msg);
		}else{
			Message msg = Message.obtain();
			msg.what = Config.ACK_BLUE_M_SUCCESS;
			msg.arg1 = info_len;
			String m_file_string = "";
			for (int k = 0; k < info_len; k++) {
				m_file_string += info[k];
			}
			msg.obj = m_file_string;
			BaseActivity.sendMessage(msg);
		}		
	}
	private byte lastByte=0;
	// ��ȡ���
	public class readThread extends Thread {
		public void run() {
			byte[] buffer = new byte[1024];
			byte[] info_temp = new byte[0];
			int bytes;
			InputStream mmInStream = null;
			int tempBytes = -1;
			// ���ڶ�ȡͼƬ
			try {
				mmInStream = socket.getInputStream();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			while (true) {
				try {
					
					if ((bytes = mmInStream.read(buffer)) > 0) {
						String file_string = "";
						for (int k = 0; k < bytes; k++) {
							file_string += buffer[k]+",";
						}
						Log.i(Config.TAG, file_string);
						for (int i = 0; i < bytes; i++) {
							if (buffer[i] == 0x40) {// 开始
								//Log.i(TAG, "检测到文件开始");
								info_temp = new byte[0];
								tempBytes = 0;
							}
							if (tempBytes != -1 && buffer[i] != -1 ) {
								byte[] temp = new byte[tempBytes + 1];
								System.arraycopy(info_temp, 0, temp, 0,
										info_temp.length);
								System.arraycopy(buffer, i, temp,
										info_temp.length, 1);
								info_temp = temp;
								tempBytes++;
							} else if (tempBytes != -1 && buffer[i] == -1 ) {
								//Log.i(TAG, "检测到文件结束");

								byte[] temp = new byte[tempBytes + 1];
								System.arraycopy(info_temp, 0, temp, 0,
										info_temp.length);
								System.arraycopy(buffer, i, temp,
										info_temp.length, 1);
								info_temp = temp;
								endFun(tempBytes,info_temp);
								tempBytes = -1;
								//((Flushable) mmInStream).flush();
							}
						}
					}
				} catch(ArrayIndexOutOfBoundsException  e){
				}catch(NegativeArraySizeException e){
				}catch (IOException e) {
					try {
						mmInStream.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}

	public void shutdownClient() {
		new Thread() {
			public void run() {
				if (clientConnectThread != null) {
					clientConnectThread.interrupt();
					clientConnectThread = null;
				}
				if (mreadThread != null) {
					mreadThread.interrupt();
					mreadThread = null;
				}
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					socket = null;
				}
			};
		}.start();
	}

	// �������
	public void sendMessageHandle(String msg) {
		if (socket == null) {
			return;
		}
		try {
			OutputStream os = socket.getOutputStream();
			os.write(msg.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private MyBinder mBinder = new MyBinder();

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
}
