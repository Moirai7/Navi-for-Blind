package com.navi.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

public abstract class Bluetooth {
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
	static boolean isOpen = false;

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
	
	public Bluetooth(Context context){
		this.context = context;
	}
	// ��ʼ������
	public void init() {
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
					isOpen = true;
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
						isOpen = true;
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
				Log.i(TAG, "��ʼ��");
				socket.connect();
				Log.i(TAG, "������");
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
				}
			}
			// }
		}
	};

	// ��ȡ���
	public class readThread extends Thread {
		public void run() {
			byte[] buffer = new byte[1024];
			int bytes;
			InputStream mmInStream = null;
			// ���ڶ�ȡͼƬ
			try {
				mmInStream = socket.getInputStream();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			while (true) {
				try {
					if ((bytes = mmInStream.read(buffer)) > 0) {
						proc(bytes,buffer);
					}
				} catch (IOException e) {
					try {
						mmInStream.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					break;
				}
			}
		}
	}
	
	public abstract void proc(int bytes, byte[] buffer) ;

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

}
