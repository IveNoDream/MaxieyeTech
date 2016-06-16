package com.maxieyetech.bt;

import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.maxieyetech.bt.utils.BTClient;
import com.maxieyetech.bt.utils.BTDeviceAdapter;
import com.maxieyetech.bt.utils.BTItem;
import com.maxieyetech.bt.utils.BTManager;
import com.maxieyetech.bt.utils.BTMsg;
import com.maxieyetech.bt.utils.BTStatus;

public class BTTestMainActivity extends Activity implements BTStatus,AdapterView.OnItemClickListener{

	private Button mBTEnable;
	private Button mBTDisable;
	//private Button mBTDiscovery;
	//private Button mBTCancelDiscovery;
	private Button mBTSearchDevices;
	private Button mSendMessage;
	private Button mDisconnect;
	private BTDeviceAdapter mAdapter;
	private ListView mDeviceList;
	private BTManager mManager = null;
	private boolean mIsRegister = false;
	private BTClient client;
	private TextView mTvReveive;
	private Button mBtnClear;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bttest_main);

		mBTEnable = (Button)findViewById(R.id.btn_enable);
		mBTDisable = (Button)findViewById(R.id.btn_disable);
		//mBTDiscovery = (Button)findViewById(R.id.btn_start_discovery);
		//mBTCancelDiscovery = (Button)findViewById(R.id.btn_stop_discovery);
		mBTSearchDevices = (Button) findViewById(R.id.btn_search_devices);
		mSendMessage = (Button) findViewById(R.id.btn_send_msg);
		mDisconnect = (Button) findViewById(R.id.btn_disconnect);
		mTvReveive = (TextView) findViewById(R.id.tv_receive);
		mBtnClear = (Button) findViewById(R.id.btn_clear);

		mBTEnable.setOnClickListener(listener);
		mBTDisable.setOnClickListener(listener);
		//mBTDiscovery.setOnClickListener(listener);
		//mBTCancelDiscovery.setOnClickListener(listener);
		mBTSearchDevices.setOnClickListener(listener);
		mSendMessage.setOnClickListener(listener);
		mDisconnect.setOnClickListener(listener);
		mBtnClear.setOnClickListener(listener);

		mAdapter = new BTDeviceAdapter(this);
		mDeviceList = (ListView) findViewById(R.id.lv_device_list);
		mDeviceList.setOnItemClickListener(this);
		mDeviceList.setAdapter(mAdapter);

		mManager = BTManager.getInstance();
		mManager.setBTStatusListner(this);

		Log.i("mijie", "gps enable: " + isGpsEnable(BTTestMainActivity.this));

		//client = new BTClient(BTManager.getInstance().getAdapter(),handler);
	}

	public static final boolean isGpsEnable(final Context context) {
		LocationManager locationManager
				= (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		if (gps || network) {
			return true;
		}
		return false;
	}

	public View.OnClickListener listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()){
				case R.id.btn_enable:
					mManager.openBT(BTTestMainActivity.this);
					break;
				case R.id.btn_disable:
					mManager.closeBT();
					break;
				/*case R.id.btn_start_discovery:
					mManager.startDiscoveringDevice();
					break;
				case R.id.btn_stop_discovery:
					mManager.stopDiscoveringDevice();
					break;*/
				case R.id.btn_search_devices:
					mManager.startDiscoveringDevice();
					break;
				case R.id.btn_send_msg:
					//BTClient client = new BTClient(BTManager.getInstance().getAdapter(),handler);
					//client.connectBTServer(BTMsg.BlueToothAddress);
					client.sendmsg("M6123456678");
					break;
				case R.id.btn_disconnect:
					client.closeBTClient();
					break;
				case R.id.btn_clear:
					mTvReveive.setText("");
					break;
				default:

					break;
			}
		}
	};

	@Override
	protected void onStart() {
		super.onStart();
		if (!mIsRegister){
			mIsRegister = true;
			BTManager.getInstance().registerBluetoothReceiver(this);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mIsRegister) {
			BTManager.getInstance().unregisterBluetooth(this);
		}
	}

	@Override
	public void BTDeviceSearchStatus(int resultCode) {
		switch (resultCode){
			case BTStatus.SEARCH_START:
				mAdapter.clearData();
				mAdapter.addDataModel(BTManager.getInstance().getPairBluetoothItem());
				break;
			case BTStatus.SEARCH_END:
				break;
		}
	}

	@Override
	public void BTSearchFindItem(BTItem item) {
		mAdapter.addDataModel(item);
	}

	@Override
	public void BTConnectStatus(int result) {

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		BTItem item = (BTItem) mAdapter.getItem(position);
		Log.i("mijie","device name: " + item.getBTAddress() + " name: " + item.getBTName() + " type: " + item.getBTType());
		BTMsg.BlueToothAddress = item.getBTAddress();
		if (BTMsg.LastblueToothAddress != BTMsg.BlueToothAddress){
			BTMsg.LastblueToothAddress = BTMsg.BlueToothAddress;
		}
		client = new BTClient(BTManager.getInstance().getAdapter(),handler);
		client.connectBTServer(BTMsg.BlueToothAddress);
	}

	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what){
				case 0:
					Toast.makeText(BTTestMainActivity.this,msg.getData().getString("msg"),Toast.LENGTH_LONG).show();
					Log.i("mijie","msg: " + msg.getData().getString("msg"));
					break;
				case 1:
					mTvReveive.append(msg.getData().getString("msg") + "\n");
					Log.i("mijie","receive msg: " + msg.getData().getString("msg"));
					break;
				default:
					break;
			}
		}
	};
}
