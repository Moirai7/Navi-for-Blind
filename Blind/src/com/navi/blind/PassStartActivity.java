package com.navi.blind;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.DrivingRouteOvelray;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.baidu.mapapi.overlayutil.TransitRouteOverlay;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.iflytek.speech.SpeechRecognizer;
import com.iflytek.speech.SpeechSynthesizer;
import com.navi.baidu.BaiduApplication;
import com.navi.blind.BaseActivity;
import com.navi.blind.R;
import com.navi.client.Config;
import com.navi.client.Conmmunication;
import com.navi.client.Constant;
import com.navi.util.BluetoothService;
import com.navi.util.PathOperationService;
import com.navi.util.UploadHistoryService.MyLocationListenner;
import com.navi.voice.*;

/**
 * 此demo用来展示如何进行驾车、步行、公交路线搜索并在地图使用RouteOverlay、TransitOverlay绘制
 * 同时展示如何进行节点浏览并弹出泡泡
 */
public class PassStartActivity extends BaseActivity implements
		OnGetRoutePlanResultListener {
	// 浏览路线节点相关
	Button mBtnPre = null;// 上一个节点
	Button mBtnNext = null;// 下一个节点
	int nodeIndex = -2;// 节点索引,供浏览节点时使用
	RouteLine route = null;
	OverlayManager routeOverlay = null;
	boolean useDefaultIcon = false;
	private TextView popupText = null;// 泡泡view
	private View viewCache = null;
	private String newString;

	// 地图相关，使用继承MapView的MyRouteMapView目的是重写touch事件实现泡泡处理
	// 如果不处理touch事件，则无需继承，直接使用MapView即可
	MapView mMapView = null; // 地图View

	BaiduMap mBaidumap = null;
	// 搜索相关
	RoutePlanSearch mSearch = null; // 搜索模块，也可去掉地图模块独立使用

	// ui
	EditText editSt;
	EditText editEn;
	EditText et;

	/** 语音相关 **/

	// 语音识别对象。
	private SpeechRecognizer mIat;
	private Toast mToast;
	private static final String ACTION_INPUT = "com.iflytek.speech.action.voiceinput";
	// 语音合成对象
	private SpeechSynthesizer mTts;

	private SharedPreferences mSharedPreferences;

	private static String TAG = "路线规划";

	private int proID;

	private List<String> RouteString;

	// flag
	public static boolean flag_iat = false;
	public static boolean flag_tts = false;
	public static boolean isFirstLoc = true;

	public static boolean voice_flag= false, path_flag= false,bluetooth_flag = false;

	// 定位
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	private static String city = "北京";
	public String endpoint="unknown";

	private int CURRENT_ACK = -1;

	private Queue<BDLocation> loc_q = new ArrayBlockingQueue<BDLocation>(17);
	// 定位相关
	private TimerTask task;
	private Timer timer;
	//private Conmmunication con= Conmmunication.newInstance();

	// private Intent intent_main_service;
	private VoiceService.MyBinder voice_binder;
	private PathOperationService.MyBinder path_binder;
	private BluetoothService.MyBinder bluetooth_binder;

	private Context context = this;

	private String startpoint = "001";
	private boolean checkpoint = false;
	private boolean server_checkpoint = false;

	// private Intent intent_path,intent_bluetooth;

	private ServiceConnection connection_path = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			path_flag = false;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			path_binder = (PathOperationService.MyBinder) service;
			path_binder.initStartService();
			path_flag = true;
			Message msg = Message.obtain();
			msg.what = Config.ACK_NONE;
			BaseActivity.sendMessage(msg);
			Log.v("tag", "bind");
			//if (voice_flag)
				//StartRead("请根据提示说出终点", Config.ACK_SAY_END);
		}
	};

	private ServiceConnection connection_voice = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			voice_flag = false;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			voice_binder = (VoiceService.MyBinder) service;
			voice_flag = true;
			Log.v("tag", "bind");
			
			Message msg = Message.obtain();
			msg.what = Config.ACK_NONE;
			BaseActivity.sendMessage(msg);

			//if (path_flag)
				//StartRead("请根据提示说出终点", Config.ACK_SAY_END);
		}
	};

	private ServiceConnection connection_bluetooth = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			bluetooth_binder = (BluetoothService.MyBinder) service;
			//TODO DONE 蓝牙初始化方法
			bluetooth_binder.startService(context);
			bluetooth_flag = true;
			
			Message msg = Message.obtain();
			msg.what = Config.ACK_NONE;
			BaseActivity.sendMessage(msg);
			

			Log.v(Config.TAG, "bind");
		}
	};
	
	private String startNode;
	private String endNode;
	
	private int flagflag=0;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_routeplan);
		CharSequence titleLable = "路线规划功能";
		setTitle(titleLable);
		db.getInstance(this);
		// 初始化地图
		mMapView = (MapView) findViewById(R.id.map);
		mBaidumap = mMapView.getMap();
		mBtnPre = (Button) findViewById(R.id.pre);
		mBtnNext = (Button) findViewById(R.id.next);
		mBtnPre.setVisibility(View.INVISIBLE);
		mBtnNext.setVisibility(View.INVISIBLE);
		// 地图点击事件处理
		ImageView iv = (ImageView) findViewById(R.id.iv);

		iv.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent e) {
				// TODO Auto-generated method stub
				int ee = e.getAction();
				int eee = e.getActionMasked();

				// showTip("iv");

				switch (e.getAction() & MotionEvent.ACTION_MASK) {// &
				// MotionEvent.ACTION_MASK
				// 多点
				case MotionEvent.ACTION_DOWN:
					break;
				case MotionEvent.ACTION_UP:

					long start = e.getEventTime();
					long end = e.getDownTime();
					long total = start - end;

					if (total < 100) {
						StopListen();
						checkpoint = false;
						Log.i("lanlan", "开始说");
						Message msg = Message.obtain();
						msg.what = Config.ACK_LISTEN_END;
						BaseActivity.sendMessage(msg);
					}

					return true;
				case MotionEvent.ACTION_CANCEL:
					break;
				case MotionEvent.ACTION_POINTER_DOWN:
					finish();
					return true;

				}

				return true;
			}

		});

		// 初始化搜索模块，注册事件监听
		mSearch = RoutePlanSearch.newInstance();
		mSearch.setOnGetRoutePlanResultListener(this);

		editSt = (EditText) findViewById(R.id.et_start);
		editEn = (EditText) findViewById(R.id.et_end);

		et = (EditText) findViewById(R.id.et_start);

		// nodeIndex = 0;

		mSharedPreferences = getSharedPreferences(IatSettings.PREFER_NAME,
				Activity.MODE_PRIVATE);

		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setAddrType("all"); // 设置有返回值
		option.setScanSpan(1000);
		mLocClient.setLocOption(option);
		mLocClient.start();

		UiSettings mUiSettings = mBaidumap.getUiSettings();
		mUiSettings.setAllGesturesEnabled(false);

		// intent_main_service = new Intent(this, VoiceService.class);
		BaiduApplication app = (BaiduApplication) getApplication();

		// myBinder = app.getBinder();

		// StartRoute();
	}

	protected void StopListen() {

		voice_binder.StopListen();

	}

	protected void StartRoute() {

		// proID = id;
		route = null;
		mBtnPre.setVisibility(View.INVISIBLE);
		mBtnNext.setVisibility(View.INVISIBLE);
		mBaidumap.clear();

		String startNode = editSt.getText().toString();

		String endNode = editEn.getText().toString();

		startNode = "西直门";
		endNode = "东直门";

		// while(endNode == null){
		// endNode = editEn.getText().toString();
		// }

		// endNode = endNode.substring(0, startNode.length()-1);

		Log.v("start", startNode);
		Log.v("end", endNode);

		if (startNode == "" || endNode == "") {
			StartRead("请单击重试", Config.ACK_NONE);
		} else {
			PlanNode stNode = PlanNode
					.withCityNameAndPlaceName(city, startNode);
			PlanNode enNode = PlanNode.withCityNameAndPlaceName(city, endNode);

			mSearch.transitSearch((new TransitRoutePlanOption()).from(stNode)
					.city(city).to(enNode));
		}

	}

	protected void StartListen(int ackSayEnd) {
		voice_binder.SetACK(ackSayEnd);
		voice_binder.StartListen();

	}

	protected void StartRead(String string, int ackListenStart) {
		voice_binder.SetACK(ackListenStart);
		voice_binder.StartRead(string);
	}

	/**
	 * 节点浏览示例
	 * 
	 * @param v
	 */
	public void nodeClick(View v) {
		if (nodeIndex < -1 || route == null || route.getAllStep() == null
				|| nodeIndex > route.getAllStep().size()) {
			return;
		}
		// 设置节点索引
		if (v.getId() == R.id.next && nodeIndex < route.getAllStep().size() - 1) {
			nodeIndex++;
		} else if (v.getId() == R.id.pre && nodeIndex > 1) {
			nodeIndex--;
		}
		if (nodeIndex < 0 || nodeIndex >= route.getAllStep().size()) {
			return;
		}

		// 获取节结果信息
		LatLng nodeLocation = null;
		String nodeTitle = null;
		Object step = route.getAllStep().get(nodeIndex);
		
		
		if (step instanceof DrivingRouteLine.DrivingStep) {
			nodeLocation = ((DrivingRouteLine.DrivingStep) step).getEntrace()
					.getLocation();
			nodeTitle = ((DrivingRouteLine.DrivingStep) step).getInstructions();
		} else if (step instanceof WalkingRouteLine.WalkingStep) {
			nodeLocation = ((WalkingRouteLine.WalkingStep) step).getEntrace()
					.getLocation();
			nodeTitle = ((WalkingRouteLine.WalkingStep) step).getInstructions();
		} else if (step instanceof TransitRouteLine.TransitStep) {
			nodeLocation = ((TransitRouteLine.TransitStep) step).getEntrace()
					.getLocation();
			nodeTitle = ((TransitRouteLine.TransitStep) step).getInstructions();
		}

		if (nodeLocation == null || nodeTitle == null) {
			return;
		}
		// 移动节点至中心
		// mBaidumap.setMapStatus(MapStatusUpdateFactory.newLatLng(nodeLocation));
		// show popup
		// viewCache = getLayoutInflater()
		// .inflate(R.layout.custom_text_view, null);
		// popupText = (TextView) viewCache.findViewById(R.id.textcache);
		// popupText.setBackgroundResource(R.drawable.popup);
		// popupText.setText(nodeTitle);
		// mBaidumap.showInfoWindow(new InfoWindow(popupText, nodeLocation,
		// null));

	}

	/**
	 * 切换路线图标，刷新地图使其生效 注意： 起终点图标使用中心对齐.
	 */
	public void changeRouteIcon(View v) {
		if (routeOverlay == null) {
			return;
		}
		if (useDefaultIcon) {
			((Button) v).setText("自定义起终点图标");
			Toast.makeText(this, "将使用系统起终点图标", Toast.LENGTH_SHORT).show();

		} else {
			((Button) v).setText("系统起终点图标");
			Toast.makeText(this, "将使用自定义起终点图标", Toast.LENGTH_SHORT).show();

		}
		useDefaultIcon = !useDefaultIcon;
		routeOverlay.removeFromMap();
		routeOverlay.addToMap();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public void onGetWalkingRouteResult(WalkingRouteResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(PassStartActivity.this, "抱歉，未找到结果",
					Toast.LENGTH_SHORT).show();

		}
		if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
			// 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
			// result.getSuggestAddrInfo()
			return;
		}
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			nodeIndex = -1;
			mBtnPre.setVisibility(View.VISIBLE);
			mBtnNext.setVisibility(View.VISIBLE);
			route = result.getRouteLines().get(0);
			WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaidumap);
			mBaidumap.setOnMarkerClickListener(overlay);
			routeOverlay = overlay;
			overlay.setData(result.getRouteLines().get(0));
			overlay.addToMap();
			overlay.zoomToSpan();
		}

	}

	@Override
	public void onGetTransitRouteResult(TransitRouteResult result) {

		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(PassStartActivity.this, "抱歉，未找到结果",
					Toast.LENGTH_SHORT).show();
			StartRead("无结果，请单击屏幕重试", Config.ACK_NONE);
		}
		if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
			// 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
			// result.getSuggestAddrInfo()
			return;
		}
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {

			nodeIndex = -1;
			mBtnPre.setVisibility(View.VISIBLE);
			mBtnNext.setVisibility(View.VISIBLE);
			route = result.getRouteLines().get(0);
			TransitRouteOverlay overlay = new MyTransitRouteOverlay(mBaidumap);
			mBaidumap.setOnMarkerClickListener(overlay);
			routeOverlay = overlay;
			overlay.setData(result.getRouteLines().get(0));
			overlay.addToMap();
			overlay.zoomToSpan();

			// TODO
			String location = "";

			if (nodeIndex < -1 || route == null || route.getAllStep() == null
					|| nodeIndex > route.getAllStep().size()) {
				return;
			}
			// 设置节点索引
			if (nodeIndex < route.getAllStep().size() - 1) {
				nodeIndex++;
			}

			if (nodeIndex < 0 || nodeIndex >= route.getAllStep().size()) {
				return;
			}

			// 获取节结果信息
			while (nodeIndex >= 0 && nodeIndex < route.getAllStep().size()) {
				// 获取节结果信息
				LatLng nodeLocation = null;
				String routeTitle = null;
				Object step = route.getAllStep().get(nodeIndex);

				
				String nodeTitle = ((TransitRouteLine.TransitStep) step).getEntrace().getTitle();
				

				nodeLocation = ((TransitRouteLine.TransitStep) step)
						.getEntrace().getLocation();
				nodeTitle = ((TransitRouteLine.TransitStep) step)
						.getInstructions();

				location += routeTitle;
				
				routeTitle = ((TransitRouteLine.TransitStep) step)
						.getInstructions();
				
				String start = route.getStarting().getTitle();
				String endd = route.getTerminal().getTitle();
				String end = ((TransitRouteLine.TransitStep) step).getExit().getTitle();
				
				String endpoint = routeTitle.substring(routeTitle.indexOf("到达")+2, routeTitle.length());
				
				if(routeTitle.indexOf("步行")>=0 && nodeIndex==0){
					// 调用算路
					StartRead("正在计算步行路线",Config.ACK_NONE);
					path_binder.findPath(startpoint, "A");
					break;
				}

				nodeIndex++;

			}

			Log.v("node location", location);
			StartRead(location, Config.ACK_ROUTE_RETURN);
		}

	}

	@Override
	public void onGetDrivingRouteResult(DrivingRouteResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(PassStartActivity.this, "抱歉，未找到结果",
					Toast.LENGTH_SHORT).show();
		}
		if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
			// 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
			// result.getSuggestAddrInfo()
			return;
		}
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			nodeIndex = -1;
			mBtnPre.setVisibility(View.VISIBLE);
			mBtnNext.setVisibility(View.VISIBLE);
			route = result.getRouteLines().get(0);
			DrivingRouteOvelray overlay = new MyDrivingRouteOverlay(mBaidumap);
			routeOverlay = overlay;
			mBaidumap.setOnMarkerClickListener(overlay);
			overlay.setData(result.getRouteLines().get(0));
			overlay.addToMap();
			overlay.zoomToSpan();
		}
	}

	// 定制RouteOverly
	private class MyDrivingRouteOverlay extends DrivingRouteOvelray {

		public MyDrivingRouteOverlay(BaiduMap baiduMap) {
			super(baiduMap);
		}

		@Override
		public BitmapDescriptor getStartMarker() {
			if (useDefaultIcon) {
				return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
			}
			return null;
		}

		@Override
		public BitmapDescriptor getTerminalMarker() {
			if (useDefaultIcon) {
				return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
			}
			return null;
		}
	}

	private class MyWalkingRouteOverlay extends WalkingRouteOverlay {

		public MyWalkingRouteOverlay(BaiduMap baiduMap) {
			super(baiduMap);
		}

		@Override
		public BitmapDescriptor getStartMarker() {
			if (useDefaultIcon) {
				return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
			}
			return null;
		}

		@Override
		public BitmapDescriptor getTerminalMarker() {
			if (useDefaultIcon) {
				return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
			}
			return null;
		}
	}

	private class MyTransitRouteOverlay extends TransitRouteOverlay {

		public MyTransitRouteOverlay(BaiduMap baiduMap) {
			super(baiduMap);
		}

		@Override
		public BitmapDescriptor getStartMarker() {
			if (useDefaultIcon) {
				return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
			}
			return null;
		}

		@Override
		public BitmapDescriptor getTerminalMarker() {
			if (useDefaultIcon) {
				return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
			}
			return null;
		}
	}

	@Override
	protected void onPause() {
		mMapView.onPause();

		Log.v(TAG, "pause");

		super.onPause();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		// bindService(intent_main_service, connection, BIND_AUTO_CREATE);
		Log.v(TAG, "resume");
		super.onResume();
	}

	@Override
	protected void onStart() {

		// bind
		Intent intent_voice_service = new Intent(this, VoiceService.class);
		startService(intent_voice_service);
		bindService(intent_voice_service, connection_voice, BIND_AUTO_CREATE);
		Intent intent_path_service = new Intent(this,
				PathOperationService.class);
		startService(intent_path_service);
		bindService(intent_path_service, connection_path, BIND_AUTO_CREATE);

		Intent intent_bluetooth_service = new Intent(this,
				BluetoothService.class);
		startService(intent_bluetooth_service);
		bindService(intent_bluetooth_service, connection_bluetooth,
				BIND_AUTO_CREATE);
		super.onStart();
	}

	@Override
	protected void onStop() {
		Log.v(TAG, "stop");
		super.onStop();
	}

	@Override
	protected void onDestroy() {

		super.onDestroy();
		if(connection_voice!=null)
            unbindService(connection_voice);
		if(connection_path!=null)
            unbindService(connection_path);
		if(connection_bluetooth!=null){
			bluetooth_binder.stopService();
            unbindService(connection_bluetooth);
		}

		// 退出时销毁定位
		mLocClient.stop();
		// 关闭定位图层
		mBaidumap.setMyLocationEnabled(false);
		Log.v(TAG, "destory");
		mMapView.onDestroy();
		mMapView = null;

	}

	/**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			if (location == null) {
				return;
			}
			
			// 更新地图上点位置
			MyLocationData locData = new MyLocationData.Builder()
			.accuracy(location.getRadius())
			// 此处设置开发者获取到的方向信息，顺时针0-360
			.direction(100).latitude(location.getLatitude())
			.longitude(location.getLongitude()).build();
	mBaidumap.setMyLocationData(locData);
			if (isFirstLoc) {

				city = location.getCity();

//				Message msg = Message.obtain();
//				msg.what = Config.ACK_SAY_END;
//				BaseActivity.sendMessage(msg);

				isFirstLoc = false;

			}

			if (loc_q.size() == 5) {
				loc_q.poll();
			}

			loc_q.add(location);
			
			if(!endpoint.equals("unknown")){
				
			}
			
			// TODO 判定是否到达终点
		}

		public void onReceivePoi(BDLocation poiLocation) {

		}
	}

	
	
	private void sendHistory(){
		if(loc_q.size()>0){
			float loc_fl = loc_q.poll().getRadius();
			String loc_str = String.valueOf(loc_fl);
			SimpleDateFormat formatter = new SimpleDateFormat    ("yyyy年MM月dd日    HH:mm:ss     ");     
			Date curDate = new Date(System.currentTimeMillis());//获取当前时间     
			String  str  =  formatter.format(curDate);
			con.setHistory(Constant.userName, loc_str, str);			
		}
	}
	
	private void sendMessage(String phone, String message){
		 
        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, PassStartActivity.class), 0);
 
        SmsManager sms = SmsManager.getDefault();
 
        sms.sendTextMessage(phone, null, message, pi, null);
 
    }
	private boolean endCheck=false;
	@Override
	public void processMessage(Message message) {
		
		if(voice_flag && bluetooth_flag && path_flag){
			flagflag++;
			Log.v("lanlan","服务启动成功");
			//StartRead("服务启动成功",Config.ACK_NOTHING);
			server_checkpoint = true;
			//TODO DONE测试方法，这个StartRead应该注释掉
			//StartRead("服务启动成功", Config.ACK_SAY_END);
			voice_flag = false;
			bluetooth_flag = false;
			path_flag = false;
			//path_binder.downloadInitService();
		}
		
		switch (message.what) {
		case Config.ACK_OPEN_ROUTE:
			StartRead("请根据提示说出起点和终点", Config.ACK_SAY_START);
			break;
		case Config.ACK_SAY_START:
			et = (EditText) findViewById(R.id.et_start);
			StartRead("起点", Config.ACK_LISTEN_START);
			break;
		case Config.ACK_LISTEN_START:
			StartListen(Config.ACK_SAY_END);
			break;
		case Config.ACK_SAY_END:
			Log.i("lanlan", "开始");
			StartRead("请根据提示说出终点", Config.ACK_NONE);
			break;
		case Config.ACK_LISTEN_END:
			endCheck=false;
			StartListen(Config.ACK_START_ROUTE);
			break;
		case Config.ACK_START_ROUTE:
			/* old */
			// et.setText((String) message.obj);
			// StartRoute();
			/* new */
			// path_binder.findPath("001", (String) message.obj);
			Log.i("lanlan", "找路"+(String)message.obj);
			if(((String)message.obj).equals("LANLANERROR")){
				StartRead("请重试，终点", Config.ACK_NONE);
				Log.i("lanlan", "重试第一次");
			}else{
				checkpoint = true;
				endCheck=true;
				et.setText((String) message.obj);
				et = (EditText) findViewById(R.id.et_end);
				//TODO DONE此处001应该是前面得到的起点
				String temp=(String) message.obj;
				newString = temp.substring(0,temp.length()-1);
				//startpoint = "023";
				path_binder.findPath(startpoint, newString);
				//path_binder.findPath(startpoint, (String) message.obj);
				
			}
			break;
		case Config.ACK_ROUTE_RETURN:
			// finish();
			break;
		case Config.SUCCESS:
			String next = (String) message.obj;
			StartRead(next, Config.ACK_NONE);
			break;
		case Config.FAIl:
			StartRead("已偏离，正在重新查找路线", Config.ACK_NONE);
			//String nowString=(String) message.obj;
			path_binder.findPath(startpoint, newString);
			break;
		case Config.ACK_BLUE_SUCCESS:
			startpoint = (String) message.obj;
			if(startpoint == Config.ACK_ISSUE){
				//TODO 表示发送短信,最好加上当前位置，问高晓旭
				db.getReceiverAndDetail();
				sendMessage(Constant.receiver,Constant.detail);
			}else{
				Log.i("lanlan", "蓝牙传来消息"+(String)message.obj);
				//TODO DONE蓝牙测试方法，应该删除
				//path_binder.CheckPoint(startpoint);
				//TODO DONE蓝牙正式方法
				if (!checkpoint&&server_checkpoint) {
					checkpoint = true;
					StartRead("起点为"+startpoint, Config.ACK_SAY_END);
				} else if(endCheck){
					path_binder.CheckPoint(startpoint);
					//sendHistory();
				}
			}
			break;
		case Config.ACK_BLUE_M_SUCCESS:
			StartRead("前方"+(String)message.obj+"有路障", Config.ACK_NONE);
			break;
		case Config.ACK_BLUE_CON_SUCCESS:
			Log.i(Config.TAG, "bluetooth 连接成功");
			break;
		case Config.ACK_END_POINT:
			StartRead("已到达终点", Config.ACK_NONE);
			checkpoint = false;
			endCheck=false;
			break;
		case Config.NONEPLACE:
			StartRead("没有找到地方", Config.ACK_NONE);
			break;
		case Config.ACK_NOTHING:
			break;
		case Config.ACK_FINDPATH_SUCCESS:  // path service success
			StartRead((String)message.obj,Config.ACK_NONE);
			//TODO DONE蓝牙测试方法
			//bluetooth_binder.startTimer();
			break;
		case Config.ACK_FINDPATH_FAIL:
			StartRead("未找到地方,请根据提示说出终点", Config.ACK_SAY_END);
			break;
		case Config.ACK_CHECKPOINT_SUCCESS:
			StartRead((String)message.obj, Config.ACK_NONE);
			break;

		default:
			break;

		}

		if (flag_iat && flag_tts) {
			StartRead("请根据提示说出起点和终点", Config.ACK_SAY_START);
			flag_iat = false;

		}

	}

}
