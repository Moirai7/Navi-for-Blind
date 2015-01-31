package com.navi.blind;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfigeration;
import com.baidu.mapapi.map.MyLocationConfigeration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.baidu.mapapi.overlayutil.TransitRouteOverlay;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRouteResult;

import com.navi.blind.R;
import com.navi.client.Config;
import com.navi.model.History;

/**
 * 此demo用来展示如何结合定位SDK实现定位，并使用MyLocationOverlay绘制定位位置 同时展示如何使用自定义图标绘制并点击时弹出泡泡
 * 
 */
public class ShowPositionActivity extends BaseActivity {

	// 定位相关
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	private LocationMode mCurrentMode;
	BitmapDescriptor mCurrentMarker;

	MapView mMapView;
	BaiduMap mBaiduMap;

	// UI相关
	OnCheckedChangeListener radioButtonListener;
	Button requestLocButton, locUri, bt_history;
	boolean isFirstLoc = true;// 是否首次定位
	TextView tv;
	Intent intent;

	float loc_fl;
	double longtitude;
	double latitude;

	// route
	// 浏览路线节点相关
	Button mBtnPre = null;// 上一个节点
	Button mBtnNext = null;// 下一个节点
	int nodeIndex = -2;// 节点索引,供浏览节点时使用
	RouteLine route = null;
	OverlayManager routeOverlay = null;
	boolean useDefaultIcon = false;
	private TextView popupText = null;// 泡泡view
	private View viewCache = null;

	// 地图相关，使用继承MapView的MyRouteMapView目的是重写touch事件实现泡泡处理
	// 如果不处理touch事件，则无需继承，直接使用MapView即可

	BaiduMap mBaidumap = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// SDKInitializer.initialize(getApplicationContext());

		setContentView(R.layout.activity_showpos);

		tv = (TextView) findViewById(R.id.tv_location);
		tv.setText("当前位置");
		tv.setTextColor(Color.WHITE);

		requestLocButton = (Button) findViewById(R.id.button1);
		locUri = (Button) findViewById(R.id.button2);
		bt_history = (Button) findViewById(R.id.history);

		mCurrentMode = LocationMode.NORMAL;
		requestLocButton.setText("普通");
		OnClickListener btnClickListener = new OnClickListener() {
			public void onClick(View v) {
				switch (mCurrentMode) {
				case NORMAL:
					requestLocButton.setText("跟随");
					mCurrentMode = LocationMode.FOLLOWING;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfigeration(
									mCurrentMode, true, mCurrentMarker));
					break;
				case COMPASS:
					requestLocButton.setText("普通");
					mCurrentMode = LocationMode.NORMAL;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfigeration(
									mCurrentMode, true, mCurrentMarker));
					break;
				case FOLLOWING:
					requestLocButton.setText("罗盘");
					mCurrentMode = LocationMode.COMPASS;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfigeration(
									mCurrentMode, true, mCurrentMarker));
					break;
				}
			}
		};

		requestLocButton.setOnClickListener(btnClickListener);

		locUri.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Uri uri = getIntent().getData();
				String[] loc_str;
				if (uri != null) {
					loc_str = uri.getQueryParameter("id").split("@");

					loc_fl = Float.valueOf(loc_str[0]);
					longtitude = Float.valueOf(loc_str[1]);
					latitude = Float.valueOf(loc_str[2]);

					MyLocationData locData = new MyLocationData.Builder()
							.accuracy(loc_fl)
							// 此处设置开发者获取到的方向信息，顺时针0-360
							.direction(100).latitude(latitude)
							.longitude(longtitude).build();

					mBaiduMap.setMyLocationData(locData);

					LatLng ll = new LatLng(latitude, longtitude);
					MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
					mBaiduMap.animateMapStatus(u);

				}
			}

		});

		bt_history.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = getIntent();
				Bundle bd = intent.getBundleExtra("bd_history");

				ArrayList<String> loc = bd.getStringArrayList("loc");
				ArrayList<String> time = bd.getStringArrayList("time");
				List<LatLng> points = new ArrayList<LatLng>();
				
				for (int i=0; i<loc.size(); i++) {
					String loc_str = loc.get(i);
					
					String[] info = loc_str.split("@");
					
					if(info.length == 3){
						double latitude = Double.valueOf(info[1]);
						double longtitude = Double.valueOf(info[2]);
						LatLng ll = new LatLng(latitude, longtitude);
						points.add(ll);
						
						String time_str = time.get(i);
						drawText(ll,time_str);						
					}
					
				}

				
				addCustomElements(points);
			}

		});

		// 地图初始化
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);
		// 定位初始化
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setAddrType("all"); // 设置有返回值
		option.setScanSpan(1000);
		mLocClient.setLocOption(option);
		mLocClient.start();

		intent = getIntent();

		// Uri uri = getIntent().getData();
		// String[] loc_str;
		// if(uri != null){
		// loc_str = uri.getQueryParameter("id").split(";;;");
		//
		// loc_fl = Float.valueOf(loc_str[0]);
		// longtitude = Float.valueOf(loc_str[1]);
		// latitude = Float.valueOf(loc_str[2]);
		//
		// // MyLocationData locData = new MyLocationData.Builder()
		// // .accuracy(loc_fl)
		// // // 此处设置开发者获取到的方向信息，顺时针0-360
		// // .direction(100).latitude(latitude)
		// // .longitude(longtitude).build();
		// //
		// // LatLng ll = new LatLng(latitude,
		// // longtitude);
		// // MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
		// // mBaiduMap.animateMapStatus(u);
		//
		// }

		// mBaiduMap.setMyLocationData(locData);

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

	/**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置

			if (location == null || mMapView == null) {
				return;
			}

			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(100).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			// mBaiduMap.setMyLocationData(locData);
			if (isFirstLoc) {
				isFirstLoc = false;
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				// mBaiduMap.animateMapStatus(u);
			}

			// set the location info at the tv_location

		}

		public void onReceivePoi(BDLocation poiLocation) {

		}

	}

	@Override
	protected void onPause() {
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// 退出时销毁定位
		mLocClient.stop();
		// 关闭定位图层
		mBaiduMap.setMyLocationEnabled(false);
		mMapView.onDestroy();
		mMapView = null;
		super.onDestroy();
	}

	@Override
	public void processMessage(Message message) {
		// TODO Auto-generated method stub

	}

	public class PathInfo {
		private String latitude;
		private String longitude;

		public String getLatitude() {
			return latitude;
		}

		public void setLatitude(String latitude) {
			this.latitude = latitude;
		}

		public String getLongitude() {
			return longitude;
		}

		public void setLongitude(String longitude) {
			this.longitude = longitude;
		}

		@Override
		public String toString() {
			return "{lat:" + latitude + "," + "lng:" + longitude + "}";
		}
	}

	public void addCustomElements(List<LatLng> points) {

		// double la=latLng.latitude;
		// double lon=latLng.longitude;
		// // 添加折线
		// /*LatLng p1 = new LatLng(39.97923, 116.357428);
		// LatLng p2 = new LatLng(39.94923, 116.397428);
		// LatLng p3 = new LatLng(39.97923, 116.437428);*/
		// LatLng p1 = new LatLng(la,lon);
		// LatLng p2 = new LatLng(la-0.03, lon+0.03);
		// LatLng p3 = new LatLng(la-0.03, lon);
		// List<LatLng> points = new ArrayList<LatLng>();
		// points.add(p1);
		// points.add(p2);
		// points.add(p3);
		if(points.size()>2){
			OverlayOptions ooPolyline = new PolylineOptions().width(6)
					.color(0xAAFF0000).points(points);
			mBaiduMap.addOverlay(ooPolyline);			
		}
		
		



	}
	
	protected void drawText(LatLng llText,String content) {
		// 构建文字Option对象，用于在地图上添加文字
		OverlayOptions textOption = new TextOptions().bgColor(0xAAFFFF00)
				.fontSize(24).fontColor(0xFFFF00FF).text(content).rotate(0)
				.position(llText);
		// 在地图上添加该文字对象并显示
		mBaiduMap.addOverlay(textOption);		
	}

}
